package org.akaza.openclinica.bean.oid;

public class CrfVersionOidGenerator extends OidGenerator {

    @Override
    protected int getArgumentLength() {
        return 2;
    }

    @Override
    String createOid(String... keys) {
        logger.debug("In Create OID");
        String oid;
        String crfOid = keys[0];
        String crfVersion = keys[1];

        crfVersion = truncateToXChars(capitalize(stripNonAlphaNumeric(crfVersion)), 10);

        logger.debug(crfOid);
        logger.info(crfVersion);
        oid = crfOid + "_" + crfVersion;

        return oid;
    }
}
