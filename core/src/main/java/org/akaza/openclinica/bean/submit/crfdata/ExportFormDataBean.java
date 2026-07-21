/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2008 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.submit.crfdata;


/**
 * OpenClinica form attributes have been included in addition to ODM FormData
 * attributes
 * 
 * @author ywang (Nov, 2008)
 */

public class ExportFormDataBean extends FormDataBean {
    private String crfVersion;
    private String interviewerName;
    private String interviewDate;
    private String status;
    private String locked;
    private String signed;
    private String stopped;
    private String signerName;
    private String signatureDate;
    private String signatureReason;

    public ExportFormDataBean() {
        super();
    }

    public void setCrfVersion(String crfVersion) {
        this.crfVersion = crfVersion;
    }

    public String getCrfVersion() {
        return this.crfVersion;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getInterviewerName() {
        return this.interviewerName;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewDate() {
        return this.interviewDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public String getSigned() {
        return signed;
    }

    public void setSigned(String signed) {
        this.signed = signed;
    }

    public String getStopped() {
        return stopped;
    }

    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
    public String getSignatureDate() { return signatureDate; }
    public void setSignatureDate(String signatureDate) { this.signatureDate = signatureDate; }
    public String getSignatureReason() { return signatureReason; }
    public void setSignatureReason(String signatureReason) { this.signatureReason = signatureReason; }

    public void setStopped(String stopped) {
        this.stopped = stopped;
    }
}
