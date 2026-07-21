package org.akaza.openclinica.controller.openrosa;
import org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import java.util.Date;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.akaza.openclinica.dao.hibernate.NotificationOutboxDao;
import org.akaza.openclinica.domain.datamap.NotificationOutbox;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

@Component
public class OpenRosaSubmissionService {

    @Autowired
    SubmissionProcessorChain submissionProcessorChain;
    
    @Autowired
    StudyDao studyDao;
    
    @Autowired
    CrfVersionDao crfVersionDao;

    @Autowired
    NotificationOutboxDao notificationOutboxDao;
    
    @Autowired
    UnifiedWorkflowEnforcementService unifiedWorkflowEnforcementService;

    @Transactional(timeout = 60)
    public void processRequest(Study study, HashMap<String,String> subjectContext, InputStream requestBody, Errors errors, Locale locale, ArrayList <HashMap> listOfUploadFilePaths) throws Exception {
        // Execute save as Hibernate transaction to avoid partial imports
        CrfVersion crfVersion = crfVersionDao.findByOcOID(subjectContext.get("crfVersionOID"));
        String requestPayload = parseSubmission(requestBody, crfVersion);
        runAsTransaction(study, requestPayload, subjectContext, errors, locale ,listOfUploadFilePaths);

        if (!errors.hasErrors()) {
            NotificationOutbox outbox = new NotificationOutbox();
            outbox.setStudyOid(study.getOc_oid());
            outbox.setStudyEventDefId(Integer.valueOf(subjectContext.get("studyEventDefinitionID")));
            outbox.setStudyEventDefOrdinal(Integer.valueOf(subjectContext.get("studyEventOrdinal")));
            outbox.setCrfVersionOid(subjectContext.get("crfVersionOID"));
            outbox.setStatus("PENDING");
            outbox.setAttemptCount(0);
            outbox.setCreatedAt(new Date());
            notificationOutboxDao.saveOrUpdate(outbox);
        }
    }
    
    private void runAsTransaction(Study study, String requestBody, HashMap<String, String> subjectContext, Errors errors, Locale locale,ArrayList <HashMap> listOfUploadFilePaths) throws Exception{

        SubmissionContainer container = new SubmissionContainer(study,requestBody,subjectContext,errors,locale ,listOfUploadFilePaths);
        try {
            submissionProcessorChain.processSubmission(container);
        } finally {
            if (container.getEventCrf() != null) {
                unifiedWorkflowEnforcementService.unlock(container.getEventCrf());
            }
        }

    }

    private String parseSubmission(InputStream is, CrfVersion crfVersion) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader r = factory.createXMLStreamReader(is);

        StringWriter sw = new StringWriter();
        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter w = outFactory.createXMLStreamWriter(sw);

        w.writeStartElement("instance");

        boolean inForm = false;
        int metaDepth = 0;
        int formDepth = 0;

        String expectedFormName = crfVersion.getXformName();
        boolean hasExpected = (expectedFormName != null && !expectedFormName.isEmpty());

        while (r.hasNext()) {
            int event = r.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    String name = r.getLocalName();
                    if (!inForm) {
                        if ((hasExpected && expectedFormName.equals(name)) ||
                            (!hasExpected && name.startsWith("F_"))) {
                            inForm = true;
                            formDepth = 1;
                            writeStart(w, r);
                        }
                    } else {
                        if (name.equals("meta") && metaDepth == 0) {
                            metaDepth++;
                        } else if (metaDepth > 0) {
                            metaDepth++;
                        } else {
                            formDepth++;
                            writeStart(w, r);
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (inForm) {
                        if (metaDepth > 0) {
                            metaDepth--;
                        } else {
                            formDepth--;
                            w.writeEndElement();
                            if (formDepth == 0) {
                                inForm = false;
                            }
                        }
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (inForm && metaDepth == 0) {
                        w.writeCharacters(r.getText());
                    }
                    break;
            }
        }
        w.writeEndElement(); // instance
        w.flush();
        return sw.toString();
    }
    
    private void writeStart(XMLStreamWriter w, XMLStreamReader r) throws Exception {
        String prefix = r.getPrefix() == null ? "" : r.getPrefix();
        String uri = r.getNamespaceURI() == null ? "" : r.getNamespaceURI();

        w.writeStartElement(prefix, r.getLocalName(), uri);

        for (int i = 0; i < r.getNamespaceCount(); i++) {
            String p = r.getNamespacePrefix(i) == null ? "" : r.getNamespacePrefix(i);
            String u = r.getNamespaceURI(i) == null ? "" : r.getNamespaceURI(i);
            if (!p.isEmpty()) {
                w.writeNamespace(p, u);
            } else {
                w.writeDefaultNamespace(u);
            }
        }

        for (int i = 0; i < r.getAttributeCount(); i++) {
            String p = r.getAttributePrefix(i) == null ? "" : r.getAttributePrefix(i);
            String u = r.getAttributeNamespace(i) == null ? "" : r.getAttributeNamespace(i);
            String ln = r.getAttributeLocalName(i);
            String val = r.getAttributeValue(i);
            if (!u.isEmpty()) {
                w.writeAttribute(p, u, ln, val);
            } else {
                w.writeAttribute(ln, val);
            }
        }
    }


}
