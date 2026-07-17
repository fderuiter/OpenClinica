package org.akaza.openclinica.bean.extract.odm;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonPostProcessor {
    void process(JsonNode json);
}
