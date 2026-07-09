package org.akaza.openclinica.dao.core.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "queries")
@XmlAccessorType(XmlAccessType.FIELD)
public class Queries {

    @XmlElement(name = "query")
    private List<Query> queryList;

    public List<Query> getQueryList() {
        return queryList;
    }

    public void setQueryList(List<Query> queryList) {
        this.queryList = queryList;
    }
}
