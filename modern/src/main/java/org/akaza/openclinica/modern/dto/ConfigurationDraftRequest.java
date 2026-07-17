package org.akaza.openclinica.modern.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConfigurationDraftRequest {
    
    private String userName = "system";

    @NotNull
    private String draftData = "{}";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDraftData() {
        return draftData;
    }

    public void setDraftData(String draftData) {
        this.draftData = draftData;
    }
}
