package org.akaza.openclinica.bean.login;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StudyStatusTransitionDTO {

    @NotNull(message = "Status cannot be null")
    @Size(min = 1, message = "Status cannot be blank")
    private String status;

    public StudyStatusTransitionDTO() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
