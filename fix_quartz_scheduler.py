import os
import re

def process_file(file):
    with open(file, 'r') as f:
        c = f.read()

    # scheduler.getTriggerState(triggerName, groupName) -> scheduler.getTriggerState(org.quartz.TriggerKey.triggerKey(triggerName, groupName))
    c = re.sub(r'scheduler\.getTriggerState\(([^,]+),\s*([^)]+)\)', r'scheduler.getTriggerState(org.quartz.TriggerKey.triggerKey(\1, \2))', c)

    # scheduler.getJobDetail(jobName, groupName) -> scheduler.getJobDetail(org.quartz.JobKey.jobKey(jobName, groupName))
    c = re.sub(r'scheduler\.getJobDetail\(([^,]+),\s*([^)]+)\)', r'scheduler.getJobDetail(org.quartz.JobKey.jobKey(\1, \2))', c)

    # scheduler.getTrigger(triggerName, groupName) -> scheduler.getTrigger(org.quartz.TriggerKey.triggerKey(triggerName, groupName))
    c = re.sub(r'scheduler\.getTrigger\(([^,]+),\s*([^)]+)\)', r'scheduler.getTrigger(org.quartz.TriggerKey.triggerKey(\1, \2))', c)

    # scheduler.deleteJob(jobName, groupName) -> scheduler.deleteJob(org.quartz.JobKey.jobKey(jobName, groupName))
    c = re.sub(r'scheduler\.deleteJob\(([^,]+),\s*([^)]+)\)', r'scheduler.deleteJob(org.quartz.JobKey.jobKey(\1, \2))', c)

    # scheduler.getTriggerGroupNames() -> scheduler.getTriggerGroupNames() works, but getting trigger keys
    # wait, getTriggerGroupNames is deprecated/returns List<String> or something. Let's see if we hit it.

    # trigger.VOLATILITY -> removed? Let's check trigger.VOLATILITY.
    # trigger.STATE_NORMAL -> org.quartz.Trigger.TriggerState.NORMAL.ordinal()?
    # Actually Quartz 2 uses Trigger.TriggerState enum. Trigger.TriggerState.NORMAL.
    c = c.replace("Trigger.STATE_NORMAL", "org.quartz.Trigger.TriggerState.NORMAL.ordinal()")
    c = c.replace("Trigger.STATE_ERROR", "org.quartz.Trigger.TriggerState.ERROR.ordinal()")
    c = c.replace("Trigger.STATE_PAUSED", "org.quartz.Trigger.TriggerState.PAUSED.ordinal()")
    c = c.replace("Trigger.STATE_BLOCKED", "org.quartz.Trigger.TriggerState.BLOCKED.ordinal()")
    c = c.replace("Trigger.STATE_COMPLETE", "org.quartz.Trigger.TriggerState.COMPLETE.ordinal()")

    with open(file, 'w') as f:
        f.write(c)

for root, dirs, files in os.walk('web/src/main/java'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file))

