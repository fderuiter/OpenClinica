package org.akaza.openclinica.bean.submit;

public class EditFormDTO {
    private String populatedInstance;
    private String crfVersionOid;
    private String studySubjectOid;

    public EditFormDTO() {
    }

    public EditFormDTO(String populatedInstance, String crfVersionOid, String studySubjectOid) {
        this.populatedInstance = populatedInstance;
        this.crfVersionOid = crfVersionOid;
        this.studySubjectOid = studySubjectOid;
    }

    public String getPopulatedInstance() {
        return populatedInstance;
    }

    public void setPopulatedInstance(String populatedInstance) {
        this.populatedInstance = populatedInstance;
    }

    public String getCrfVersionOid() {
        return crfVersionOid;
    }

    public void setCrfVersionOid(String crfVersionOid) {
        this.crfVersionOid = crfVersionOid;
    }

    public String getStudySubjectOid() {
        return studySubjectOid;
    }

    public void setStudySubjectOid(String studySubjectOid) {
        this.studySubjectOid = studySubjectOid;
    }
}
