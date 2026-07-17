package org.akaza.openclinica.bean.oid;

public class StudyOidGenerator extends OidGenerator {

    @Override
    public int getArgumentLength() {
        return 1;
    }

    @Override
    String createOid(String... keys) {
        String oid = "S_";
        String uniqueProtocolID = keys[0];
        uniqueProtocolID = truncateTo8Chars(capitalize(stripNonAlphaNumeric(uniqueProtocolID)));
        oid = oid + uniqueProtocolID;
        return oid;
    }
}
