import os
import re

def process_file(file):
    with open(file, 'r') as f:
        c = f.read()

    c = c.replace("org.springframework.scheduling.quartz.JobDetailBean", "org.quartz.impl.JobDetailImpl")
    c = c.replace("JobDetailBean jobDetailBean", "JobDetailImpl jobDetailBean")
    c = c.replace("new JobDetailBean(", "new JobDetailImpl(")
    
    # context.getJobDetail().getFullName() -> context.getJobDetail().getKey().toString()
    c = c.replace(".getJobDetail().getFullName()", ".getJobDetail().getKey().toString()")
    c = c.replace(".getJobDetail().getName()", ".getJobDetail().getKey().getName()")
    c = c.replace(".getJobDetail().getGroup()", ".getJobDetail().getKey().getGroup()")
    
    c = c.replace(".getTrigger().getFullName()", ".getTrigger().getKey().toString()")
    c = c.replace(".getTrigger().getName()", ".getTrigger().getKey().getName()")
    c = c.replace(".getTrigger().getGroup()", ".getTrigger().getKey().getGroup()")
    
    c = c.replace("JobDetail jobDetail = context.getJobDetail();", "org.quartz.impl.JobDetailImpl jobDetail = (org.quartz.impl.JobDetailImpl) context.getJobDetail();")
    
    # CoreSecureController.java
    c = c.replace("getScheduler(request).getTriggerState(jobName, groupName)", "getScheduler(request).getTriggerState(org.quartz.TriggerKey.triggerKey(jobName, groupName)).ordinal()")
    c = c.replace("getScheduler(request).getJobDetail(jobName, groupName)", "getScheduler(request).getJobDetail(org.quartz.JobKey.jobKey(jobName, groupName))")
    
    with open(file, 'w') as f:
        f.write(c)

for f in ["web/src/main/java/org/akaza/openclinica/control/admin/CreateJobExportServlet.java",
          "web/src/main/java/org/akaza/openclinica/control/core/SecureController.java",
          "web/src/main/java/org/akaza/openclinica/control/core/CoreSecureController.java",
          "web/src/main/java/org/akaza/openclinica/controller/ScheduledJobController.java",
          "web/src/main/java/org/akaza/openclinica/web/job/ImportSpringJob.java",
          "web/src/main/java/org/akaza/openclinica/web/job/ExampleSpringJob.java"]:
    process_file(f)

