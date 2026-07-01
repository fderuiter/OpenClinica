/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import org.akaza.openclinica.bean.login.UserAccountBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.extract.CommaReportBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExportFormatBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.SPSSReportBean;
import org.akaza.openclinica.bean.extract.TabReportBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.extract.GenerateExtractFileService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.bean.ArchivedDatasetFileRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.job.XalanTriggerService;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdScheduler;
import org.springframework.scheduling.quartz.JobDetailBean;

/**
 * Take a dataset and show it in different formats,<BR/> Detect whether or not
 * files exist in the system or database,<BR/> Give the user the option of
 * showing a stored dataset, or refresh the current one.
 * </P>
 *
 * TODO eventually allow for a thread to be split off, so that exporting can run
 * seperately from the servlet and be retrieved at a later time.
 *
 * @author thickerson
 *
 *
 */
public class ExportDatasetServlet extends SecureController {
    // Background worker pool
    private static final int MAX_WORKERS = 5;
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_WORKERS);
    private static final ConcurrentHashMap<String, ExportTask> exportTasks = new ConcurrentHashMap<String, ExportTask>();

    public static ExportTask getExportTask(String id) {
        return exportTasks.get(id);
    }

    public static int getActiveTaskCount() {
        return executor.getActiveCount();
    }


    public static String getLink(int dsId) {
        return "ExportDataset?datasetId=" + dsId;
    }

    private StdScheduler scheduler;

    private static String SCHEDULER = "schedulerFactoryBean";
    private static final String DATASET_DIR = SQLInitServlet.getField("filePath") + "datasets" + File.separator;

    private static String WEB_DIR = "/WEB-INF/datasets/";
    // may not use the above, security issue
    public File SASFile;
    public String SASFilePath;
    public File SPSSFile;
    public String SPSSFilePath;
    public File TXTFile;
    public String TXTFilePath;
    public File CSVFile;
    public String CSVFilePath;
    public ArrayList fileList;

    @Override
    public void processRequest() throws Exception {
        DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        ArchivedDatasetFileDAO asdfdao = new ArchivedDatasetFileDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);

        GenerateExtractFileService generateFileService = new GenerateExtractFileService(sm.getDataSource(),
                (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources"),
                (RuleSetRuleDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetRuleDao"));
        String action = fp.getString("action");
        int datasetId = fp.getInt("datasetId");
        int adfId = fp.getInt("adfId");
        if (datasetId == 0) {
            try {
                DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
                datasetId = dsb.getId();
                logger.info("dataset id was zero, trying session: " + datasetId);
            } catch (NullPointerException e) {

                e.printStackTrace();
                logger.info("tripped over null pointer exception");
            }
        }
        DatasetBean db = (DatasetBean) dsdao.findByPK(datasetId);
       StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean)sdao.findByPK(db.getStudyId());
        checkRoleByUserAndStudy(ub, study.getParentStudyId(), study.getId());

        //Checks if the study is current study or child of current study
        if (study.getId() != currentStudy.getId() && study.getParentStudyId() != currentStudy.getId()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                    + " " + respage.getString("change_active_study_or_contact"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }
        /**
         * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION get study_id and
         *      parentstudy_id int currentstudyid = currentStudy.getId(); int
         *      parentstudy = currentStudy.getParentStudyId(); if (parentstudy >
         *      0) { // is OK } else { // same parentstudy = currentstudyid; } //
         */
        int currentstudyid = currentStudy.getId();
        // YW 11-09-2008 << modified logic here.
        int parentstudy = currentstudyid;
        // YW 11-09-2008 >>

        StudyBean parentStudy = new StudyBean();
        if (currentStudy.getParentStudyId() > 0) {
            //StudyDAO sdao = new StudyDAO(sm.getDataSource());
            parentStudy = (StudyBean) sdao.findByPK(currentStudy.getParentStudyId());
        }

        ExtractBean eb = generateFileService.generateExtractBean(db, currentStudy, parentStudy);

        // new ExtractBean(sm.getDataSource());
        // eb.setDataset(db);
        // eb.setShowUniqueId(SQLInitServlet.getField("show_unique_id"));
        // eb.setStudy(currentStudy);
        // eb.setParentStudy(parentStudy);
        // eb.setDateCreated(new java.util.Date());

        if (StringUtil.isBlank(action)) {
            loadList(db, asdfdao, datasetId, fp, eb);
            forwardPage(Page.EXPORT_DATASETS);
        } else if ("delete".equalsIgnoreCase(action) && adfId > 0) {
            boolean success = false;
            ArchivedDatasetFileBean adfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(adfId);
            File file = new File(adfBean.getFileReference());
            if (!file.canWrite()) {
                addPageMessage(respage.getString("write_protected"));
            } else {
                success = file.delete();
                if (success) {
                    asdfdao.deleteArchiveDataset(adfBean);
                    addPageMessage(respage.getString("file_removed"));
                } else {
                    addPageMessage(respage.getString("error_removing_file"));
                }
            }
            loadList(db, asdfdao, datasetId, fp, eb);
            forwardPage(Page.EXPORT_DATASETS);
        } else {
            logger.info("**** found action ****: " + action);
            if ("html".equalsIgnoreCase(action)) {
                // html based dataset browser
                TabReportBean answer = new TabReportBean();
                eb = dsdao.getDatasetData(eb, currentstudyid, parentstudy);
                eb.getMetadata();
                eb.computeReport(answer);
                request.setAttribute("dataset", db);
                request.setAttribute("extractBean", eb);
                forwardPage(Page.GENERATE_DATASET_HTML);
            } else {
                // Check pool capacity
                if (executor.getActiveCount() >= MAX_WORKERS) {
                    addPageMessage("The system is currently processing the maximum number of dataset exports. Please try again later.");
                    loadList(db, asdfdao, datasetId, fp, eb);
                    forwardPage(Page.EXPORT_DATASETS);
                    return;
                }

                final ExportTask task = new ExportTask();
                exportTasks.put(task.getId(), task);

                final String fAction = action;
                final int fCurrentStudyId = currentstudyid;
                final int fParentStudy = parentstudy;
                final StudyBean fCurrentStudy = currentStudy;
                final StudyBean fParentStudyObj = parentStudy;
                final String fOdmVersion = fp.getString("odmVersion");
                final String fXalan = fp.getString("xalan");
                final DatasetBean fDb = db;
                final ExtractBean fEb = eb;
                final GenerateExtractFileService fGenerateFileService = generateFileService;
                final UserAccountBean fUb = ub;
                final DatasetDAO fDsdao = dsdao;

                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            long sysTimeBegin = System.currentTimeMillis();
                            int fId = 0;
                            String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
                            SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
                            String generalFileDir = DATASET_DIR + fDb.getId() + File.separator + sdfDir.format(new java.util.Date());
                            
                            fDb.setName(fDb.getName().replaceAll(" ", "_"));
                            
                            if ("sas".equalsIgnoreCase(fAction)) {
                                long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
                                String SASFileName = fDb.getName() + "_sas.sas";
                                fId = fGenerateFileService.createFile(SASFileName, generalFileDir, "", fDb, sysTimeEnd, ExportFormatBean.TXTFILE, true, fUb);
                            } else if ("odm".equalsIgnoreCase(fAction)) {
                                String ODMXMLFileName = "";
                                HashMap answerMap = fGenerateFileService.createODMFile(fOdmVersion, sysTimeBegin, generalFileDir, fDb, fCurrentStudy, "", fEb, fCurrentStudy.getId(), fCurrentStudy.getParentStudyId(), "99", true, true, true, null, fUb);
                                for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
                                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                                    ODMXMLFileName = (String) entry.getKey();
                                    Integer fileID = (Integer) entry.getValue();
                                    fId = fileID.intValue();
                                }
                                if (fXalan != null) {
                                    XalanTriggerService xts = new XalanTriggerService();
                                    String propertiesPath = SQLInitServlet.getField("filePath");
                                    openZipFile(generalFileDir + ODMXMLFileName + ".zip");
                                    SimpleTrigger simpleTrigger = xts.generateXalanTrigger(propertiesPath + File.separator + "ODMReportStylesheet.xsl",
                                            ODMXMLFileName, generalFileDir + "output.sql", fDb.getId());
                                    StdScheduler sched = getScheduler();
                                    org.springframework.scheduling.quartz.JobDetailBean jobDetailBean = new org.springframework.scheduling.quartz.JobDetailBean();
                                    jobDetailBean.setGroup(xts.TRIGGER_GROUP_NAME);
                                    jobDetailBean.setName(simpleTrigger.getName());
                                    jobDetailBean.setJobClass(org.akaza.openclinica.web.job.XalanStatefulJob.class);
                                    jobDetailBean.setJobDataMap(simpleTrigger.getJobDataMap());
                                    jobDetailBean.setDurability(true);
                                    jobDetailBean.setVolatility(false);
                                    sched.scheduleJob(jobDetailBean, simpleTrigger);
                                }
                            } else if ("txt".equalsIgnoreCase(fAction)) {
                                fDsdao.getDatasetData(fEb, fCurrentStudyId, fParentStudy);
                                HashMap answerMap = fGenerateFileService.createTabFile(fEb, sysTimeBegin, generalFileDir, fDb, fCurrentStudyId, fParentStudy, "", fUb);
                                for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
                                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                                    Integer fileID = (Integer) entry.getValue();
                                    fId = fileID.intValue();
                                }
                            } else if ("spss".equalsIgnoreCase(fAction)) {
                                SPSSReportBean answer = new SPSSReportBean();
                                fDsdao.getDatasetData(fEb, fCurrentStudyId, fParentStudy);
                                fEb.getMetadata();
                                fEb.computeReport(answer);
                                HashMap answerMap = fGenerateFileService.createSPSSFile(fDb, fEb, fCurrentStudy, fParentStudyObj, sysTimeBegin, generalFileDir, answer, "", fUb);
                                for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
                                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                                    Integer fileID = (Integer) entry.getValue();
                                    fId = fileID.intValue();
                                }
                            } else if ("csv".equalsIgnoreCase(fAction)) {
                                CommaReportBean answer = new CommaReportBean();
                                fDsdao.getDatasetData(fEb, fCurrentStudyId, fParentStudy);
                                fEb.getMetadata();
                                fEb.computeReport(answer);
                                long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
                                String CSVFileName = fDb.getName() + "_comma.txt";
                                fId = fGenerateFileService.createFile(CSVFileName, generalFileDir, answer.toString(), fDb, sysTimeEnd, ExportFormatBean.CSVFILE, true, fUb);
                            }
                            
                            task.setDownloadUrl(String.valueOf(fId));
                            task.setStatus("Completed");
                        } catch (Exception e) {
                            e.printStackTrace();
                            task.setStatus("Failed");
                            task.setErrorMessage(e.getMessage());
                        }
                    }
                });

                request.setAttribute("taskId", task.getId());
                request.setAttribute("datasetId", db.getId());
                forwardPage(Page.EXPORT_DATASET_WAIT);
            }
        }
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }

    public ArchivedDatasetFileBean generateFileBean(File datasetFile, String relativePath, int formatId) {
        ArchivedDatasetFileBean adfb = new ArchivedDatasetFileBean();
        adfb.setName(datasetFile.getName());
        if (datasetFile.canRead()) {
            logger.info("File can be read");
        } else {
            logger.info("File CANNOT be read");
        }
        logger.info("Found file length: " + datasetFile.length());
        logger.info("Last Modified: " + datasetFile.lastModified());
        adfb.setFileSize(new Long(datasetFile.length()).intValue());
        adfb.setExportFormatId(formatId);
        adfb.setWebPath(relativePath);
        adfb.setDateCreated(new java.util.Date(datasetFile.lastModified()));
        return adfb;
    }

    private void openZipFile(String fileName) {
        try {
            ZipFile zipFile = new ZipFile(fileName);

            java.util.Enumeration entries = zipFile.entries();

            while(entries.hasMoreElements()) {
              ZipEntry entry = (ZipEntry)entries.nextElement();

              if(entry.isDirectory()) {
                // Assume directories are stored parents first then children.
                logger.debug("Extracting directory: " + entry.getName());
                // This is not robust, just for demonstration purposes.
                (new File(entry.getName())).mkdir();
                // no dirs necessary?
                continue;
              }

              logger.debug("Extracting file: " + entry.getName());
              // System.out.println("Writing to dir " + targetDir);
              copyInputStream(zipFile.getInputStream(entry),
                 new java.io.BufferedOutputStream(new java.io.FileOutputStream(entry.getName())));
            }

            zipFile.close();
          } catch (java.io.IOException ioe) {
        	  logger.error("Unhandled exception:");
            ioe.printStackTrace();
            return;
          }
    }

    public void loadList(DatasetBean db, ArchivedDatasetFileDAO asdfdao, int datasetId, FormProcessor fp, ExtractBean eb) {
        logger.info("action is blank");
        request.setAttribute("dataset", db);
        logger.info("just set dataset to request");
        request.setAttribute("extractProperties", CoreResources.getExtractProperties());
        // find out if there are any files here:
        File currentDir = new File(DATASET_DIR + db.getId() + File.separator);

        //JN: Commenting out this, as its creating directories without any reason. TODO: Check why was this added.
       // if (!currentDir.isDirectory()) {
      //      currentDir.mkdirs();
      //  }

        ArrayList fileListRaw = new ArrayList();
        fileListRaw = asdfdao.findByDatasetId(datasetId);
        fileList = new ArrayList();
        Iterator fileIterator = fileListRaw.iterator();
        while (fileIterator.hasNext()) {
            ArchivedDatasetFileBean asdfBean = (ArchivedDatasetFileBean) fileIterator.next();
            // set the correct webPath in each bean here
            // changed here, tbh, 4-18
            // asdfBean.setWebPath(WEB_DIR+db.getId()+"/"+asdfBean.getName());
            // asdfBean.setWebPath(DATASET_DIR+db.getId()+File.separator+
            // asdfBean.getName());
            asdfBean.setWebPath(asdfBean.getFileReference());
            if (new File(asdfBean.getFileReference()).isFile()) {
                // logger.warn(asdfBean.getFileReference()+" is a
                // file!");
                fileList.add(asdfBean);
            } else {
                logger.warn(asdfBean.getFileReference() + " is NOT a file!");
            }
        }

        logger.warn("");
        logger.warn("file list length: " + fileList.size());
        request.setAttribute("filelist", fileList);

        ArrayList filterRows = ArchivedDatasetFileRow.generateRowsFromBeans(fileList);
        EntityBeanTable table = fp.getEntityBeanTable();
        table.setSortingIfNotExplicitlySet(3, false);// sort by date
        String[] columns =
            { resword.getString("file_name"), resword.getString("run_time"), resword.getString("file_size"), resword.getString("created_date"),
                resword.getString("created_by"), resword.getString("action") };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(0);
        table.hideColumnLink(1);
        table.hideColumnLink(2);
        table.hideColumnLink(3);
        table.hideColumnLink(4);
        table.hideColumnLink(5);

        table.setQuery("ExportDataset?datasetId=" + db.getId(), new HashMap());
        // trying to continue...
        request.setAttribute("newDataset", db);
        table.setRows(filterRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        // for the side info bar
        TabReportBean answer = new TabReportBean();

        resetPanel();
        panel.setStudyInfoShown(false);
        setToPanel(resword.getString("study_name"), eb.getStudy().getName());
        setToPanel(resword.getString("protocol_ID"), eb.getStudy().getIdentifier());
        setToPanel(resword.getString("dataset_name"), db.getName());
        setToPanel(resword.getString("created_date"), local_df.format(db.getCreatedDate()));
        setToPanel(resword.getString("dataset_owner"), db.getOwner().getName());
        try {
            // do we not set this or is it null b/c we come to the page with no session?
            setToPanel(resword.getString("date_last_run"), local_df.format(db.getDateLastRun()));
        } catch (NullPointerException npe) {
            logger.error("exception: " + npe.getMessage());
        }

        logger.warn("just set file list to request, sending to page");

    }

    private StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }

    private static final void copyInputStream(InputStream in, OutputStream out)
    throws IOException
    {
      byte[] buffer = new byte[1024];
      int len = 0;

      while((len = in.read(buffer)) > 0)
        out.write(buffer, 0, len);

      in.close();
      out.close();
    }
}
