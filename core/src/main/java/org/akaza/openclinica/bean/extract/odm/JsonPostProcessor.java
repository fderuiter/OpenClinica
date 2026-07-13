package org.akaza.openclinica.bean.extract.odm;

import org.json.JSONObject;

public interface JsonPostProcessor {
    void process(JSONObject json);
}
