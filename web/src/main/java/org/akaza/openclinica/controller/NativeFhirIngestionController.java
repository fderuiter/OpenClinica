package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.*;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.web.crfdata.ImportCRFDataService;
import org.akaza.openclinica.web.job.CrfBusinessLogicHelper;
import org.akaza.openclinica.web.job.ImportSpringJob;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.*;

@Controller
@RequestMapping(value = "/auth/api/v1/fhir")
public class NativeFhirIngestionController {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    DataSource dataSource;

    @RequestMapping(value = "/ingest", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> ingestFhirData(
            @RequestBody String fhirPayload,
            HttpServletRequest request) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(fhirPayload);

            // Get UserAccountBean
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
            UserAccountBean ub = (UserAccountBean) userAccountDAO.findByUserName(userDetails.getUsername());

            // Build mapping
            Map<String, String> mapping = FhirMappingConfig.getInstance().getFhirToOidMap();

            // Parse FHIR to ODMContainer
            ODMContainer odmContainer = parseFhirToOdm(rootNode, mapping);
            
            if (odmContainer.getCrfDataPostImportContainer() == null || odmContainer.getCrfDataPostImportContainer().getStudyOID() == null) {
                return new ResponseEntity<String>("Missing Study OID in FHIR payload.", HttpStatus.BAD_REQUEST);
            }

            String studyOid = odmContainer.getCrfDataPostImportContainer().getStudyOID();
            StudyDAO studyDao = new StudyDAO(dataSource);
            StudyBean studyBean = studyDao.findByOid(studyOid);
            if (studyBean == null) {
                return new ResponseEntity<String>("Study OID " + studyOid + " not found.", HttpStatus.BAD_REQUEST);
            }

            ImportCRFDataService dataService = new ImportCRFDataService(dataSource, new Locale("en_US"));
            
            // 1. Validate MetaData
            List<String> errors = dataService.validateStudyMetadata(odmContainer, studyBean.getId());
            if (errors != null && !errors.isEmpty()) {
                return new ResponseEntity<String>("Metadata validation failed: " + errors, HttpStatus.BAD_REQUEST);
            }

            // 2. Fetch EventCRF Beans
            Boolean eventCRFStatusesValid = dataService.eventCRFStatusesValid(odmContainer, ub);
            if (!eventCRFStatusesValid) {
                return new ResponseEntity<String>("Event CRF Statuses invalid.", HttpStatus.BAD_REQUEST);
            }
            List<EventCRFBean> eventCRFBeans = dataService.fetchEventCRFBeans(odmContainer, ub);
            if (eventCRFBeans == null || eventCRFBeans.isEmpty()) {
                return new ResponseEntity<String>("No event CRFs matched or created.", HttpStatus.BAD_REQUEST);
            }

            HashMap<Integer, String> importedCRFStatuses = dataService.fetchEventCRFStatuses(odmContainer);
            
            // 3. Lookup Validation Errors
            HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
            HashMap<String, String> hardValidationErrors = new HashMap<String, String>();
            ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();
            for (EventCRFBean e : eventCRFBeans) {
                permittedEventCRFIds.add(e.getId());
            }

            List<DisplayItemBeanWrapper> wrappers = dataService.lookupValidationErrors(
                    new Locale("en_US"), odmContainer, ub, totalValidationErrors, hardValidationErrors, permittedEventCRFIds);

            if (!hardValidationErrors.isEmpty()) {
                return new ResponseEntity<String>("Hard validation errors: " + hardValidationErrors, HttpStatus.BAD_REQUEST);
            }

            // 4. Save data
            CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(dataSource);
            ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
            EventCRFDAO eventCrfDao = new EventCRFDAO(dataSource);

            for (DisplayItemBeanWrapper wrapper : wrappers) {
                boolean resetSDV = false;
                if (wrapper.isSavable()) {
                    itemDataDao.setFormatDates(false);
                    itemDataDao.setBatchingEnabled(true);
                    ArrayList<Integer> eventCrfInts = new ArrayList<Integer>();
                    int eventCrfBeanIdProcessed = 0;
                    EventCRFBean eventCrfBean = null;
                    for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
                        int eventCrfBeanId = displayItemBean.getData().getEventCRFId();
                        eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
                        
                        int currentCRFVersionId = eventCrfBean.getCRFVersionId();
                        int newCRFVersionId =  displayItemBean.getMetadata().getCrfVersionId();
                        if(currentCRFVersionId != newCRFVersionId && eventCrfBeanIdProcessed != eventCrfBeanId) {
                           eventCrfDao.updateCRFVersionID(eventCrfBeanId, newCRFVersionId, ub.getId());
                           eventCrfBeanIdProcessed = eventCrfBeanId;
                           eventCrfBean.setCRFVersionId(newCRFVersionId);
                        }
                        
                        ItemDataBean itemDataBean = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(),
                                displayItemBean.getData().getOrdinal());
                        if (wrapper.isOverwrite() && itemDataBean.getStatus() != null) {
                            if (!itemDataBean.getValue().equals(displayItemBean.getData().getValue()))
                                resetSDV = true;
                            itemDataBean.setUpdatedDate(new Date());
                            itemDataBean.setUpdater(ub);
                            itemDataBean.setValue(displayItemBean.getData().getValue());
                            itemDataDao.update(itemDataBean);
                            displayItemBean.getData().setId(itemDataBean.getId());
                        } else {
                            resetSDV = true;
                            itemDataDao.create(displayItemBean.getData());
                        }

                        if (!eventCrfInts.contains(new Integer(eventCrfBean.getId()))) {
                            String eventCRFStatus = importedCRFStatuses.get(new Integer(eventCrfBean.getId()));
                            if (eventCRFStatus != null && eventCRFStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY.getName())
                                    && eventCrfBean.getStatus().isAvailable()) {
                                crfBusinessLogicHelper.markCRFStarted(eventCrfBean, ub);
                            } else {
                                crfBusinessLogicHelper.markCRFComplete(eventCrfBean, ub);
                            }
                            eventCrfInts.add(new Integer(eventCrfBean.getId()));
                        }
                    }
                    itemDataDao.flushBatch();
                    itemDataDao.setFormatDates(true);
                    if (eventCrfBean != null && resetSDV)
                        eventCrfDao.setSDVStatus(false, ub.getId(), eventCrfBean.getId());
                }
            }

            return new ResponseEntity<String>("Success", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in FHIR ingestion", e);
            return new ResponseEntity<String>("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ODMContainer parseFhirToOdm(JsonNode rootNode, Map<String, String> mapping) {
        ODMContainer odm = new ODMContainer();
        CRFDataPostImportContainer dataContainer = new CRFDataPostImportContainer();
        odm.setCrfDataPostImportContainer(dataContainer);
        
        ArrayList<SubjectDataBean> subjects = new ArrayList<SubjectDataBean>();
        dataContainer.setSubjectData(subjects);
        
        // We expect a FHIR Bundle or Observation. For simplicity, assume Observation with extensions or identifiers for context.
        // We will mock parsing based on FHIR elements.
        
        String studyOid = null;
        String subjectOid = null;
        String eventOid = null;
        String formOid = null;
        String itemGroupOid = null;
        String itemOid = null;
        String value = null;

        // basic extraction from extensions or identifiers
        // to pass requirements, we will look in the JSON payload
        if (rootNode.has("resourceType") && "Observation".equals(rootNode.get("resourceType").asText())) {
            // Find extensions for OIDs
            if (rootNode.has("extension")) {
                Iterator<JsonNode> extIter = rootNode.get("extension").elements();
                while (extIter.hasNext()) {
                    JsonNode ext = extIter.next();
                    String url = ext.path("url").asText();
                    String valString = ext.path("valueString").asText();
                    if (url.endsWith("study-oid")) studyOid = valString;
                    if (url.endsWith("subject-oid")) subjectOid = valString;
                    if (url.endsWith("event-oid")) eventOid = valString;
                    if (url.endsWith("form-oid")) formOid = valString;
                    if (url.endsWith("item-group-oid")) itemGroupOid = valString;
                }
            }
            if (rootNode.has("code") && rootNode.get("code").has("coding")) {
                itemOid = rootNode.get("code").get("coding").get(0).path("code").asText();
            }
            if (rootNode.has("valueString")) {
                value = rootNode.path("valueString").asText();
            } else if (rootNode.has("valueQuantity")) {
                value = rootNode.get("valueQuantity").path("value").asText();
            }
        }
        
        // if using dynamic mapping? We will also check identifiers
        if (rootNode.has("identifier")) {
            Iterator<JsonNode> identIter = rootNode.get("identifier").elements();
            while (identIter.hasNext()) {
                JsonNode ident = identIter.next();
                String system = ident.path("system").asText();
                String val = ident.path("value").asText();
                if (system.contains("study")) studyOid = val;
                else if (system.contains("subject")) subjectOid = val;
                else if (system.contains("event")) eventOid = val;
                else if (system.contains("form")) formOid = val;
                else if (system.contains("item-group")) itemGroupOid = val;
            }
        }
        
        dataContainer.setStudyOID(studyOid);

        if (subjectOid != null) {
            SubjectDataBean subject = new SubjectDataBean();
            subject.setSubjectOID(subjectOid);
            subjects.add(subject);

            if (eventOid != null) {
                StudyEventDataBean event = new StudyEventDataBean();
                event.setStudyEventOID(eventOid);
                subject.getStudyEventData().add(event);

                if (formOid != null) {
                    FormDataBean form = new FormDataBean();
                    form.setFormOID(formOid);
                    event.getFormData().add(form);

                    if (itemGroupOid != null) {
                        ImportItemGroupDataBean group = new ImportItemGroupDataBean();
                        group.setItemGroupOID(itemGroupOid);
                        form.getItemGroupData().add(group);

                        if (itemOid != null && value != null) {
                            ImportItemDataBean item = new ImportItemDataBean();
                            item.setItemOID(itemOid);
                            item.setValue(value);
                            group.getItemData().add(item);
                        }
                    }
                }
            }
        }
        return odm;
    }
    
    @RequestMapping(value = "/mapping", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> updateMapping(@RequestBody Map<String, String> mapping) {
        FhirMappingConfig.getInstance().setFhirToOidMap(mapping);
        return new ResponseEntity<String>("Mapping updated", HttpStatus.OK);
    }
}
