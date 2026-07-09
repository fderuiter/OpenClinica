import os
import re

def process_file(file):
    with open(file, 'r') as f:
        c = f.read()

    # .getTriggerState(name, group) -> .getTriggerState(org.quartz.TriggerKey.triggerKey(name, group))
    c = re.sub(r'\.getTriggerState\(([^,]+),\s*([^)]+)\)', r'.getTriggerState(org.quartz.TriggerKey.triggerKey(\1, \2))', c)

    # .getJobDetail(name, group) -> .getJobDetail(org.quartz.JobKey.jobKey(name, group))
    c = re.sub(r'\.getJobDetail\(([^,]+),\s*([^)]+)\)', r'.getJobDetail(org.quartz.JobKey.jobKey(\1, \2))', c)

    # .getTrigger(name, group) -> .getTrigger(org.quartz.TriggerKey.triggerKey(name, group))
    c = re.sub(r'\.getTrigger\(([^,]+),\s*([^)]+)\)', r'.getTrigger(org.quartz.TriggerKey.triggerKey(\1, \2))', c)

    # .deleteJob(name, group) -> .deleteJob(org.quartz.JobKey.jobKey(name, group))
    c = re.sub(r'\.deleteJob\(([^,]+),\s*([^)]+)\)', r'.deleteJob(org.quartz.JobKey.jobKey(\1, \2))', c)

    # Also fix OCTableFacadeImpl incompatibility
    c = c.replace("org.akaza.openclinica.web.filter.HttpServletRequestAdapter.adapt(request)", "request")
    c = c.replace("new XmlViewExporter(view, cc, request, org.akaza.openclinica.web.filter.HttpServletResponseAdapter.adapt(response))", 
                  "new XmlViewExporter(view, cc, org.akaza.openclinica.web.filter.HttpServletRequestAdapter.adapt(request), org.akaza.openclinica.web.filter.HttpServletResponseAdapter.adapt(response))")
                  
    with open(file, 'w') as f:
        f.write(c)

for root, dirs, files in os.walk('web/src/main/java'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file))

