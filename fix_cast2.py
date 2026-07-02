with open("core/src/main/java/org/akaza/openclinica/bean/extract/odm/MetaDataReportBean.java", "r") as f:
    content = f.read()
content = content.replace("rpic.populate(a.getRuleSetRules());", "rpic.populate((java.util.List)a.getRuleSetRules());")
with open("core/src/main/java/org/akaza/openclinica/bean/extract/odm/MetaDataReportBean.java", "w") as f:
    f.write(content)

with open("core/src/main/java/org/akaza/openclinica/logic/odmExport/MetadataUnit.java", "r") as f:
    content = f.read()
content = content.replace("ArrayList<RuleSetRuleBean>", "java.util.List")
with open("core/src/main/java/org/akaza/openclinica/logic/odmExport/MetadataUnit.java", "w") as f:
    f.write(content)
