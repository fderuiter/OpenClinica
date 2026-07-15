package org.akaza.openclinica.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.akaza.openclinica.bean.submit.EditFormDTO;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.ResponseTypeDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.ResponseType;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Service
public class EditFormService {

    @Autowired
    private CrfVersionDao crfVersionDao;

    @Autowired
    private StudyEventDao studyEventDao;

    @Autowired
    private StudyEventDefinitionDao studyEventDefinitionDao;

    @Autowired
    private StudySubjectDao studySubjectDao;

    @Autowired
    private EventCrfDao eventCrfDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemGroupDao itemGroupDao;

    @Autowired
    private ItemGroupMetadataDao itemGroupMetadataDao;

    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;

    @Autowired
    private ResponseTypeDao responseTypeDao;

    @Autowired
    private ItemDataDao itemDataDao;

    @Transactional
    public EditFormDTO getEditFormDetails(HashMap<String, String> userContext) throws Exception {
        StudyEventDefinition eventDef = studyEventDefinitionDao.findById(Integer.valueOf(userContext.get("studyEventDefinitionID")));
        CrfVersion crfVersion = crfVersionDao.findByOcOID(userContext.get("crfVersionOID"));
        StudySubject subject = studySubjectDao.findByOcOID(userContext.get("studySubjectOID"));
        StudyEvent event = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(eventDef.getOc_oid(), Integer.valueOf(userContext.get("studyEventOrdinal")),
                subject.getStudySubjectId());
        EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdCrfVersionId(event.getStudyEventId(), subject.getStudySubjectId(),
                crfVersion.getCrfVersionId());

        String populatedInstance = getPopulatedInstance(crfVersion, eventCrf);

        return new EditFormDTO(populatedInstance, crfVersion.getOcOid(), subject.getOcOid());
    }

    private String getPopulatedInstance(CrfVersion crfVersion, EventCrf eventCrf) throws Exception {
        boolean isXform = false;
        if (crfVersion.getXform() != null && !crfVersion.getXform().equals(""))
            isXform = true;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder build = docFactory.newDocumentBuilder();
        Document doc = build.newDocument();

        Element crfElement = null;
        if (isXform)
            crfElement = doc.createElement(crfVersion.getXformName());
        else
            crfElement = doc.createElement(crfVersion.getOcOid());
        doc.appendChild(crfElement);

        ArrayList<ItemGroup> itemGroups = itemGroupDao.findByCrfVersionId(crfVersion.getCrfVersionId());
        for (ItemGroup itemGroup : itemGroups) {
            ItemGroupMetadata itemGroupMetadata = itemGroupMetadataDao.findByItemGroupCrfVersion(itemGroup.getItemGroupId(), crfVersion.getCrfVersionId()).get(0);
            ArrayList<Item> items = (ArrayList<Item>) itemDao.findByItemGroupCrfVersionOrdered(itemGroup.getItemGroupId(), crfVersion.getCrfVersionId());

            int maxGroupRepeat = itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), items.get(0).getItemId());
            String repeatGroupMin = itemGroupMetadata.getRepeatNumber().toString();
            Boolean isrepeating = itemGroupMetadata.isRepeatingGroup();

            for (int i = 0; i < maxGroupRepeat; i++) {
                Element groupElement = null;

                if (isXform)
                    groupElement = doc.createElement(itemGroup.getName());
                else
                    groupElement = doc.createElement(itemGroup.getOcOid());
                Element repeatOrdinal = null;
                if (isrepeating) {
                    repeatOrdinal = doc.createElement("OC.REPEAT_ORDINAL");
                    repeatOrdinal.setTextContent(String.valueOf(i+1));
                    groupElement.appendChild(repeatOrdinal);
                }
                boolean hasItemData = false;
                for (Item item : items) {
                    ItemFormMetadata itemMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                    ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), i + 1);

                    Element question = null;
                    if (crfVersion.getXform() != null && !crfVersion.getXform().equals(""))
                        question = doc.createElement(item.getName());
                    else
                        question = doc.createElement(item.getOcOid());

                    if (itemData != null && itemData.getValue() != null && !itemData.getValue().equals("")) {
                        ResponseType responseType = responseTypeDao.findByItemFormMetaDataId(itemMetadata.getItemFormMetadataId());
                        String itemValue = itemData.getValue();
                        if (responseType.getResponseTypeId() == 3 || responseType.getResponseTypeId() == 7) {
                            itemValue = itemValue.replaceAll(",", " ");
                        }

                        question.setTextContent(itemValue);
                    }
                    if (itemData==null || !itemData.isDeleted()) { 
                        hasItemData = true; 
                        groupElement.appendChild(question);
                    }
                } 
                if (hasItemData) {
                    crfElement.appendChild(groupElement);
                }
            }
        } 

        TransformerFactory transformFactory = TransformerFactory.newInstance();
        Transformer transformer = transformFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        String instance = writer.toString();
        System.out.println("Editable instance = " + instance);
        return instance;
    }
}
