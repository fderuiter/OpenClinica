package org.akaza.openclinica.domain.datamap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import java.util.Date;

@Entity
@Table(name = "notification_outbox")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "notification_outbox_id_seq") })
public class NotificationOutbox extends AbstractMutableDomainObject {

    private String studyOid;
    private Integer studyEventDefId;
    private Integer studyEventDefOrdinal;
    private String crfVersionOid;
    private String status;
    private Integer attemptCount;
    private String lastErrorMessage;
    private Date createdAt;
    private Date updatedAt;

    public NotificationOutbox() {}

    @Column(name = "study_oid")
    public String getStudyOid() { return studyOid; }
    public void setStudyOid(String studyOid) { this.studyOid = studyOid; }

    @Column(name = "study_event_def_id")
    public Integer getStudyEventDefId() { return studyEventDefId; }
    public void setStudyEventDefId(Integer studyEventDefId) { this.studyEventDefId = studyEventDefId; }

    @Column(name = "study_event_def_ordinal")
    public Integer getStudyEventDefOrdinal() { return studyEventDefOrdinal; }
    public void setStudyEventDefOrdinal(Integer studyEventDefOrdinal) { this.studyEventDefOrdinal = studyEventDefOrdinal; }

    @Column(name = "crf_version_oid")
    public String getCrfVersionOid() { return crfVersionOid; }
    public void setCrfVersionOid(String crfVersionOid) { this.crfVersionOid = crfVersionOid; }

    @Column(name = "status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Column(name = "attempt_count")
    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }

    @Column(name = "last_error_message")
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }

    @Column(name = "created_at")
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Column(name = "updated_at")
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
