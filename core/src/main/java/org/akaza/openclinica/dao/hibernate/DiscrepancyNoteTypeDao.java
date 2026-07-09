package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.DiscrepancyNoteType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class DiscrepancyNoteTypeDao extends AbstractDomainDao<DiscrepancyNoteType> {

    @Override
    public Class<DiscrepancyNoteType> domainClass() {
        return DiscrepancyNoteType.class;
    }
    public DiscrepancyNoteType findByDiscrepancyNoteTypeId(Integer discrepancyNoteTypeId) {
        String query = "from " + getDomainClassName() + " do  where do.discrepancyNoteTypeId = :discrepancynotetypeid";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("discrepancynotetypeid", discrepancyNoteTypeId);
        return (DiscrepancyNoteType) q.getResultList().stream().findFirst().orElse(null);
    }

}
