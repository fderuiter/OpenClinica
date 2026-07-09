package org.akaza.openclinica.domain.xform.dto;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name="translation")
@XmlAccessorType(XmlAccessType.FIELD)
public class Translation {
    private String defaultLang;
    private String lang;
    private List<Text> text;

    public String getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(String defaultLang) {
        this.defaultLang = defaultLang;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public List<Text> getText() {
        return text;
    }

    public void setText(List<Text> text) {
        this.text = text;
    }

}
