package org.akaza.openclinica.modern.model;

import java.util.Date;

public class ConfigurationDraft {
    private String id;
    private String userName;
    private String draftType;
    private String draftData;
    private Date createdAt;
    private Date expiresAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getDraftType() { return draftType; }
    public void setDraftType(String draftType) { this.draftType = draftType; }
    
    public String getDraftData() { return draftData; }
    public void setDraftData(String draftData) { this.draftData = draftData; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
}
