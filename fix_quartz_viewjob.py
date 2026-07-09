import re

file = "web/src/main/java/org/akaza/openclinica/control/admin/ViewJobServlet.java"
with open(file, "r") as f:
    c = f.read()

c = c.replace("org.quartz.TriggerKey.triggerKey(org.quartz.TriggerKey.triggerKey(", "org.quartz.TriggerKey.triggerKey(")
c = c.replace("XsltTriggerService.TRIGGER_GROUP_NAME)))", "XsltTriggerService.TRIGGER_GROUP_NAME))")
c = c.replace("IMPORT_TRIGGER)))", "IMPORT_TRIGGER))")
c = c.replace(".ordinal()", "")

with open(file, "w") as f:
    f.write(c)
