package org.akaza.openclinica.domain.xform.dto;

import javax.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name="itext")
@XmlAccessorType(XmlAccessType.FIELD)
public class Itext {
    private List<Translation> translation;

    public List<Translation> getTranslation() {
        return translation;
    }

    public void setTranslation(List<Translation> translation) {
        this.translation = translation;
    }

}
