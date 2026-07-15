/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.job;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class JobTriggerListener extends TriggerListenerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(JobTriggerListener.class);

    @Override
    public String getName() {
        return "JobTriggerListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        super.triggerFired(trigger, context);
        logTriggerInfo(trigger, "Trigger {} fired");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        boolean result =  super.vetoJobExecution(trigger, context);
        
        try {
            org.quartz.JobDataMap jobDataMap = context.getMergedJobDataMap();
            if (jobDataMap != null && jobDataMap.containsKey("user_id")) {
                Object userIdObj = jobDataMap.get("user_id");
                if (userIdObj != null) {
                    int userId = -1;
                    if (userIdObj instanceof Integer) {
                        userId = (Integer) userIdObj;
                    } else if (userIdObj instanceof String) {
                        try {
                            userId = Integer.parseInt((String) userIdObj);
                        } catch (NumberFormatException e) {
                            LOG.warn("Invalid user_id format in JobDataMap: {}", userIdObj);
                        }
                    }
                    if (userId > 0) {
                        org.springframework.context.ApplicationContext appCtx = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext();
                        if (appCtx != null) {
                            org.akaza.openclinica.dao.login.UserAccountDAO userAccountDao = (org.akaza.openclinica.dao.login.UserAccountDAO) appCtx.getBean("userAccountDao");
                            if (userAccountDao != null) {
                                org.akaza.openclinica.bean.login.UserAccountBean userAccount = (org.akaza.openclinica.bean.login.UserAccountBean) userAccountDao.findByPK(userId);
                                
                                if (userAccount != null && userAccount.getId() > 0) {
                                    boolean isEnabled = userAccount.getEnabled() != null && userAccount.getEnabled();
                                    boolean isNonLocked = userAccount.getAccountNonLocked() != null && userAccount.getAccountNonLocked();
                                    boolean isAvailable = userAccount.getStatus() != null && userAccount.getStatus().isAvailable();
                                    
                                    if (!isAvailable || !isEnabled || !isNonLocked) {
                                        LOG.warn("Security Warning: Background job vetoed. Job ID: {}, User ID: {}. Account is disabled, locked, or deleted.", context.getJobDetail().getKey().getName(), userId);
                                        result = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error during security interceptor check in JobTriggerListener", e);
        }

        logTriggerInfo(trigger, "Trigger {} vetoed");
        return result;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        super.triggerMisfired(trigger);
        logTriggerInfo(trigger, "Trigger {} misfired");
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, org.quartz.Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        //super.triggerComplete(trigger, context, triggerInstructionCode);
        logTriggerInfo(trigger, "Trigger {} complete");
    }

    private void logTriggerInfo(Trigger trigger, String message) {
        LOG.debug(message, trigger.getKey().getName());
    }



}
