package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.IdtView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfItemTag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class EventDefinitionCrfItemTagDao extends AbstractDomainDao<EventDefinitionCrfItemTag> {

    @Override
    Class<EventDefinitionCrfItemTag> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrfItemTag.class;
    }

    public List<EventDefinitionCrfItemTag> findAllByCrfPath(int tag_id, String crfPath, boolean active) {

        String query = " from " + getDomainClassName() + "  where " + " tag_id= " + tag_id + " and active=" + active + " and path LIKE '" + crfPath + ".%'";

        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        return (List<EventDefinitionCrfItemTag>) q.getResultList();
    }

    public EventDefinitionCrfItemTag findByItemPath(int tag_id, boolean active, String itemPath) {

        String query = " from " + getDomainClassName() + "  where " + " tag_id= " + tag_id + " and active=" + active + " and path= '" + itemPath + "'";

        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        return (EventDefinitionCrfItemTag) q.getResultList().stream().findFirst().orElse(null);
    }

}
