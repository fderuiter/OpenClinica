import os
import re

def process_file(file):
    with open(file, 'r') as f:
        c = f.read()

    # TriggerState enum mappings
    c = c.replace("Trigger.STATE_NORMAL", "org.quartz.Trigger.TriggerState.NORMAL.ordinal()")
    c = c.replace("Trigger.STATE_ERROR", "org.quartz.Trigger.TriggerState.ERROR.ordinal()")
    c = c.replace("Trigger.STATE_PAUSED", "org.quartz.Trigger.TriggerState.PAUSED.ordinal()")
    c = c.replace("Trigger.STATE_BLOCKED", "org.quartz.Trigger.TriggerState.BLOCKED.ordinal()")
    c = c.replace("Trigger.STATE_COMPLETE", "org.quartz.Trigger.TriggerState.COMPLETE.ordinal()")
    
    # getTriggerState(a, b) -> getTriggerState(TriggerKey.triggerKey(a, b)).ordinal() 
    # But ONLY if it has exactly two arguments and doesn't already have TriggerKey.
    # Actually it's easier to just do simple string replacements for the exact lines that fail.

    c = c.replace("getScheduler(request).getTriggerState(jobName, groupName)", "getScheduler(request).getTriggerState(org.quartz.TriggerKey.triggerKey(jobName, groupName)).ordinal()")
    c = c.replace("getScheduler(request).getJobDetail(jobName, groupName)", "getScheduler(request).getJobDetail(org.quartz.JobKey.jobKey(jobName, groupName))")
    c = c.replace("scheduler.deleteJob(theJobName, theJobGroupName)", "scheduler.deleteJob(org.quartz.JobKey.jobKey(theJobName, theJobGroupName))")
    c = c.replace("scheduler.getJobDetail(jobName, groupName)", "scheduler.getJobDetail(org.quartz.JobKey.jobKey(jobName, groupName))")
    c = c.replace("scheduler.getTrigger(triggerName, groupName)", "scheduler.getTrigger(org.quartz.TriggerKey.triggerKey(triggerName, groupName))")
    c = c.replace("scheduler.deleteJob(triggerName, TRIGGER_IMPORT_GROUP)", "scheduler.deleteJob(org.quartz.JobKey.jobKey(triggerName, TRIGGER_IMPORT_GROUP))")
    c = c.replace("scheduler.deleteJob(triggerName, xsltService.getTriggerGroupNameForExportJobs())", "scheduler.deleteJob(org.quartz.JobKey.jobKey(triggerName, xsltService.getTriggerGroupNameForExportJobs()))")
    c = c.replace("scheduler.deleteJob(triggerName, xsltService.TRIGGER_GROUP_NAME)", "scheduler.deleteJob(org.quartz.JobKey.jobKey(triggerName, xsltService.TRIGGER_GROUP_NAME))")
    c = c.replace("scheduler.deleteJob(triggerName, \"DEFAULT\")", "scheduler.deleteJob(org.quartz.JobKey.jobKey(triggerName, \"DEFAULT\"))")

    c = c.replace("scheduler.getTriggerNames(XsltTriggerService.TRIGGER_GROUP_NAME)", "scheduler.getTriggerKeys(org.quartz.impl.matchers.GroupMatcher.triggerGroupEquals(XsltTriggerService.TRIGGER_GROUP_NAME)).stream().map(org.quartz.TriggerKey::getName).toArray(String[]::new)")

    c = c.replace("SimpleTrigger trigger = new SimpleTrigger(", "org.quartz.impl.triggers.SimpleTriggerImpl trigger = new org.quartz.impl.triggers.SimpleTriggerImpl(")
    c = c.replace("SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW", "org.quartz.SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW")
    c = c.replace("SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT", "org.quartz.SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT")
    c = c.replace("private SimpleTrigger trigger;", "private org.quartz.impl.triggers.SimpleTriggerImpl trigger;")
    c = c.replace("SimpleTrigger trigger = null;", "org.quartz.impl.triggers.SimpleTriggerImpl trigger = null;")
    
    # Remove .setVolatility
    lines = c.split('\n')
    lines = [line for line in lines if '.setVolatility(' not in line]
    c = '\n'.join(lines)

    with open(file, 'w') as f:
        f.write(c)

for f in ["web/src/main/java/org/akaza/openclinica/control/admin/CreateJobExportServlet.java",
          "web/src/main/java/org/akaza/openclinica/control/core/SecureController.java",
          "web/src/main/java/org/akaza/openclinica/controller/ScheduledJobController.java",
          "web/src/main/java/org/akaza/openclinica/web/job/ImportSpringJob.java",
          "web/src/main/java/org/akaza/openclinica/web/job/ExampleSpringJob.java"]:
    process_file(f)

