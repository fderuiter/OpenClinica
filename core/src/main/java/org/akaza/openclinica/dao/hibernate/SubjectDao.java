package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Subject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class SubjectDao extends AbstractDomainDao<Subject> {

    @Override
    Class<Subject> domainClass() {
        // TODO Auto-generated method stub
        return Subject.class;
    }
    
    public Subject findBySubjectId(Integer subjectId) {
        String query = "from " + getDomainClassName() + " do  where do.subjectId = :subject_id ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("subject_id", subjectId);
        return (Subject) q.getResultList().stream().findFirst().orElse(null);
    }

    public Subject findByUniqueIdentifier(String uniqueIdentifier) {
        String query = "from " + getDomainClassName() + " do  where do.uniqueIdentifier = :unique_identifier ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("unique_identifier", uniqueIdentifier);
        return (Subject) q.getResultList().stream().findFirst().orElse(null);
    }
}
