package org.akaza.openclinica.domain.xform.dto;

import javax.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name="body")
@XmlAccessorType(XmlAccessType.FIELD)
public class Body {
    private String cssClass = null;
    private String appearance = null;
    private List<Group> group;

    public List<Group> getGroup() {
        return group;
    }

    public void setGroup(List<Group> group) {
        this.group = group;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }
}