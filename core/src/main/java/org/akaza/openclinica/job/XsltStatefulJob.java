package org.akaza.openclinica.job;

import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.StatefulJob;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class XsltStatefulJob extends XsltTransformJob implements StatefulJob, InterruptableJob {
    private StudyDAO _studyDAO;


    private static final Logger LOG = LoggerFactory.getLogger(XsltStatefulJob.class);

    protected JobTerminationMonitor jobTerminationMonitor;

    @Autowired
    public XsltStatefulJob(StudyDAO _studyDAO) {
        super(_studyDAO, _studyDAO);
        this._studyDAO = _studyDAO;

    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        jobTerminationMonitor = JobTerminationMonitor.createInstance(context.getJobDetail().getKey().toString());
        super.executeInternal(context);
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (jobTerminationMonitor != null) {
            LOG.info("Received cancellation request for job " + jobTerminationMonitor.getJobName());
            jobTerminationMonitor.terminate();
        }
    }
}
