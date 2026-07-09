import re

file = "web/src/main/java/org/akaza/openclinica/control/admin/CreateJobExportServlet.java"
with open(file, "r") as f:
    c = f.read()

c = c.replace(
    "scheduler.getTriggerNames(XsltTriggerService.TRIGGER_GROUP_NAME)",
    "scheduler.getTriggerKeys(org.quartz.impl.matchers.GroupMatcher.triggerGroupEquals(XsltTriggerService.TRIGGER_GROUP_NAME)).stream().map(org.quartz.TriggerKey::getName).toArray(String[]::new)"
)

# And let's check for any extra `)))`
c = c.replace(")))", "))")

with open(file, "w") as f:
    f.write(c)

