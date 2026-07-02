import re
with open("domain/src/main/java/org/akaza/openclinica/bean/submit/SCDItemDisplayInfo.java", "r") as f:
    content = f.read()

replacement = """    public static boolean isSCDItem(DisplayItemBean displayItemBean) {
        int scdId = 0;
        try {
            Object scdData = displayItemBean.getScdData();
            if (scdData != null) {
                java.lang.reflect.Method m1 = scdData.getClass().getMethod("getScdItemMetadataBean");
                Object obj = m1.invoke(scdData);
                if (obj != null) {
                    java.lang.reflect.Method m2 = obj.getClass().getMethod("getScdItemFormMetadataId");
                    scdId = (Integer) m2.invoke(obj);
                }
            }
        } catch (Exception e) {}
        return scdId>0 && scdId == displayItemBean.getMetadata().getId();
    }"""

content = re.sub(r'public static boolean isSCDItem\(DisplayItemBean displayItemBean\) \{.*?\n    \}', replacement, content, flags=re.DOTALL)

with open("domain/src/main/java/org/akaza/openclinica/bean/submit/SCDItemDisplayInfo.java", "w") as f:
    f.write(content)
