package org.akaza.openclinica.modern.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "configuration_drafts")
public class ConfigurationDraft {
    @Id
    private String id;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "draft_type")
    private String draftType;
    
    @Column(name = "draft_data")
    private String draftData;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
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
