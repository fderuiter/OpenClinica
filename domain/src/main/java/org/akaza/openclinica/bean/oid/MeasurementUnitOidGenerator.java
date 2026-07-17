package org.akaza.openclinica.bean.oid;

public class MeasurementUnitOidGenerator extends OidGenerator {

    @Override
    protected int getArgumentLength() {
        return 1;
    }

    @Override
    String createOid(String... keys) {
        String oid = this.truncateToXChars("MU_" + capitalize(stripNonAlphaNumeric(keys[0])),35);
        logger.info("OID : " + oid);
        return oid;
    }

    @Override
    protected boolean isPreserveUnderscores() {
        return true;
    }
}
