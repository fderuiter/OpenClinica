package org.akaza.openclinica.domain.xform.dto;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="value")
@XmlAccessorType(XmlAccessType.FIELD)
public class Value {
    private String form;
    private String value;
    private String ref;

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

}
