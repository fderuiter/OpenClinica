package org.akaza.openclinica.web.job;

import org.akaza.openclinica.bean.admin.NewCRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.hibernate.MeasurementUnitDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.control.admin.SpreadSheetTableRepeating;
import org.akaza.openclinica.control.admin.SpreadSheetTableClassic;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.SQLInitServlet;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Iterator;
import org.akaza.openclinica.bean.admin.CRFBean;

public class AsyncExcelImportJob extends QuartzJobBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public static final String FILE_PATH = "filePath";
    public static final String USER_ID = "userId";
    public static final String STUDY_ID = "studyId";
    public static final String LOCALE = "locale";
    public static final String CRF_ID = "crfId";
    public static final String PREVIOUS_VERSION_ID = "previousVersionId";
    public static final String DELETE_PREVIOUS_VERSION = "deletePreviousVersion";
    public static final String VERSION_NAME = "versionName";

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            TransactionTemplate transactionTemplate = (TransactionTemplate) appContext.getBean("sharedTransactionTemplate");
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        processExcel(context, appContext);
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        logger.error("Error in AsyncExcelImportJob", e);
                        sendEmail(context, appContext, false, e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    private void processExcel(JobExecutionContext context, ApplicationContext appContext) throws Exception {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String filePath = dataMap.getString(FILE_PATH);
        int userId = dataMap.getInt(USER_ID);
        int studyId = dataMap.getInt(STUDY_ID);
        String localeStr = dataMap.getString(LOCALE);
        int crfId = dataMap.getInt(CRF_ID);
        int previousVersionId = dataMap.getInt(PREVIOUS_VERSION_ID);
        boolean deletePrevious = dataMap.getBooleanValue(DELETE_PREVIOUS_VERSION);
        String versionName = dataMap.getString(VERSION_NAME);

        Locale locale = new Locale(localeStr);
        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle(locale);

        DataSource rawDataSource = (DataSource) appContext.getBean("dataSource");
        DataSource dataSource;
        if (rawDataSource instanceof org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy) {
            dataSource = rawDataSource;
        } else {
            dataSource = new org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy(rawDataSource);
        }
        
        MeasurementUnitDao measurementUnitDao = (MeasurementUnitDao) appContext.getBean("measurementUnitDao");
        UserAccountDAO udao = new UserAccountDAO(dataSource);
        UserAccountBean ub = (UserAccountBean) udao.findByPK(userId);

        File f = new File(filePath);
        FileInputStream inStream = new FileInputStream(f);
        FileInputStream inStreamClassic = null;
        NewCRFBean nib = null;

        try {
            SpreadSheetTableRepeating htab = new SpreadSheetTableRepeating(inStream, ub, versionName, locale, studyId);
            htab.setMeasurementUnitDao(measurementUnitDao);

            if (htab.isRepeating()) {
                htab.setCrfId(crfId);
                nib = htab.toNewCRF(dataSource, respage);
            } else {
                inStreamClassic = new FileInputStream(filePath);
                SpreadSheetTableClassic sstc = new SpreadSheetTableClassic(inStreamClassic, ub, versionName, locale, studyId);
                sstc.setMeasurementUnitDao(measurementUnitDao);
                sstc.setCrfId(crfId);
                nib = sstc.toNewCRF(dataSource, respage);
            }
        } finally {
            inStream.close();
            if (inStreamClassic != null) {
                inStreamClassic.close();
            }
        }

        if (nib.getErrors() != null && !nib.getErrors().isEmpty()) {
            throw new Exception("Validation errors found in spreadsheet: " + nib.getErrors().toString());
        }

        nib.setVersionName(versionName);
        nib.setCrfId(crfId);

        if (deletePrevious && previousVersionId > 0) {
            CRFVersionDAO cdao = new CRFVersionDAO(dataSource);
            ArrayList items = cdao.findNotSharedItemsByVersion(previousVersionId);
            nib.setDeleteQueries(cdao.generateDeleteQueries(previousVersionId, items));
            nib.deleteInsertToDB();
        } else {
            nib.insertToDB();
        }

        // Post-process logic from Servlet
        CRFVersionDAO cvdao = new CRFVersionDAO(dataSource);
        int crfVersionId = 0;
        ArrayList crfvbeans = cvdao.findAllByCRFId(crfId);
        if (!crfvbeans.isEmpty()) {
            CRFVersionBean cvbean = (CRFVersionBean) crfvbeans.get(crfvbeans.size() - 1);
            crfVersionId = cvbean.getId();
            for (Iterator iter = crfvbeans.iterator(); iter.hasNext();) {
                cvbean = (CRFVersionBean) iter.next();
                if (crfVersionId < cvbean.getId()) {
                    crfVersionId = cvbean.getId();
                }
            }
        }
        if (crfVersionId == 0) {
            crfVersionId = cvdao.findCRFVersionId(crfId, versionName);
        }
        CRFVersionBean finalVersion = (CRFVersionBean) cvdao.findByPK(crfVersionId);

        CRFDAO cdao = new CRFDAO(dataSource);
        CRFBean crfBean = (CRFBean) cdao.findByPK(crfId);
        crfBean.setUpdatedDate(new java.util.Date());
        crfBean.setUpdater(ub);
        cdao.update(crfBean);

        String finalDir = SQLInitServlet.getField("filePath") + "crf" + File.separator + "new" + File.separator;
        if (!new File(finalDir).isDirectory()) {
            new File(finalDir).mkdirs();
        }
        String newFile = crfId + finalVersion.getOid() + ".xls";
        File nf = new File(finalDir + newFile);
        org.springframework.util.FileCopyUtils.copy(f, nf);

        sendEmail(context, appContext, true, null);
    }

    private void sendEmail(JobExecutionContext context, ApplicationContext appContext, boolean success, String errorMessage) {
        try {
            JobDataMap dataMap = context.getMergedJobDataMap();
            int userId = dataMap.getInt(USER_ID);
            String filePath = dataMap.getString(FILE_PATH);
            File f = new File(filePath);

            DataSource dataSource = (DataSource) appContext.getBean("dataSource");
            UserAccountDAO udao = new UserAccountDAO(dataSource);
            UserAccountBean ub = (UserAccountBean) udao.findByPK(userId);

            OpenClinicaMailSender mailSender = (OpenClinicaMailSender) appContext.getBean("openClinicaMailSender");
            String toEmail = ub.getEmail();
            String fromEmail = EmailEngine.getAdminEmail();
            String subject = "Excel CRF Import Results";
            StringBuilder body = new StringBuilder();

            body.append("<p>Dear ").append(ub.getFirstName()).append(" ").append(ub.getLastName()).append(",</p>");
            body.append("<p>Your Excel CRF template upload for file <b>").append(f.getName()).append("</b> has finished processing.</p>");

            if (success) {
                body.append("<p><b>Status: Success</b></p>");
                body.append("<p>The template was successfully validated and saved to the database.</p>");
            } else {
                body.append("<p><b>Status: Failed</b></p>");
                body.append("<p>Errors encountered:</p><p>").append(errorMessage).append("</p>");
            }

            mailSender.sendEmail(toEmail, fromEmail, subject, body.toString(), true);
            logger.info("Sent background excel import result email to " + toEmail);
        } catch (Exception e) {
            logger.error("Failed to send import result email", e);
        }
    }
}
