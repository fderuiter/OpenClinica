cat << 'INNER_EOF' > patch.txt
    public static boolean isSCDItem(DisplayItemBean displayItemBean) {
        int scdId = 0;
        try {
            Object obj = displayItemBean.getScdData().getScdItemMetadataBean();
            if (obj != null) {
                java.lang.reflect.Method m = obj.getClass().getMethod("getScdItemFormMetadataId");
                scdId = (Integer) m.invoke(obj);
            }
        } catch (Exception e) {}
        return scdId>0 && scdId == displayItemBean.getMetadata().getId() ? true : false;
    }
INNER_EOF
sed -i -e '/public static boolean isSCDItem(DisplayItemBean displayItemBean) {/,/}/c\' -e "$(cat patch.txt)" domain/src/main/java/org/akaza/openclinica/bean/submit/SCDItemDisplayInfo.java
