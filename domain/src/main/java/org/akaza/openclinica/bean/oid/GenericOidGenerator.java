package org.akaza.openclinica.bean.oid;

import java.io.Serializable;

public class GenericOidGenerator extends OidGenerator implements Serializable {

    @Override
    protected int getArgumentLength() {
        return 1;
    }

    @Override
    String createOid(String... keys) {
        String oid;
        String key = keys[0];

        oid = truncateTo4Chars(capitalize(stripNonAlphaNumeric(key)));

        logger.debug("OID : " + oid);
        return oid;
    }
}
