package org.akaza.openclinica.control.extract;

import java.util.UUID;

public class ExportTask {
    private String id;
    private String status; // "Processing", "Completed", "Failed"
    private String downloadUrl;
    private String errorMessage;

    public ExportTask() {
        this.id = UUID.randomUUID().toString();
        this.status = "Processing";
    }

    public String getId() { return id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
