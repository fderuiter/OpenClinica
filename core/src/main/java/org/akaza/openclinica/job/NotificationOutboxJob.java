package org.akaza.openclinica.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.context.ApplicationContext;
import org.akaza.openclinica.dao.hibernate.NotificationOutboxDao;
import org.akaza.openclinica.domain.datamap.NotificationOutbox;
import org.akaza.openclinica.service.pmanage.Submission;
import org.akaza.openclinica.service.pmanage.Study;
import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class NotificationOutboxJob extends QuartzJobBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            NotificationOutboxDao dao = (NotificationOutboxDao) appContext.getBean("notificationOutboxDao");

            List<NotificationOutbox> pending = dao.findPendingNotifications();
            if (pending == null || pending.isEmpty()) return;

            RestTemplate rest = new RestTemplate();
            String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/submission";
            
            int backoffMultiplier = 5; // default 5 minutes
            try {
                String configuredBackoff = CoreResources.getField("notification.outbox.backoff.multiplier");
                if (configuredBackoff != null && !configuredBackoff.isEmpty()) {
                    backoffMultiplier = Integer.parseInt(configuredBackoff);
                }
            } catch (Exception e) {
                logger.warn("Could not parse backoff multiplier, using default");
            }

            long now = System.currentTimeMillis();

            for (NotificationOutbox outbox : pending) {
                int attempts = outbox.getAttemptCount() != null ? outbox.getAttemptCount() : 0;
                if (attempts > 0 && outbox.getUpdatedAt() != null) {
                    long delayMillis = (long) Math.pow(2, attempts - 1) * backoffMultiplier * 60 * 1000L;
                    if (now - outbox.getUpdatedAt().getTime() < delayMillis) {
                        continue; // Skip this record, backoff hasn't expired
                    }
                }

                try {
                    Submission submission = new Submission();
                    Study pManageStudy = new Study();
                    pManageStudy.setInstanceUrl(CoreResources.getField("sysURL.base") + "rest2/openrosa/" + outbox.getStudyOid());
                    pManageStudy.setStudyOid(outbox.getStudyOid());
                    submission.setStudy(pManageStudy);
                    submission.setStudy_event_def_id(outbox.getStudyEventDefId());
                    submission.setStudy_event_def_ordinal(outbox.getStudyEventDefOrdinal());
                    // Wait, we need crf_version_id, but outbox has crfVersionOid. We need to look it up!
                    // Let's get crfVersionDao from context
                    org.akaza.openclinica.dao.hibernate.CrfVersionDao cvDao = (org.akaza.openclinica.dao.hibernate.CrfVersionDao) appContext.getBean("crfVersionDao");
                    submission.setCrf_version_id(cvDao.findByOcOID(outbox.getCrfVersionOid()).getCrfVersionId());

                    String result = rest.postForObject(pManageUrl, submission, String.class);
                    logger.debug("Notified Participate of CRF submission with a result of: " + result);

                    outbox.setStatus("COMPLETED");
                    outbox.setAttemptCount(outbox.getAttemptCount() + 1);
                    outbox.setUpdatedAt(new java.util.Date());
                    dao.saveOrUpdate(outbox);
                } catch (Exception e) {
                    logger.error("Unable to notify Participate. Will retry.", e);
                    outbox.setAttemptCount(outbox.getAttemptCount() + 1);
                    outbox.setLastErrorMessage(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 999)) : "Unknown error");
                    outbox.setUpdatedAt(new java.util.Date());
                    dao.saveOrUpdate(outbox);
                }
            }
        } catch (Exception e) {
            logger.error("Error running NotificationOutboxJob", e);
        }
    }
}
