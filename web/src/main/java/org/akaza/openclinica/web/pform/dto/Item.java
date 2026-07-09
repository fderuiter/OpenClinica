package org.akaza.openclinica.web.pform.dto;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name="item")
@XmlAccessorType(XmlAccessType.FIELD)
public class Item {
	private Label label;
	private String value;
	
	public Label getLabel() {
		return label;
	}
	public void setLabel(Label label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
