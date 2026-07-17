package org.akaza.openclinica.bean.oid;

public class ItemOidGenerator extends OidGenerator {

    @Override
    protected int getArgumentLength() {
        return 2;
    }

    @Override
    String createOid(String... keys) {
        String oid = "I_";
        String crfName = keys[0];
        String itemLabel = keys[1];

        logger.debug(crfName);
        logger.debug(itemLabel);

        crfName = truncateToXChars(capitalize(stripNonAlphaNumeric(crfName)), 5);
        itemLabel = truncateToXChars(capitalize(stripNonAlphaNumeric(itemLabel)), 27);

        oid = oid + crfName + "_" + itemLabel;

        logger.debug("OID : " + oid);
        return oid;
    }
}
