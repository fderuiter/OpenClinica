package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.CompletionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class CompletionStatusDao extends AbstractDomainDao<CompletionStatus> {

    @Override
    Class<CompletionStatus> domainClass() {
        // TODO Auto-generated method stub
        return CompletionStatus.class;
    }

    public CompletionStatus findByCompletionStatusId(int completion_status_id) {
        String query = "from " + getDomainClassName() + " completion_status  where completion_status.completionStatusId = :completionstatusid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("completionstatusid", completion_status_id);
        return (CompletionStatus) q.getResultList().stream().findFirst().orElse(null);
    }


}
