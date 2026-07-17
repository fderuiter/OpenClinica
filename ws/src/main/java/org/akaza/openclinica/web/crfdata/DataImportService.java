package org.akaza.openclinica.web.crfdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.web.job.CrfBusinessLogicHelper;
import org.akaza.openclinica.web.job.TriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author thickerson, daniel
 * 
 */
public class DataImportService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    ResourceBundle respage;

    public ResourceBundle getRespage() {
        return respage;
    }

    public void setRespage(ResourceBundle respage) {
        this.respage = respage;
    }

    Locale locales;

    public Locale getLocale() {
        if (locales == null)
            locales = new Locale("en-US");
        return locales;
    }

    public void setLocale(Locale locale) {
        if (locale == null)
            locale = new Locale("en-us");
        this.locales = locale;
    }

    private ImportCRFDataService dataService;

    public List<String> validateMetaData(ODMContainer odmContainer, DataSource dataSource, CoreResources resources, StudyBean studyBean,
            UserAccountBean userBean, List<DisplayItemBeanWrapper> displayItemBeanWrappers, HashMap<Integer, String> importedCRFStatuses) {

        logger.debug("passing an odm container and study bean id: " + studyBean.getId());
        List<String> errors = getImportCRFDataService(dataSource).validateStudyMetadata(odmContainer, studyBean.getId());

        if (errors == null)
            return new ArrayList<String>();
        else
            return errors;

    }

    /**
     * Import Data, the logic which imports the data for our data service. Note that we will return three strings string
     * 0: status, either 'success', 'fail', or 'warn'. string 1: the message string which will be returned in our soap
     * response string 2: the audit message, currently not used but will be saved in the event of a success.
     * 
     * import consist from 3 steps 1) parse xml and extract data 2) validation 3) data submission
     * 
     * @author thickerson
     * @param dataSource
     * @param resources
     * @param studyBean
     * @param userBean
     * @param xml
     * @return
     * @throws Exception
     * 
     *             /* VALIDATE data on all levels
     * 
     *             msg - contains status messages
     * @return list of errors
     */
    public List<String> validateData(ODMContainer odmContainer, DataSource dataSource, CoreResources resources, StudyBean studyBean, UserAccountBean userBean,
            List<DisplayItemBeanWrapper> displayItemBeanWrappers, HashMap<Integer, String> importedCRFStatuses) {
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle();
        setRespage(respage);
        TriggerService triggerService = new TriggerService();

        StringBuffer auditMsg = new StringBuffer();
        List<String> errors = new ArrayList<String>();

        // htaycher: return back later?
        auditMsg.append(respage.getString("passed_study_check") + " ");
        auditMsg.append(respage.getString("passed_oid_metadata_check") + " ");

        // validation errors, the same as in the ImportCRFDataServlet. DRY?
        Boolean eventCRFStatusesValid = getImportCRFDataService(dataSource).eventCRFStatusesValid(odmContainer, userBean);
        List<EventCRFBean> eventCRFBeans = getImportCRFDataService(dataSource).fetchEventCRFBeans(odmContainer, userBean);
        // The following line updates a map that is used for setting the EventCRF status post import
        getImportCRFDataService(dataSource).fetchEventCRFStatuses(odmContainer, importedCRFStatuses);

        ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();

        // -- does the event already exist? if not, fail
        if (eventCRFBeans == null) {
            errors.add(respage.getString("the_event_crf_not_correct_status"));
            return errors;
        } else if (eventCRFBeans.isEmpty() && !eventCRFStatusesValid) {
            errors.add(respage.getString("the_event_crf_not_correct_status"));
            return errors;
        } else if (eventCRFBeans.isEmpty()) {
            errors.add(respage.getString("no_event_crfs_matching_the_xml_metadata"));
            return errors;
        }
        logger.debug("found a list of eventCRFBeans: " + eventCRFBeans.toString());

        for (EventCRFBean eventCRFBean : eventCRFBeans) {
            DataEntryStage dataEntryStage = eventCRFBean.getStage();
            Status eventCRFStatus = eventCRFBean.getStatus();

            logger.debug("Event CRF Bean: id " + eventCRFBean.getId() + ", data entry stage " + dataEntryStage.getName() + ", status "
                    + eventCRFStatus.getName());
            if (eventCRFStatus.equals(Status.AVAILABLE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
                    || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
                    || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                permittedEventCRFIds.add(new Integer(eventCRFBean.getId()));
            } else {
                errors.add(respage.getString("your_listed_crf_in_the_file") + " " + eventCRFBean.getEventName());
                continue;
            }
        }

        if (eventCRFBeans.size() >= permittedEventCRFIds.size()) {
            auditMsg.append(respage.getString("passed_event_crf_status_check") + " ");
        } else {
            auditMsg.append(respage.getString("the_event_crf_not_correct_status") + " ");
        }

        // List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
        HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
        HashMap<String, String> hardValidationErrors = new HashMap<String, String>();

        try {
            List<DisplayItemBeanWrapper> tempDisplayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
            // htaycher: this should be rewritten with validator not to use request to store data
            

            tempDisplayItemBeanWrappers = getImportCRFDataService(dataSource).lookupValidationErrors(getLocale(), odmContainer, userBean, totalValidationErrors,
                    hardValidationErrors, permittedEventCRFIds);
            displayItemBeanWrappers.addAll(tempDisplayItemBeanWrappers);
            logger.debug("size of total validation errors: " + (totalValidationErrors.size() + hardValidationErrors.size()));
            ArrayList<SubjectDataBean> subjectData = odmContainer.getCrfDataPostImportContainer().getSubjectData();
            if (!hardValidationErrors.isEmpty()) {
                // check here where to get group repeat key
                errors.add(triggerService.generateHardValidationErrorMessage(subjectData, hardValidationErrors, "1"));
            }
            if (!totalValidationErrors.isEmpty()) {
                errors.add(triggerService.generateHardValidationErrorMessage(subjectData, totalValidationErrors, "1"));
            }

        } catch (NullPointerException npe1) {
            // what if you have 2 event crfs but the third is a fake?
            npe1.printStackTrace();
            errors.add(respage.getString("an_error_was_thrown_while_validation_errors"));
            logger.debug("=== threw the null pointer, import === " + npe1.getMessage());
        } catch (OpenClinicaException oce1) {
            errors.add(oce1.getOpenClinicaMessage());
            logger.debug("=== threw the openclinica message, import === " + oce1.getOpenClinicaMessage());
        }

        auditMsg.append(respage.getString("passing_crf_edit_checks") + " ");

        return errors;

    }

    public ArrayList<String> submitData(final ODMContainer odmContainer, final DataSource dataSource, final StudyBean studyBean, final UserAccountBean userBean,
            final List<DisplayItemBeanWrapper> displayItemBeanWrappers, final Map<Integer, String> importedCRFStatuses) throws Exception {

        org.springframework.transaction.support.TransactionTemplate transactionTemplate = 
            (org.springframework.transaction.support.TransactionTemplate) org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("sharedTransactionTemplate");
        
        return transactionTemplate.execute(new org.springframework.transaction.support.TransactionCallback<ArrayList<String>>() {
            @Override
            public ArrayList<String> doInTransaction(org.springframework.transaction.TransactionStatus status) {
                java.util.Set<Integer> lockedCrfIds = new java.util.HashSet<Integer>();
                try {
            boolean discNotesGenerated = false;

        ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
        itemDataDao.setFormatDates(false);
        EventCRFDAO eventCrfDao = new EventCRFDAO(dataSource);

        StringBuffer auditMsg = new StringBuffer();
        int eventCrfBeanId = -1;
        EventCRFBean eventCrfBean = null;
        ArrayList<Integer> eventCrfInts;
        ItemDataBean itemDataBean;

        CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(dataSource);
        org.akaza.openclinica.dao.hibernate.ItemDataDao itemDataHibernateDao = (org.akaza.openclinica.dao.hibernate.ItemDataDao) org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("itemDataDao");
        int itemDataCount = 0;
        for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {
            boolean resetSDV = false;

            logger.debug("right before we check to make sure it is savable: " + wrapper.isSavable());
            if (wrapper.isSavable()) {
                eventCrfInts = new ArrayList<Integer>();
                logger.debug("wrapper problems found : " + wrapper.getValidationErrors().toString());
                if (wrapper.getDisplayItemBeans() != null && wrapper.getDisplayItemBeans().size() == 0) {
                    return getReturnList("fail", "", "No items to submit. Please check your XML.");
                }
                
                int eventCrfBeanIdProcessed = 0;
                
                for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
                    eventCrfBeanId = displayItemBean.getData().getEventCRFId();
                    eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
                    
                    try {
                        org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService unifiedService = 
                            org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext()
                            .getBean(org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService.class);
                        unifiedService.validateLock(eventCrfBeanId, userBean.getId());
                        lockedCrfIds.add(eventCrfBeanId);
                    } catch (org.akaza.openclinica.service.clinical.exception.CRFLockedException | org.akaza.openclinica.service.clinical.exception.ClinicalWorkflowException e) {
                        return getReturnList("fail", "", "CRF or Study Event is locked/signed and cannot be modified via API.");
                    }

                    logger.debug("found value here: " + displayItemBean.getData().getValue());
                    logger.debug("found status here: " + eventCrfBean.getStatus().getName());
                    
                    /**
                     *  OC-8239
                     *  now  it's time to update database for the migrated CRF version
                     */
                    int currentCRFVersionId = eventCrfBean.getCRFVersionId();
                    int newCRFVersionId =  displayItemBean.getMetadata().getCrfVersionId();
                    if(currentCRFVersionId != newCRFVersionId && eventCrfBeanIdProcessed != eventCrfBeanId) {
                       eventCrfDao.updateCRFVersionID(eventCrfBeanId, newCRFVersionId, userBean.getId());
                       eventCrfBeanIdProcessed = eventCrfBeanId;

                       // also update the value in current memory
                       eventCrfBean.setCRFVersionId(newCRFVersionId);
                     }
      			    
                    boolean localResetSDV = false;
                    org.akaza.openclinica.domain.datamap.ItemData idData = itemDataHibernateDao.findByItemEventCrfOrdinal(displayItemBean.getData().getItemId(), displayItemBean.getData().getEventCRFId(), displayItemBean.getData().getOrdinal());
                    if (wrapper.isOverwrite() && idData != null && idData.getStatus() != null) {
                        if (!idData.getValue().equals(displayItemBean.getData().getValue())) {
                            localResetSDV = true;
                        }
                        idData.setDateUpdated(new java.util.Date());
                        idData.setUpdateId(userBean.getId());
                        idData.setValue(displayItemBean.getData().getValue());
                        if (displayItemBean.getData().getStatus() != null) {
                            idData.setStatus((org.akaza.openclinica.domain.Status) itemDataHibernateDao.getEntityManager().getReference(org.akaza.openclinica.domain.Status.class, displayItemBean.getData().getStatus().getId()));
                        }
                    } else if (idData == null) {
                        localResetSDV = true;
                        idData = new org.akaza.openclinica.domain.datamap.ItemData();
                        idData.setDateCreated(new java.util.Date());
                        idData.setItem((org.akaza.openclinica.domain.datamap.Item) itemDataHibernateDao.getEntityManager().getReference(org.akaza.openclinica.domain.datamap.Item.class, displayItemBean.getData().getItemId()));
                        idData.setEventCrf((org.akaza.openclinica.domain.datamap.EventCrf) itemDataHibernateDao.getEntityManager().getReference(org.akaza.openclinica.domain.datamap.EventCrf.class, displayItemBean.getData().getEventCRFId()));
                        idData.setUserAccount((org.akaza.openclinica.domain.user.UserAccount) itemDataHibernateDao.getEntityManager().getReference(org.akaza.openclinica.domain.user.UserAccount.class, userBean.getId()));
                        idData.setValue(displayItemBean.getData().getValue());
                        idData.setOrdinal(displayItemBean.getData().getOrdinal());
                        int statusId = (displayItemBean.getData().getStatus() != null) ? displayItemBean.getData().getStatus().getId() : 1;
                        idData.setStatus((org.akaza.openclinica.domain.Status) itemDataHibernateDao.getEntityManager().getReference(org.akaza.openclinica.domain.Status.class, statusId));
                    }
                    
                    if (localResetSDV) {
                        org.springframework.context.ApplicationContext appCtx = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext();
                        org.akaza.openclinica.domain.datamap.Study study = appCtx.getBean(org.akaza.openclinica.dao.hibernate.StudyDao.class).findById(studyBean.getId());
                        org.akaza.openclinica.domain.user.UserAccount userAcc = appCtx.getBean(org.akaza.openclinica.dao.hibernate.UserAccountDao.class).findById(userBean.getId());
                        org.akaza.openclinica.domain.datamap.StudySubject ss = appCtx.getBean(org.akaza.openclinica.dao.hibernate.StudySubjectDao.class).findById(eventCrfBean.getStudySubjectId());
                        org.akaza.openclinica.domain.datamap.EventCrf evCrf = appCtx.getBean(org.akaza.openclinica.dao.hibernate.EventCrfDao.class).findById(eventCrfBean.getId());
                        
                        org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService unifiedService = appCtx.getBean(org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService.class);
                        org.akaza.openclinica.domain.datamap.ItemData savedData = unifiedService.saveItemData(idData, evCrf, study, userAcc, ss);
                        displayItemBean.getData().setId(savedData.getItemDataId());
                        unifiedService.executeRulesAndMetadata(evCrf, study, userAcc);
                    }
                    
                    try {
                        org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService unifiedService = 
                            org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext()
                            .getBean(org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService.class);
                        if (displayItemBean.getData().getId() > 0 && eventCrfBean != null) {
                            unifiedService.captureReasonForChange(displayItemBean.getData().getId(), eventCrfBean.getId(), studyBean.getId(), userBean.getId(), eventCrfBean.getStudySubjectId());
                        }
                    } catch (Exception e) {
                        logger.error("Failed to capture reason for change: ", e);
                    }

                    if (localResetSDV) {
                        resetSDV = true;
                    }
                    
                    if (++itemDataCount % 50 == 0) {
                        itemDataHibernateDao.getEntityManager().flush();
                        itemDataHibernateDao.getEntityManager().clear();
                    }
                }

                for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
                    eventCrfBeanId = displayItemBean.getData().getEventCRFId();
                    eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
                    ItemDAO idao = new ItemDAO(dataSource);
                    ItemBean ibean = (ItemBean) idao.findByPK(displayItemBean.getData().getItemId());
                    // logger.debug("*** checking for validation errors: " + ibean.getName());
                    String itemOid = displayItemBean.getItem().getOid() + "_" + wrapper.getStudyEventRepeatKey() + "_" + displayItemBean.getData().getOrdinal()
                            + "_" + wrapper.getStudySubjectOid();
                    // logger.debug("+++ found validation errors hash map: " +
                    // wrapper.getValidationErrors().toString());
                    if (wrapper.getValidationErrors().containsKey(itemOid)) {
                        ArrayList<String> messageList = (ArrayList<String>) wrapper.getValidationErrors().get(itemOid);
                        for (String message : messageList) {
                            DiscrepancyNoteBean parentDn = createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, null, userBean, dataSource,
                                    studyBean);
                            createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, parentDn.getId(), userBean, dataSource, studyBean);
                            discNotesGenerated = true;
                            logger.debug("*** created disc note with message: " + message);
                            auditMsg.append(wrapper.getStudySubjectOid() + ": " + ibean.getOid() + ": " + message + "---");
                            // split by this ? later, tbh
                            // displayItemBean);
                        }
                    }

                    if (!eventCrfInts.contains(new Integer(eventCrfBean.getId()))) {
                        String eventCRFStatus = importedCRFStatuses.get(new Integer(eventCrfBean.getId()));

                        if (eventCRFStatus != null && eventCRFStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY.getName())
                                && eventCrfBean.getStatus().isAvailable()) {
                            crfBusinessLogicHelper.markCRFStarted(eventCrfBean, userBean, true);
                        } else {
                            crfBusinessLogicHelper.markCRFComplete(eventCrfBean, userBean, true);
                        }
                        eventCrfInts.add(new Integer(eventCrfBean.getId()));
                    }
                }
                // Reset the SDV status if item data has been changed or added
                if (eventCrfBean != null && resetSDV)
                    eventCrfDao.setSDVStatus(false, userBean.getId(), eventCrfBean.getId());
            }
        }
        if (!discNotesGenerated) {
            return getReturnList("success", "", auditMsg.toString());
        } else {
            return getReturnList("warn", "", auditMsg.toString());
        }
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw new RuntimeException("Transaction was rolled back: " + e.getMessage(), e);
                } finally {
            try {
                org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService unifiedService = 
                    org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext()
                    .getBean(org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService.class);
                for (Integer lockedCrfId : lockedCrfIds) {
                    unifiedService.unlock(lockedCrfId);
                }
            } catch (Exception e) {
                logger.error("Error unlocking CRFs during SOAP data import", e);
            }
        }
            }
        });
    }

    public DiscrepancyNoteBean createDiscrepancyNote(ItemBean itemBean, String message, EventCRFBean eventCrfBean, DisplayItemBean displayItemBean,
            Integer parentId, UserAccountBean uab, DataSource ds, StudyBean study) {

        DiscrepancyNoteBean note = new DiscrepancyNoteBean();
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);
        note.setDescription(message);
        note.setDetailedNotes("Failed Validation Check");
        note.setOwner(uab);
        note.setCreatedDate(new Date());
        note.setResolutionStatusId(ResolutionStatus.OPEN.getId());
        note.setDiscrepancyNoteTypeId(DiscrepancyNoteType.FAILEDVAL.getId());
        if (parentId != null) {
            note.setParentDnId(parentId);
        }

        note.setField(itemBean.getName());
        note.setStudyId(study.getId());
        note.setEntityName(itemBean.getName());
        note.setEntityType("ItemData");
        note.setEntityValue(displayItemBean.getData().getValue());

        note.setEventName(eventCrfBean.getName());
        note.setEventStart(eventCrfBean.getCreatedDate());
        note.setCrfName(displayItemBean.getEventDefinitionCRF().getCrfName());

        StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        note.setSubjectName(ss.getName());

        note.setEntityId(displayItemBean.getData().getId());
        note.setColumn("value");

        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(ds);
        note = (DiscrepancyNoteBean) dndao.create(note);
        // so that the below method works, need to set the entity above
        logger.debug("trying to create mapping with " + note.getId() + " " + note.getEntityId() + " " + note.getColumn() + " " + note.getEntityType());
        dndao.createMapping(note);
        logger.debug("just created mapping");
        return note;
    }

    public List<ImportDataRuleRunnerContainer> runRulesSetup(DataSource dataSource, StudyBean studyBean, UserAccountBean userBean,
            List<SubjectDataBean> subjectDataBeans, RuleSetServiceInterface ruleSetService) {
        List<ImportDataRuleRunnerContainer> containers = new ArrayList<ImportDataRuleRunnerContainer>();
        if (ruleSetService.getCountByStudy(studyBean) > 0) {
            
            org.akaza.openclinica.logic.importdata.SubjectDataProcessor.process(subjectDataBeans, new org.akaza.openclinica.logic.importdata.SubjectDataProcessor<Object>() {
public void process(SubjectDataBean subjectDataBean) {
                ImportDataRuleRunnerContainer container = new ImportDataRuleRunnerContainer();
                container.initRuleSetsAndTargets(dataSource, studyBean, subjectDataBean, ruleSetService);
                if (container.getShouldRunRules())
                    containers.add(container);
            }
});

            if (containers != null && !containers.isEmpty())
                ruleSetService.runRulesInImportData(containers, studyBean, userBean, ExecutionMode.DRY_RUN);
        }
        return containers;
    }

    public List<String> runRules(StudyBean studyBean, UserAccountBean userBean, List<ImportDataRuleRunnerContainer> containers,
            RuleSetServiceInterface ruleSetService, ExecutionMode executionMode) {
        List<String> messages = new ArrayList<String>();
        if (containers != null && !containers.isEmpty()) {
            HashMap<String, ArrayList<String>> summary = ruleSetService.runRulesInImportData(containers, studyBean, userBean, executionMode);
            messages = extractRuleActionWarnings(summary);
        }
        return messages;
    }

    private List<String> extractRuleActionWarnings(HashMap<String, ArrayList<String>> summaryMap) {
        List<String> messages = new ArrayList<String>();
        if (summaryMap != null && !summaryMap.isEmpty()) {
            for (String key : summaryMap.keySet()) {
                StringBuilder mesg = new StringBuilder(key + " : ");
                for (String s : summaryMap.get(key)) {
                    mesg.append(s + ", ");
                }
                messages.add(mesg.toString());
            }
        }
        return messages;
    }

    private ImportCRFDataService getImportCRFDataService(DataSource dataSource) {
        /*
         * if (locale == null) {locale = new Locale("en-US");} dataService = this.dataService != null? dataService : new
         * ImportCRFDataService(dataSource, locale);
         */
        return new ImportCRFDataService(dataSource, getLocale());
    }

    private ArrayList<String> getReturnList(String status, String msg, String auditMsg) {
        ArrayList<String> retList = new ArrayList<String>(3);
        retList.add(status);
        retList.add(msg.toString());
        retList.add(auditMsg.toString());
        return retList;
    }
}
