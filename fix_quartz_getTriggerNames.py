import re

file = "web/src/main/java/org/akaza/openclinica/control/admin/ViewJobServlet.java"
with open(file, "r") as f:
    c = f.read()

c = c.replace(
    "String[] triggerNames = scheduler.getTriggerNames(xsltTriggerSrvc.getTriggerGroupNameForExportJobs());",
    "java.util.Set<org.quartz.TriggerKey> keys = scheduler.getTriggerKeys(org.quartz.impl.matchers.GroupMatcher.triggerGroupEquals(xsltTriggerSrvc.getTriggerGroupNameForExportJobs()));\n" +
    "        String[] triggerNames = new String[keys.size()];\n" +
    "        int _idx = 0; for(org.quartz.TriggerKey k : keys) { triggerNames[_idx++] = k.getName(); }\n"
)

# And fix line 83: Trigger trigger = scheduler.getTrigger(org.quartz.TriggerKey.triggerKey(triggerName, xsltTriggerSrvc.getTriggerGroupNameForExportJobs())));
# Wait, my regex earlier left an extra parenthesis! `)))`
c = c.replace("org.quartz.TriggerKey.triggerKey(triggerName, xsltTriggerSrvc.getTriggerGroupNameForExportJobs())))",
              "org.quartz.TriggerKey.triggerKey(triggerName, xsltTriggerSrvc.getTriggerGroupNameForExportJobs()))")

with open(file, "w") as f:
    f.write(c)

