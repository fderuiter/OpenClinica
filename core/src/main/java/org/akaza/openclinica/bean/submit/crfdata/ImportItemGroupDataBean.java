package org.akaza.openclinica.bean.submit.crfdata;

import jakarta.xml.bind.annotation.*;


import java.util.ArrayList;

@XmlRootElement(name="ItemGroupData")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportItemGroupDataBean {
    @XmlElement(name="ItemData")
    private ArrayList<ImportItemDataBean> itemData;
    @XmlAttribute(name="ItemGroupOID")
    private String itemGroupOID;
    @XmlAttribute(name="ItemGroupRepeatKey")
    private String itemGroupRepeatKey;
    
    public ImportItemGroupDataBean() {
        itemData = new ArrayList<ImportItemDataBean>();
    }

    public String getItemGroupRepeatKey() {
        return itemGroupRepeatKey;
    }

    public void setItemGroupRepeatKey(String itemGroupRepeatKey) {
        this.itemGroupRepeatKey = itemGroupRepeatKey;
    }

    public String getItemGroupOID() {
        return itemGroupOID;
    }

    public void setItemGroupOID(String itemGroupOID) {
        this.itemGroupOID = itemGroupOID;
    }

    public ArrayList<ImportItemDataBean> getItemData() {
        return itemData;
    }

    public void setItemData(ArrayList<ImportItemDataBean> itemData) {
        this.itemData = itemData;
    }
}
