package org.akaza.openclinica.controller;

import java.util.HashMap;
import java.util.Map;

public class FhirMappingConfig {
    private static FhirMappingConfig instance;
    private Map<String, String> fhirToOidMap = new HashMap<String, String>();

    private FhirMappingConfig() {
        // Defaults
        fhirToOidMap.put("Observation.code", "ItemOID");
        fhirToOidMap.put("Patient.identifier", "SubjectOID");
        fhirToOidMap.put("Encounter.identifier", "StudyEventOID");
    }

    public static synchronized FhirMappingConfig getInstance() {
        if (instance == null) {
            instance = new FhirMappingConfig();
        }
        return instance;
    }

    public Map<String, String> getFhirToOidMap() {
        return fhirToOidMap;
    }

    public void setFhirToOidMap(Map<String, String> map) {
        this.fhirToOidMap = map;
    }
}
