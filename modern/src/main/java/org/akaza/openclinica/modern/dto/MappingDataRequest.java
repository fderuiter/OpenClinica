package org.akaza.openclinica.modern.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MappingDataRequest {

    @NotBlank(message = "subject_id is required")
    @JsonProperty("subject_id")
    private String subjectId;

    @NotBlank(message = "event_id is required")
    @JsonProperty("event_id")
    private String eventId;

    @NotBlank(message = "item_value is required")
    @JsonProperty("item_value")
    private String itemValue;

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

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }
}
