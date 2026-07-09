package org.akaza.openclinica.dao.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.DnItemDataMap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class DnItemDataMapDao extends AbstractDomainDao<DnItemDataMap> {

    @Override
    Class<DnItemDataMap> domainClass() {
        return DnItemDataMap.class;
    }

    public List<DnItemDataMap> findByItemData(Integer itemDataId) {
        String query = "from " + getDomainClassName() + " do where do.itemData.itemDataId = :itemdataid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("itemdataid", itemDataId);
        return (List<DnItemDataMap>) q.getResultList();
    }
}
