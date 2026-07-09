package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ResolutionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ResolutionStatusDao extends AbstractDomainDao<ResolutionStatus> {

    @Override
    public Class<ResolutionStatus> domainClass() {
        return ResolutionStatus.class;
    }
    public ResolutionStatus findByResolutionStatusId(Integer resolutionStatusId) {
        String query = "from " + getDomainClassName() + " do  where do.resolutionStatusId = :resolutionstatusid";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("resolutionstatusid", resolutionStatusId);
        return (ResolutionStatus) q.getResultList().stream().findFirst().orElse(null);
    }

}
