/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import javax.xml.transform.stream.StreamSource;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.crfdata.ImportCRFDataService;
import org.apache.commons.lang.exception.ExceptionUtils;


import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Create a new CRF verison by uploading excel file. Makes use of several other classes to validate and provide accurate
 * validation. More specifically, uses XmlSchemaValidationHelper, ImportCRFDataService, ODMContainer, and others to
 * import all the XML in the ODM 1.3 standard.
 * 
 * @author Krikor Krumlian, Tom Hickerson updated Apr-May 2008
 */
public class ImportCRFDataServlet extends SecureController {


    private static final int MAX_WORKERS = 3;
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_WORKERS);

    Locale locale;


    private ImportCRFDataService dataService;

    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    FileUploadHelper uploadHelper = new FileUploadHelper();

    // < ResourceBundleresword,resexception,respage;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.MENU_SERVLET, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.MENU_SERVLET, respage.getString("current_study_frozen"));

        locale = LocaleResolver.getLocale(request);
        if (ub.isSysAdmin()) {
            return;
        }

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT)
                || r.equals(Role.RESEARCHASSISTANT2)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);

        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        // keep the module in the session
        session.setAttribute(MODULE, module);

        String action = request.getParameter("action");
        CRFVersionBean version = (CRFVersionBean) session.getAttribute("version");

        File xsdFile = new File(SpringServletAccess.getPropertiesDir(context) + "ODM1-3-0.xsd");
        File xsdFile2 = new File(SpringServletAccess.getPropertiesDir(context) + "ODM1-2-1.xsd");

        if (StringUtil.isBlank(action)) {
            logger.info("action is blank");
            request.setAttribute("version", version);
            forwardPage(Page.IMPORT_CRF_DATA);
        }
        if ("confirm".equalsIgnoreCase(action)) {
            String dir = SQLInitServlet.getField("filePath");
            if (!new File(dir).exists()) {
                logger.info("The filePath in datainfo.properties is invalid " + dir);
                addPageMessage(respage.getString("filepath_you_defined_not_seem_valid"));
                forwardPage(Page.IMPORT_CRF_DATA);
            }
            // All the uploaded files will be saved in filePath/crf/original/
            String theDir = dir + "crf" + File.separator + "original" + File.separator;
            if (!new File(theDir).isDirectory()) {
                new File(theDir).mkdirs();
                logger.info("Made the directory " + theDir);
            }
            // MultipartRequest multi = new MultipartRequest(request, theDir, 50 * 1024 * 1024);
            File f = null;
            try {
                f = uploadFile(theDir, version);

            } catch (Exception e) {
                logger.warn("*** Found exception during file upload***");
                e.printStackTrace();

            }
            if (f == null) {
                forwardPage(Page.IMPORT_CRF_DATA);
                return;
            }

            final File finalFile = f;
            final UserAccountBean finalUb = ub;
            final org.akaza.openclinica.bean.managestudy.StudyBean finalStudy = currentStudy;
            final org.springframework.context.ApplicationContext finalContext = SpringServletAccess.getApplicationContext(context);
            final java.util.Locale finalLocale = request.getLocale();
            final javax.sql.DataSource finalDataSource = sm.getDataSource();

            try {
                org.quartz.impl.StdScheduler scheduler = (org.quartz.impl.StdScheduler) SpringServletAccess.getApplicationContext(context).getBean("schedulerFactoryBean");
                org.quartz.impl.JobDetailImpl jobDetail = new org.quartz.impl.JobDetailImpl();
                jobDetail.setName("XmlImport_" + System.currentTimeMillis());
                jobDetail.setGroup("BackgroundImports");
                jobDetail.setJobClass(org.akaza.openclinica.web.job.AsyncXmlImportJob.class);
                
                org.quartz.JobDataMap jobDataMap = new org.quartz.JobDataMap();
                jobDataMap.put(org.akaza.openclinica.web.job.AsyncXmlImportJob.FILE_PATH, finalFile.getAbsolutePath());
                jobDataMap.put(org.akaza.openclinica.web.job.AsyncXmlImportJob.USER_ID, finalUb.getId());
                jobDataMap.put(org.akaza.openclinica.web.job.AsyncXmlImportJob.STUDY_ID, finalStudy.getId());
                jobDataMap.put(org.akaza.openclinica.web.job.AsyncXmlImportJob.LOCALE, finalLocale.toString());
                jobDetail.setJobDataMap(jobDataMap);
                
                org.quartz.impl.triggers.SimpleTriggerImpl trigger = new org.quartz.impl.triggers.SimpleTriggerImpl();
                trigger.setName("Trigger_" + jobDetail.getName());
                trigger.setGroup("BackgroundImports");
                trigger.setStartTime(new java.util.Date());
                
                scheduler.scheduleJob(jobDetail, trigger);
                
                addPageMessage("Your XML clinical data import has been queued for background processing. You will receive an email upon completion.");
            } catch (Exception e) {
                logger.error("Failed to schedule background job", e);
                addPageMessage("Failed to schedule background processing.");
            }
            forwardPage(Page.IMPORT_CRF_DATA);
        }
    }

    /*
     * Given the MultipartRequest extract the first File validate that it is an xml file and then return it.
     */
    private File getFirstFile() {
        File f = null;
        List<File> files = uploadHelper.returnFiles(request, context);
        for (File file : files) {
            // Enumeration files = multi.getFileNames();
            // if (files.hasMoreElements()) {
            // String name = (String) files.nextElement();
            // f = multi.getFile(name);
            f = file;
            if (f == null || f.getName() == null) {
                logger.info("file is empty.");
                Validator.addError(errors, "xml_file", "You have to provide an XML file!");
            } else if (f.getName().indexOf(".xml") < 0 && f.getName().indexOf(".XML") < 0) {
                logger.info("file name:" + f.getName());
                // TODO change the message below
                addPageMessage(respage.getString("file_you_uploaded_not_seem_xml_file"));
                f = null;
            }
        }
        return f;
    }

    /**
     * Uploads the xml file
     * 
     * @param version
     * @throws Exception
     */
    public File uploadFile(String theDir, CRFVersionBean version) throws Exception {

        return getFirstFile();
    }

    public ImportCRFDataService getImportCRFDataService() {
        dataService = this.dataService != null ? dataService : new ImportCRFDataService(sm.getDataSource(), locale);
        return dataService;
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

}
