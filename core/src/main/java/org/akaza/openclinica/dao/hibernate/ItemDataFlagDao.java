package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.EventCrfFlag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemDataFlag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ItemDataFlagDao extends AbstractDomainDao<ItemDataFlag> {

    @Override
    Class<ItemDataFlag> domainClass() {
        // TODO Auto-generated method stub
        return ItemDataFlag.class;
    }


    
    public List<ItemDataFlag> findAllByEventCrfPath(int tag_id , String eventCrfPath ) {
    	
        String query = " from " + getDomainClassName() + "  where "
                + " tag_id = :tag_id and path LIKE :eventCrfPath";
        
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("tag_id", tag_id);
        q.setParameter("eventCrfPath", eventCrfPath + ".%");
        
        return (List<ItemDataFlag>) q.getResultList();
    }

    public ItemDataFlag findByItemDataPath(int tag_id ,  String itemDataPath ) {

        String query = " from " + getDomainClassName() + "  where "
                + " tag_id= :tag_id  and path= :itemDataPath ";
        
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("tag_id", tag_id);
        q.setParameter("itemDataPath", itemDataPath);
        
        return (ItemDataFlag) q.getResultList().stream().findFirst().orElse(null);
    }


    
}
