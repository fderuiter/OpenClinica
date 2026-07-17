package org.akaza.openclinica.bean.oid;

public class StudyEventDefinitionOidGenerator extends OidGenerator {

    @Override
    protected int getArgumentLength() {
        return 1;
    }

    @Override
    String createOid(String... keys) {
        String oid = "SE_";
        String key = keys[0];
        oid = oid + truncateToXChars(capitalize(stripNonAlphaNumeric(key)), 28);
        return oid;
    }
}
