package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Section;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class SectionDao extends AbstractDomainDao<Section> {

    @Override
    Class<Section> domainClass() {
        // TODO Auto-generated method stub
        return Section.class;
    }

    public Section findByCrfVersionOrdinal(int crfVersionId, int ordinal) {
        // String query = "from " + getDomainClassName() + " section  where section.crfVersionId = :crfversionid ";
        // jakarta.persistence.Query q = getEntityManager().createQuery(query);
        // q.set.setParameter("crfversionid", crf_version_id);
        // return (Section) q.getResultList().stream().findFirst().orElse(null);

        String query = " select s.* from section s where s.crf_version_id = :crfVersionId and ordinal = :ordinal ";
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, domainClass());
        q.setParameter("crfVersionId", crfVersionId);
        q.setParameter("ordinal", ordinal);
        return (Section) q.getResultList().stream().findFirst().orElse(null);
    }

}
