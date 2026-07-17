package org.akaza.openclinica.bean.oid;

public class ItemGroupOidGenerator extends OidGenerator {

    @Override
    protected int getArgumentLength() {
        return 2;
    }

    @Override
    String createOid(String... keys) {
        String oid = "IG_";
        String crfName = keys[0];
        String itemGroupLabel = keys[1];

        logger.debug(crfName);
        logger.debug(itemGroupLabel);

        crfName = truncateToXChars(capitalize(stripNonAlphaNumeric(crfName)), 5);
        itemGroupLabel = truncateToXChars(capitalize(stripNonAlphaNumeric(itemGroupLabel)), 26);

        oid = oid + crfName + "_" + itemGroupLabel;

        logger.debug("OID : " + oid);
        return oid;
    }
}
