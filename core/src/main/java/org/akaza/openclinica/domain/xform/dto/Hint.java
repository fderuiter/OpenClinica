package org.akaza.openclinica.domain.xform.dto;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="hint")
@XmlAccessorType(XmlAccessType.FIELD)
public class Hint {
    private String ref;
    private String hint;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
