package org.akaza.openclinica.bean.odmbeans;

public class SignatureDefBean {
    private String oid;
    private String methodology;
    private String meaning;
    private String legalReason;

    public String getOid() { return oid; }
    public void setOid(String oid) { this.oid = oid; }
    
    public String getMethodology() { return methodology; }
    public void setMethodology(String methodology) { this.methodology = methodology; }
    
    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    
    public String getLegalReason() { return legalReason; }
    public void setLegalReason(String legalReason) { this.legalReason = legalReason; }
}
