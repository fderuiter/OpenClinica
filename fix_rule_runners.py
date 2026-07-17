import re

path = '/app/core/src/main/java/org/akaza/openclinica/service/rule/RuleSetService.java'
with open(path, 'r') as f:
    code = f.read()

# Fix duplicates in CrfBulkRuleRunner
code = re.sub(r'new\s+CrfBulkRuleRunner\([^;]+;', 'new CrfBulkRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender, _cRFDAO, _cRFVersionDAO, _eventCRFDAO, _itemDataDAO, _itemFormMetadataDAO, _ruleActionDAO, _ruleSetDAO, _ruleSetRuleDAO, _sectionDAO, _studyDAO, _studyEventDAO, _studySubjectDAO);', code)

# Fix duplicates in RuleSetBulkRuleRunner
code = re.sub(r'new\s+RuleSetBulkRuleRunner\([^;]+;', 'new RuleSetBulkRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender, _cRFDAO, _cRFVersionDAO, _eventCRFDAO, _itemDataDAO, _itemFormMetadataDAO, _ruleActionDAO, _ruleSetDAO, _ruleSetRuleDAO, _sectionDAO, _studyDAO, _studyEventDAO, _studySubjectDAO);', code)

# Fix duplicates in DataEntryRuleRunner
code = re.sub(r'new\s+DataEntryRuleRunner\([^;]+;', 'new DataEntryRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender, eventCRF, _cRFDAO, _cRFVersionDAO, _eventCRFDAO, _itemDataDAO, _itemFormMetadataDAO, _ruleActionDAO, _ruleSetDAO, _ruleSetRuleDAO, _sectionDAO, _studyDAO, _studyEventDAO, _studySubjectDAO);', code)

# Fix duplicates in ImportDataRuleRunner
code = re.sub(r'new\s+ImportDataRuleRunner\([^;]+;', 'new ImportDataRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender, _cRFDAO, _cRFVersionDAO, _eventCRFDAO, _itemDataDAO, _itemFormMetadataDAO, _ruleActionDAO, _ruleSetDAO, _ruleSetRuleDAO, _sectionDAO, _studyDAO, _studyEventDAO, _studySubjectDAO);', code)

# Fix duplicates in BeanPropertyRuleRunner
code = re.sub(r'new\s+BeanPropertyRuleRunner\([^;]+;', 'new BeanPropertyRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender, _studyDAO, _studyEventDAO, _cRFDAO, _cRFVersionDAO, _eventCRFDAO, _itemDataDAO, _itemFormMetadataDAO, _ruleActionDAO, _ruleSetDAO, _ruleSetRuleDAO, _sectionDAO, _studySubjectDAO);', code)


with open(path, 'w') as f:
    f.write(code)

