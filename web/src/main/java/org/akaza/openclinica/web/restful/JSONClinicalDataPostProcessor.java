package org.akaza.openclinica.web.restful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import org.apache.commons.lang3.time.FastDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Runs a set of post-processing operations on JSON clinical data.
 *
 * @author Douglas Rodrigues (drodrigues@openclinica.com)
 */
public class JSONClinicalDataPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(JSONClinicalDataPostProcessor.class);

    private static final FastDateFormat DATE_INTERNAL_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");

    private static final FastDateFormat DATE_TIME_INTERNAL_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");

    private static final FastDateFormat DATE_TIME_AUDIT_LOG_INTERNAL_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");

    private static final String DATE_FORMAT_KEY = "date_format_string";

    private static final String DATE_TIME_FORMAT_KEY = "date_time_format_string";

    private static final Pattern DATE_PATTERN = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

    private static final Pattern DATE_TIME_PATTERN =
            Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]");

    /**
     * Matches the date & time format used to render audit log entries
     */
    private static final Pattern DATE_TIME_AUDIT_LOG_PATTERN =
            Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}");


    private final Locale locale;

    private final ResourceBundle formatResourceBundle;

    public JSONClinicalDataPostProcessor(Locale locale) {
        this.locale = locale;
        this.formatResourceBundle = ResourceBundleProvider.getFormatBundle(locale);
    }

    /**
     * Iterates over the elements of a JSON object.
     *
     * @param json JSON node to be processed
     */
    public void process(JsonNode json) {
        processJSONFields(json);
    }

    private void processJSONFields(JsonNode json) {
        if (json == null) return;
        if (json.isArray()) {
            processJSONArray((ArrayNode) json);
        } else if (json.isObject()) {
            processJSONObject((ObjectNode) json);
        }
    }

    private void processJSONArray(ArrayNode jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode elem = jsonArray.get(i);
            if (elem.isArray()) {
                processJSONArray((ArrayNode) elem);
            } else if (elem.isObject()) {
                processJSONObject((ObjectNode) elem);
            } else if (elem.isTextual()) {
                jsonArray.set(i, new TextNode(processString(elem.asText())));
            }
        }
    }

    private void processJSONObject(ObjectNode jsonObject) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonObject.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode elem = field.getValue();
            if (elem.isArray()) {
                processJSONArray((ArrayNode) elem);
            } else if (elem.isObject()) {
                processJSONObject((ObjectNode) elem);
            } else if (elem.isTextual()) {
                jsonObject.set(field.getKey(), new TextNode(processString(elem.asText())));
            }
        }
    }

    private String processString(String elem) {
        /*
        Tries to match the string with the following formats:
        2011-07-05
        2011-05-17 00:00:00.0
        2013-11-18T18:42:28 (Audit log date format)
        */

        boolean isShort = DATE_PATTERN.matcher(elem).matches();
        boolean isLong = DATE_TIME_PATTERN.matcher(elem).matches();
        boolean isAudit = DATE_TIME_AUDIT_LOG_PATTERN.matcher(elem).matches();

        if (isShort || isLong || isAudit) {
            try {
                Date date;
                org.apache.commons.lang3.time.FastDateFormat formatter;

                if (isShort) {
                    date = DATE_INTERNAL_FORMAT.parse(elem);
                    formatter = org.apache.commons.lang3.time.FastDateFormat.getInstance(formatResourceBundle.getString(DATE_FORMAT_KEY), locale);
                } else if (isLong) {
                    date = DATE_TIME_INTERNAL_FORMAT.parse(elem);
                    formatter = org.apache.commons.lang3.time.FastDateFormat.getInstance(formatResourceBundle.getString(DATE_TIME_FORMAT_KEY), locale);
                } else {
                    date = DATE_TIME_AUDIT_LOG_INTERNAL_FORMAT.parse(elem);
                    formatter = org.apache.commons.lang3.time.FastDateFormat.getInstance(formatResourceBundle.getString(DATE_TIME_FORMAT_KEY), locale);
                }

                return formatter.format(date);

            } catch (ParseException e) {
                LOG.warn("Could not parse date from ODM element '" + elem + "'", e);
            }
        }

        return elem;
    }

}
