package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.web.crfdata.ImportCRFDataService;
import org.akaza.openclinica.web.job.CrfBusinessLogicHelper;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AsyncImportTask implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private File f;
    private UserAccountBean ub;
    private StudyBean currentStudy;
    private DataSource dataSource;
    private ApplicationContext context;
    private Locale locale;
    private ResourceBundle respage;

    public AsyncImportTask(File f, UserAccountBean ub, StudyBean currentStudy, DataSource dataSource, ApplicationContext context, Locale locale) {
        this.f = f;
        this.ub = ub;
        this.currentStudy = currentStudy;
        if (dataSource instanceof org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy) {
            this.dataSource = dataSource;
        } else {
            this.dataSource = new org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy(dataSource);
        }
        this.context = context;
        this.locale = locale;
        ResourceBundleProvider.updateLocale(locale);
        this.respage = ResourceBundleProvider.getPageMessagesBundle(locale);
    }

    private ImportCRFDataService getImportCRFDataService() {
        return new ImportCRFDataService(dataSource, locale);
    }

    @Override
    public void run() {
        try {
            logger.info("Starting background import for file: " + f.getName());
            XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
            File xsdFile = new File(CoreResources.PROPERTIES_DIR + File.separator + "ODM1-3-0.xsd");
            File xsdFile2 = new File(CoreResources.PROPERTIES_DIR + File.separator + "ODM1-2-1.xsd");

            ODMContainer odmContainer = new ODMContainer();
            boolean fail = false;
            StringBuilder errorMessages = new StringBuilder();

            try {
                schemaValidator.validateAgainstSchema(f, xsdFile);
                org.akaza.openclinica.logic.importdata.StreamingSubjectDataList streamingList = new org.akaza.openclinica.logic.importdata.StreamingSubjectDataList(f);
                org.akaza.openclinica.bean.submit.crfdata.CRFDataPostImportContainer container = new org.akaza.openclinica.bean.submit.crfdata.CRFDataPostImportContainer();
                container.setStudyOID(streamingList.getStudyOid());
                container.setUpsertOn(streamingList.getUpsertOn());
                container.setSubjectData(streamingList);
                odmContainer.setCrfDataPostImportContainer(container);
            } catch (Exception me1) {
                try {
                    schemaValidator.validateAgainstSchema(f, xsdFile2);
                    org.akaza.openclinica.logic.importdata.StreamingSubjectDataList streamingList = new org.akaza.openclinica.logic.importdata.StreamingSubjectDataList(f);
                    org.akaza.openclinica.bean.submit.crfdata.CRFDataPostImportContainer container = new org.akaza.openclinica.bean.submit.crfdata.CRFDataPostImportContainer();
                    container.setStudyOID(streamingList.getStudyOid());
                    container.setUpsertOn(streamingList.getUpsertOn());
                    container.setSubjectData(streamingList);
                    odmContainer.setCrfDataPostImportContainer(container);
                } catch (Exception me2) {
                    MessageFormat mf = new MessageFormat("");
                    mf.applyPattern(respage.getString("your_xml_is_not_well_formed"));
                    Object[] arguments = { me1.getMessage() };
                    errorMessages.append(mf.format(arguments)).append("<br/>");
                    fail = true;
                }
            }

            if (!fail) {
                ImportCRFDataService service = getImportCRFDataService();
                List<EventCRFBean> eventCRFBeans = service.fetchEventCRFBeans(odmContainer, ub);
                
                if (eventCRFBeans == null) {
                    fail = true;
                    errorMessages.append(respage.getString("no_event_status_matching")).append("<br/>");
                } else {
                    ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();
                    for (EventCRFBean eventCRFBean : eventCRFBeans) {
                        DataEntryStage dataEntryStage = eventCRFBean.getStage();
                        Status eventCRFStatus = eventCRFBean.getStatus();
                        if (eventCRFStatus.equals(Status.AVAILABLE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
                                || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
                                || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                            permittedEventCRFIds.add(new Integer(eventCRFBean.getId()));
                        } else {
                            fail = true;
                            errorMessages.append("Your listed Event CRF does not exist, or has already been locked for import.").append("<br/>");
                        }
                    }

                    if (!fail) {
                        HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
                        HashMap<String, String> hardValidationErrors = new HashMap<String, String>();
                        
                        List<DisplayItemBeanWrapper> displayItemBeanWrappers = service.lookupValidationErrors(locale, odmContainer, ub, totalValidationErrors,
                                hardValidationErrors, permittedEventCRFIds);

                        HashMap<Integer, String> importedCRFStatuses = service.fetchEventCRFStatuses(odmContainer);
                        ImportCRFInfoContainer importCrfInfo = new ImportCRFInfoContainer(odmContainer, dataSource);

                        // Save Phase
                        TransactionTemplate transactionTemplate = (TransactionTemplate) context.getBean("sharedTransactionTemplate");
                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus status) {
                                try {
                                    org.akaza.openclinica.logic.importdata.StreamingSubjectDataList streamingList = (org.akaza.openclinica.logic.importdata.StreamingSubjectDataList) odmContainer.getCrfDataPostImportContainer().getSubjectData();
                                    service.updateElectronicSignatures(streamingList.getEventSignatureMap(), streamingList.getFormSignatureMap(), ub);

                                    RuleSetServiceInterface ruleSetService = (RuleSetServiceInterface) context.getBean("ruleSetService");
                                    List<ImportDataRuleRunnerContainer> containers = ruleRunSetup(dataSource, currentStudy, ruleSetService, odmContainer);
                                    
                                    CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(dataSource);
                                    ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
                                    EventCRFDAO eventCrfDao = new EventCRFDAO(dataSource);

                                    org.akaza.openclinica.dao.hibernate.ItemDataDao itemDataHibernateDao = (org.akaza.openclinica.dao.hibernate.ItemDataDao) context.getBean("itemDataDao");
                                    int itemDataCount = 0;

                                    for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {
                                        boolean resetSDV = false;
                                        int eventCrfBeanId = -1;
                                        EventCRFBean eventCrfBean = new EventCRFBean();
                                        if (wrapper.isSavable()) {
                                            ArrayList<Integer> eventCrfInts = new ArrayList<Integer>();
                                            itemDataDao.setFormatDates(false);
                                            int eventCrfBeanIdProcessed = 0;
                                            
                                            for (org.akaza.openclinica.bean.submit.DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
                                                eventCrfBeanId = displayItemBean.getData().getEventCRFId();
                                                eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
                                                
                                                int currentCRFVersionId = eventCrfBean.getCRFVersionId();
                                                int newCRFVersionId =  displayItemBean.getMetadata().getCrfVersionId();
                                                if(currentCRFVersionId != newCRFVersionId && eventCrfBeanIdProcessed != eventCrfBeanId) {
                                                   eventCrfDao.updateCRFVersionID(eventCrfBeanId, newCRFVersionId, ub.getId());
                                                   eventCrfBeanIdProcessed = eventCrfBeanId;
                                                   eventCrfBean.setCRFVersionId(newCRFVersionId);
                                                }
                                                
                                                boolean localResetSDV = false;
                                                org.akaza.openclinica.domain.datamap.ItemData idData = itemDataHibernateDao.findByItemEventCrfOrdinal(displayItemBean.getData().getItemId(), displayItemBean.getData().getEventCRFId(), displayItemBean.getData().getOrdinal());
                                                if (wrapper.isOverwrite() && idData != null && idData.getStatus() != null) {
                                                    if (!idData.getValue().equals(displayItemBean.getData().getValue())) {
                                                        localResetSDV = true;
                                                    }
                                                    idData.setDateUpdated(new java.util.Date());
                                                    idData.setUpdateId(ub.getId());
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
                                                    idData.setUserAccount((org.akaza.openclinica.domain.user.UserAccount) itemDataHibernateDao.getEntityManager().getReference(org.akaza.openclinica.domain.user.UserAccount.class, ub.getId()));
                                                    idData.setValue(displayItemBean.getData().getValue());
                                                    idData.setOrdinal(displayItemBean.getData().getOrdinal());
                                                    int statusId = (displayItemBean.getData().getStatus() != null) ? displayItemBean.getData().getStatus().getId() : 1;
                                                    idData.setStatus((org.akaza.openclinica.domain.Status) itemDataHibernateDao.getEntityManager().getReference(org.akaza.openclinica.domain.Status.class, statusId));
                                                }
                                                
                                                if (localResetSDV) {
                                                    org.springframework.context.ApplicationContext appCtx = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext();
                                                    org.akaza.openclinica.domain.datamap.Study study = appCtx.getBean(org.akaza.openclinica.dao.hibernate.StudyDao.class).findById(currentStudy.getId());
                                                    org.akaza.openclinica.domain.user.UserAccount userAcc = appCtx.getBean(org.akaza.openclinica.dao.hibernate.UserAccountDao.class).findById(ub.getId());
                                                    org.akaza.openclinica.domain.datamap.StudySubject ss = appCtx.getBean(org.akaza.openclinica.dao.hibernate.StudySubjectDao.class).findById(eventCrfBean.getStudySubjectId());
                                                    org.akaza.openclinica.domain.datamap.EventCrf evCrf = appCtx.getBean(org.akaza.openclinica.dao.hibernate.EventCrfDao.class).findById(eventCrfBean.getId());
                                                    
                                                    org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService unifiedService = appCtx.getBean(org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService.class);
                                                    org.akaza.openclinica.domain.datamap.ItemData savedData = unifiedService.saveItemData(idData, evCrf, study, userAcc, ss);
                                                    displayItemBean.getData().setId(savedData.getItemDataId());
                                                    unifiedService.executeRulesAndMetadata(evCrf, study, userAcc);
                                                }
                                                if (localResetSDV) {
                                                    resetSDV = true;
                                                }
                                                
                                                if (++itemDataCount % 50 == 0) {
                                                    itemDataHibernateDao.getEntityManager().flush();
                                                    itemDataHibernateDao.getEntityManager().clear();
                                                }
                                                org.akaza.openclinica.dao.submit.ItemDAO idao = new org.akaza.openclinica.dao.submit.ItemDAO(dataSource);
                                                org.akaza.openclinica.bean.submit.ItemBean ibean = (org.akaza.openclinica.bean.submit.ItemBean) idao.findByPK(displayItemBean.getData().getItemId());
                                                String itemOid = displayItemBean.getItem().getOid() + "_" + wrapper.getStudyEventRepeatKey() + "_"
                                                        + displayItemBean.getData().getOrdinal() + "_" + wrapper.getStudySubjectOid();
                                                if (wrapper.getValidationErrors().containsKey(itemOid)) {
                                                    ArrayList messageList = (ArrayList) wrapper.getValidationErrors().get(itemOid);
                                                    for (int iter = 0; iter < messageList.size(); iter++) {
                                                        String message = (String) messageList.get(iter);

                                                        org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean parentDn = org.akaza.openclinica.web.job.ImportSpringJob.createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, null, ub, dataSource, currentStudy);
                                                        org.akaza.openclinica.web.job.ImportSpringJob.createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, parentDn.getId(), ub, dataSource, currentStudy);
                                                    }
                                                }
                                                // Update CRF status
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
                                            itemDataDao.setFormatDates(true);
                                            // Reset the SDV status if item data has been changed or added
                                            if (eventCrfBean != null && resetSDV)
                                                eventCrfDao.setSDVStatus(false, ub.getId(), eventCrfBean.getId());
                                        }
                                    }
                                    
                                    if (containers != null && !containers.isEmpty()) {
                                        ruleSetService.runRulesInImportData(containers, currentStudy, ub, ExecutionMode.SAVE);
                                    }
                                } catch (Exception e) {
                                    status.setRollbackOnly();
                                    logger.error("Error saving imported CRF data", e);
                                    throw new RuntimeException("Error saving imported CRF data", e);
                                }
                            }
                        });

                        SummaryStatsBean ssBean = service.generateSummaryStatsBean(odmContainer, displayItemBeanWrappers, importCrfInfo);
                        sendEmail(ssBean, null);
                        return; // Successfully completed
                    }
                }
            }
            
            // If we reached here, there was a failure
            sendEmail(null, errorMessages.toString());

        } catch (Exception e) {
            logger.error("Exception in background import", e);
            sendEmail(null, "An unexpected error occurred: " + e.getMessage());
        }
    }

    private List<ImportDataRuleRunnerContainer> ruleRunSetup(DataSource dataSource, StudyBean studyBean,
            RuleSetServiceInterface ruleSetService, ODMContainer odmContainer) {
        List<ImportDataRuleRunnerContainer> containers = new ArrayList<ImportDataRuleRunnerContainer>();
        if (odmContainer != null) {
            ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
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
                    ruleSetService.runRulesInImportData(containers, studyBean, ub, ExecutionMode.DRY_RUN);
            }
        }
        return containers;
    }

    private void sendEmail(SummaryStatsBean ssBean, String errors) {
        try {
            OpenClinicaMailSender mailSender = (OpenClinicaMailSender) context.getBean("openClinicaMailSender");
            String toEmail = ub.getEmail();
            String fromEmail = EmailEngine.getAdminEmail();
            String subject = "XML Clinical Data Import Results";
            StringBuilder body = new StringBuilder();
            
            body.append("<p>Dear ").append(ub.getFirstName()).append(" ").append(ub.getLastName()).append(",</p>");
            body.append("<p>Your XML import for file <b>").append(f.getName()).append("</b> has finished processing.</p>");
            
            if (errors != null && !errors.isEmpty()) {
                body.append("<p><b>Status: Failed</b></p>");
                body.append("<p>Errors:</p><ul><li>").append(errors).append("</li></ul>");
            } else if (ssBean != null) {
                body.append("<p><b>Status: Success</b></p>");
                body.append("<p>Import Summary:</p>");
                body.append("<ul>");
                body.append("<li>Total Subjects: ").append(ssBean.getStudySubjectCount()).append("</li>");
                body.append("<li>Imported CRFs: ").append(ssBean.getEventCrfCount()).append("</li>");
                body.append("<li>Skipped CRFs: ").append(ssBean.getSkippedCrfCount()).append("</li>");
                body.append("<li>Discrepancy Notes created: ").append(ssBean.getDiscNoteCount()).append("</li>");
                body.append("</ul>");
            }
            
            mailSender.sendEmail(toEmail, fromEmail, subject, body.toString(), true);
            logger.info("Sent background import result email to " + toEmail);
        } catch (Exception e) {
            logger.error("Failed to send import result email", e);
        }
    }
}
