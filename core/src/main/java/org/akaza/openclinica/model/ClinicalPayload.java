package org.akaza.openclinica.model;

public class ClinicalPayload {
    private String subjectId;
    private String eventId;
    private String value;

    public ClinicalPayload() {}

    public ClinicalPayload(String subjectId, String eventId, String value) {
        this.subjectId = subjectId;
        this.eventId = eventId;
        this.value = value;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
