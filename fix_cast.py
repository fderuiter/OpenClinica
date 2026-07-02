import re
with open("core/src/main/java/org/akaza/openclinica/bean/extract/odm/MetaDataReportBean.java", "r") as f:
    content = f.read()
content = content.replace("List<RuleSetRuleBean> rbs = getMetadataVersion().getRuleSetRules();", "List rbs = getMetadataVersion().getRuleSetRules();")
with open("core/src/main/java/org/akaza/openclinica/bean/extract/odm/MetaDataReportBean.java", "w") as f:
    f.write(content)

with open("core/src/main/java/org/akaza/openclinica/logic/odmExport/MetadataUnit.java", "r") as f:
    content = f.read()
content = content.replace("this.metaDataVersionBean.setRuleSetRules(getRuleSetRuleBeans());", "this.metaDataVersionBean.setRuleSetRules((java.util.List)getRuleSetRuleBeans());")
with open("core/src/main/java/org/akaza/openclinica/logic/odmExport/MetadataUnit.java", "w") as f:
    f.write(content)
