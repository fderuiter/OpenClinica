import re

file = "core/src/main/java/org/akaza/openclinica/job/JobTriggerListener.java"
with open(file, "r") as f:
    c = f.read()

c = c.replace("public void triggerComplete(Trigger trigger, JobExecutionContext context, int triggerInstructionCode)", "public void triggerComplete(Trigger trigger, JobExecutionContext context, org.quartz.Trigger.CompletedExecutionInstruction triggerInstructionCode)")
c = c.replace("super.triggerComplete(trigger, context, triggerInstructionCode);", "//super.triggerComplete(trigger, context, triggerInstructionCode);")
c = c.replace("trigger.getName()", "trigger.getKey().getName()")

with open(file, "w") as f:
    f.write(c)

