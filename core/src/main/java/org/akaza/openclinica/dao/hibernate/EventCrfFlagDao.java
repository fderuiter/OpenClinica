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
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfItemTag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemDataFlag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class EventCrfFlagDao extends AbstractDomainDao<EventCrfFlag> {

    @Override
    Class<EventCrfFlag> domainClass() {
        // TODO Auto-generated method stub
        return EventCrfFlag.class;
    }

    public EventCrfFlag findByEventCrfPath(int tagId, String path) {
        String query = "from " + getDomainClassName() + " where path = :path and tagId= :tagId";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("tagId", tagId);
        q.setParameter("path", path);
        
        return (EventCrfFlag) q.getResultList().stream().findFirst().orElse(null);

    }

}
