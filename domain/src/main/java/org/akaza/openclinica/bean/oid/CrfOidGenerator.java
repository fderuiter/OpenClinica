package org.akaza.openclinica.bean.oid;

public class CrfOidGenerator extends OidGenerator {

    @Override
    protected int getArgumentLength() {
        return 1;
    }

    @Override
    String createOid(String... keys) {
        String oid = "F_";
        String crfName = keys[0];

        crfName = truncateToXChars(capitalize(stripNonAlphaNumeric(crfName)), 12);
        oid = oid + crfName;

        return oid;
    }
}
