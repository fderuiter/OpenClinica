package org.akaza.openclinica.domain.rule;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "rule_evaluation_log")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "rule_evaluation_log_id_seq") })
public class RuleEvaluationLogBean extends AbstractMutableDomainObject {

    private Integer ruleSetId;
    private Integer ruleSetVersion;
    private String ruleOid;
    private String inputDataSnapshot;
    private String result;
    private String outcomeDetails;
    private Date createdAt;

    public RuleEvaluationLogBean() {
    }

    @Column(name = "rule_set_id")
    public Integer getRuleSetId() {
        return ruleSetId;
    }

    public void setRuleSetId(Integer ruleSetId) {
        this.ruleSetId = ruleSetId;
    }

    @Column(name = "rule_set_version")
    public Integer getRuleSetVersion() {
        return ruleSetVersion;
    }

    public void setRuleSetVersion(Integer ruleSetVersion) {
        this.ruleSetVersion = ruleSetVersion;
    }

    @Column(name = "rule_oid")
    public String getRuleOid() {
        return ruleOid;
    }

    public void setRuleOid(String ruleOid) {
        this.ruleOid = ruleOid;
    }

    @Column(name = "input_data_snapshot")
    public String getInputDataSnapshot() {
        return inputDataSnapshot;
    }

    public void setInputDataSnapshot(String inputDataSnapshot) {
        this.inputDataSnapshot = inputDataSnapshot;
    }

    @Column(name = "result")
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Column(name = "outcome_details")
    public String getOutcomeDetails() {
        return outcomeDetails;
    }

    public void setOutcomeDetails(String outcomeDetails) {
        this.outcomeDetails = outcomeDetails;
    }

    @Column(name = "created_at", updatable = false)
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
