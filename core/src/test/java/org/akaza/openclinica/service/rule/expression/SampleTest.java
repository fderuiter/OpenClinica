package org.akaza.openclinica.service.rule.expression;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SampleTest {

    @Test
    public void testExpressionSyntaxChecker() {
        ExpressionService service = new ExpressionService((javax.sql.DataSource) null);

        // Valid syntax: up to 4 components, alphanumeric/underscores, optional ordinals
        assertTrue("Valid full expression", service.checkSyntax("SE_OID123.CRF_OID.IG_OID.ITEM_OID"));
        assertTrue("Valid with ordinals", service.checkSyntax("SE_OID[1].CRF_OID.IG_OID[ALL].ITEM_OID"));
        assertTrue("Valid partial expression", service.checkSyntax("IG_OID.ITEM_OID"));
        assertTrue("Valid single item", service.checkSyntax("ITEM_OID"));

        // Invalid syntax: leading/trailing separators or invalid characters
        assertFalse("Leading separator is invalid", service.checkSyntax(".IG_OID.ITEM_OID"));
        assertFalse("Trailing separator is invalid", service.checkSyntax("IG_OID.ITEM_OID."));
        assertFalse("Invalid characters", service.checkSyntax("IG-OID.ITEM-OID"));
    }
}
