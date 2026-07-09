package org.akaza.openclinica.domain.xform.dto;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name="label")
@XmlAccessorType(XmlAccessType.FIELD)
public class Label {
    private String ref;
    private String label;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
