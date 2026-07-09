package org.akaza.openclinica.bean.submit.crfdata;

import jakarta.xml.bind.annotation.*;


import java.util.ArrayList;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;


@XmlRootElement(name="StudyEventData")
@XmlAccessorType(XmlAccessType.FIELD)
public class StudyEventDataBean {
    @XmlElement(name="FormData")
    private ArrayList<FormDataBean> formData;
    @XmlAttribute(name="StudyEventOID")
    private String studyEventOID;
    @XmlAttribute(name="StudyEventRepeatKey")
    private String studyEventRepeatKey;
    private AuditLogsBean auditLogs;
    private DiscrepancyNotesBean discrepancyNotes;
    private Object studyEventDefinition;

    public StudyEventDataBean() {
        formData = new ArrayList<FormDataBean>();
        auditLogs = new AuditLogsBean();
        discrepancyNotes = new DiscrepancyNotesBean();
    }

    public String getStudyEventRepeatKey() {
        return studyEventRepeatKey;
    }

    public void setStudyEventRepeatKey(String studyEventRepeatKey) {
        this.studyEventRepeatKey = studyEventRepeatKey;
    }

    public String getStudyEventOID() {
        return studyEventOID;
    }

    public void setStudyEventOID(String studyEventOID) {
        this.studyEventOID = studyEventOID;
    }

    public ArrayList<FormDataBean> getFormData() {
        return formData;
    }

    public void setFormData(ArrayList<FormDataBean> formData) {
        this.formData = formData;
    }

    public AuditLogsBean getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(AuditLogsBean auditLogs) {
        this.auditLogs = auditLogs;
    }

    public DiscrepancyNotesBean getDiscrepancyNotes() {
        return discrepancyNotes;
    }

    public void setDiscrepancyNotes(DiscrepancyNotesBean discrepancyNotes) {
        this.discrepancyNotes = discrepancyNotes;
    }

	public Object getStudyEventDefinition() {
	return studyEventDefinition;
}

    public void setStudyEventDefinition(Object studyEventDefinition) {
	this.studyEventDefinition = studyEventDefinition;
}


}
