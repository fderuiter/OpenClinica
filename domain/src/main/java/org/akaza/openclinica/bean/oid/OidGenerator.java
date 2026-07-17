package org.akaza.openclinica.bean.oid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class OidGenerator {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((logger == null) ? 0 : logger.hashCode());
        result = prime * result + oidLength;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OidGenerator other = (OidGenerator) obj;
        if (logger == null) {
            if (other.logger != null)
                return false;
        } else if (!logger.equals(other.logger))
            return false;
        if (oidLength != other.oidLength)
            return false;
        return true;
    }

    private final int oidLength = 40;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public final String generateOid(String... keys) throws Exception {
        verifyArgumentLength(keys);
        String oid = createOid(keys);
        oid = applyFallbackIfNecessary(oid);
        validate(oid);
        return oid;
    }

    public final String generateOidNoValidation(String... keys) throws Exception {
        verifyArgumentLength(keys);
        String oid = createOid(keys);
        oid = applyFallbackIfNecessary(oid);
        return oid;
    }

    private String applyFallbackIfNecessary(String oid) {
        if (oid == null || oid.isEmpty() || oid.endsWith("_")) {
            String prefix = oid == null ? "" : oid.replaceAll("_+$", "");
            return randomizeOid(prefix);
        }
        return oid;
    }

    public String randomizeOid(String input) {
        if (input == null || input.length() == 0)
            input = "";
        if (!input.endsWith("_"))
            input = input + "_";
        input = input + new Double((Math.random() * 10000)).intValue();
        return input;
    }

    protected abstract int getArgumentLength();

    public void verifyArgumentLength(String... keys) throws Exception {
        if (keys == null || keys.length != getArgumentLength()) {
            throw new Exception("Invalid number of arguments");
        }
    }

    abstract String createOid(String... keys);

    protected boolean isPreserveUnderscores() {
        return false;
    }

    String stripNonAlphaNumeric(String input) {
        if (input == null) return "";
        if (isPreserveUnderscores()) {
            return input.trim().replaceAll("[^a-zA-Z_0-9]", "");
        } else {
            return input.trim().replaceAll("[^a-zA-Z0-9]", "");
        }
    }

    String capitalize(String input) {
        return input.toUpperCase();
    }

    String truncateToXChars(String input, int x) {
        return input.length() > x ? input.substring(0, x) : input;
    }

    String truncateTo4Chars(String input) {
        return truncateToXChars(input, 4);
    }

    String truncateTo8Chars(String input) {
        return truncateToXChars(input, 8);
    }

    public boolean validate(String oid) throws Exception {
        Pattern pattern = Pattern.compile("^[A-Z_0-9]+$");
        Matcher matcher = pattern.matcher(oid);
        boolean isValid = matcher.matches();
        if (!isValid || oid.length() > oidLength || oid.length() <= 0) {
            throw new Exception();
        }
        return isValid;
    }

}
