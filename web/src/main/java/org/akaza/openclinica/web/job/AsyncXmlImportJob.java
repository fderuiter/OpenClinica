package org.akaza.openclinica.web.job;

import org.akaza.openclinica.control.submit.AsyncImportTask;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.sql.DataSource;
import java.io.File;
import java.util.Locale;

public class AsyncXmlImportJob extends QuartzJobBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public static final String FILE_PATH = "filePath";
    public static final String USER_ID = "userId";
    public static final String STUDY_ID = "studyId";
    public static final String LOCALE = "locale";

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            JobDataMap dataMap = context.getMergedJobDataMap();
            String filePath = dataMap.getString(FILE_PATH);
            int userId = dataMap.getInt(USER_ID);
            int studyId = dataMap.getInt(STUDY_ID);
            String localeStr = dataMap.getString(LOCALE);

            DataSource dataSource = (DataSource) appContext.getBean("dataSource");
            UserAccountDAO udao = new UserAccountDAO(dataSource);
            UserAccountBean ub = (UserAccountBean) udao.findByPK(userId);

            StudyDAO sdao = new StudyDAO(dataSource);
            StudyBean study = (StudyBean) sdao.findByPK(studyId);

            Locale locale = new Locale(localeStr);
            File f = new File(filePath);

            // Using the existing AsyncImportTask logic directly inside a wrapper thread or calling its run()
            // since AsyncImportTask already handles the Spring TransactionTemplate correctly for XML logic.
            AsyncImportTask task = new AsyncImportTask(f, ub, study, dataSource, appContext, locale);
            task.run();
            
        } catch (Exception e) {
            logger.error("Failed to execute AsyncXmlImportJob", e);
            throw new JobExecutionException(e);
        }
    }
}
