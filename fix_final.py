import os
import re

def process_file(file):
    with open(file, 'r') as f:
        c = f.read()

    # XmlViewExporter
    if 'XmlViewExporter.java' in file:
        c = c.replace('super(view, coreContext, response, null);', 'super(view, coreContext, org.akaza.openclinica.web.filter.HttpServletResponseAdapter.adapt(response), null);')
        c = c.replace('super(view, coreContext, response, fileName);', 'super(view, coreContext, org.akaza.openclinica.web.filter.HttpServletResponseAdapter.adapt(response), fileName);')
        c = c.replace('getResponse().getOutputStream()', '((jakarta.servlet.http.HttpServletResponse)getResponse()).getOutputStream()') # Wait, getResponse() returns javax.servlet.http.HttpServletResponse? Then it has getOutputStream! But it complains `HttpServletResponse cannot be converted to ServletResponse`.
        c = c.replace('HttpServletResponse cannot be converted to ServletResponse', '')

    # AuditDatabaseServlet and ViewCRFServlet
    c = c.replace('TableFacadeFactory.createTableFacade("auditDatabase", request)', 'org.jmesa.facade.TableFacadeFactory.createTableFacade("auditDatabase", org.akaza.openclinica.web.filter.HttpServletRequestAdapter.adapt(request))')
    c = c.replace('TableFacadeFactory.createTableFacade("viewCRF", request)', 'org.jmesa.facade.TableFacadeFactory.createTableFacade("viewCRF", org.akaza.openclinica.web.filter.HttpServletRequestAdapter.adapt(request))')

    # ViewSingleJobServlet, PauseJobServlet, ViewImportJobServlet
    c = c.replace('scheduler.getTriggerNames(xsltTriggerSrvc.getTriggerGroupNameForExportJobs())', 'scheduler.getTriggerKeys(org.quartz.impl.matchers.GroupMatcher.triggerGroupEquals(xsltTriggerSrvc.getTriggerGroupNameForExportJobs())).stream().map(org.quartz.TriggerKey::getName).toArray(String[]::new)')
    c = c.replace('scheduler.getTriggerNames(XsltTriggerService.TRIGGER_GROUP_NAME)', 'scheduler.getTriggerKeys(org.quartz.impl.matchers.GroupMatcher.triggerGroupEquals(XsltTriggerService.TRIGGER_GROUP_NAME)).stream().map(org.quartz.TriggerKey::getName).toArray(String[]::new)')
    c = c.replace('scheduler.getTriggerNames(IMPORT_TRIGGER)', 'scheduler.getTriggerKeys(org.quartz.impl.matchers.GroupMatcher.triggerGroupEquals(IMPORT_TRIGGER)).stream().map(org.quartz.TriggerKey::getName).toArray(String[]::new)')
    c = c.replace('scheduler.getTriggerNames(xsltService.getTriggerGroupNameForExportJobs())', 'scheduler.getTriggerKeys(org.quartz.impl.matchers.GroupMatcher.triggerGroupEquals(xsltService.getTriggerGroupNameForExportJobs())).stream().map(org.quartz.TriggerKey::getName).toArray(String[]::new)')

    # PauseJobServlet
    c = c.replace('scheduler.resumeJob(jobName, XsltTriggerService.TRIGGER_GROUP_NAME)', 'scheduler.resumeJob(org.quartz.JobKey.jobKey(jobName, XsltTriggerService.TRIGGER_GROUP_NAME))')
    c = c.replace('scheduler.pauseJob(jobName, XsltTriggerService.TRIGGER_GROUP_NAME)', 'scheduler.pauseJob(org.quartz.JobKey.jobKey(jobName, XsltTriggerService.TRIGGER_GROUP_NAME))')
    c = c.replace('scheduler.resumeTrigger(triggerName, XsltTriggerService.TRIGGER_GROUP_NAME)', 'scheduler.resumeTrigger(org.quartz.TriggerKey.triggerKey(triggerName, XsltTriggerService.TRIGGER_GROUP_NAME))')
    c = c.replace('scheduler.pauseTrigger(triggerName, XsltTriggerService.TRIGGER_GROUP_NAME)', 'scheduler.pauseTrigger(org.quartz.TriggerKey.triggerKey(triggerName, XsltTriggerService.TRIGGER_GROUP_NAME))')

    # CreateJobImportServlet
    c = c.replace('scheduler.getTriggerNames(TRIGGER_IMPORT_GROUP)', 'scheduler.getTriggerKeys(org.quartz.impl.matchers.GroupMatcher.triggerGroupEquals(TRIGGER_IMPORT_GROUP)).stream().map(org.quartz.TriggerKey::getName).toArray(String[]::new)')
    c = c.replace('SimpleTrigger trigger = triggerService.generateImportTrigger(', 'org.quartz.impl.triggers.SimpleTriggerImpl trigger = (org.quartz.impl.triggers.SimpleTriggerImpl) triggerService.generateImportTrigger(')

    with open(file, 'w') as f:
        f.write(c)

for root, dirs, files in os.walk('web/src/main/java'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file))

