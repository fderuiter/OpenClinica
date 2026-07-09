package org.akaza.openclinica.bean.submit.crfdata;

import jakarta.xml.bind.annotation.*;


@XmlRootElement(name="UpsertOn")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpsertOnBean {

    @XmlAttribute(name="NotStarted")
    private boolean notStarted = true;
    @XmlAttribute(name="DataEntryStarted")
    private boolean dataEntryStarted = true;
    @XmlAttribute(name="DataEntryComplete")
    private boolean dataEntryComplete = true;

    public boolean isNotStarted() {
        return notStarted;
    }

    public void setNotStarted(boolean notStarted) {
        this.notStarted = notStarted;
    }

    public boolean isDataEntryStarted() {
        return dataEntryStarted;
    }

    public void setDataEntryStarted(boolean dataEntryStarted) {
        this.dataEntryStarted = dataEntryStarted;
    }

    public boolean isDataEntryComplete() {
        return dataEntryComplete;
    }

    public void setDataEntryComplete(boolean dataEntryComplete) {
        this.dataEntryComplete = dataEntryComplete;
    }

}
