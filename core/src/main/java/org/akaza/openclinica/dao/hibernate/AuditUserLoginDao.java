package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;

public class AuditUserLoginDao extends AbstractDomainDao<AuditUserLoginBean> {

    @Override
    public Class<AuditUserLoginBean> domainClass() {
        return AuditUserLoginBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<AuditUserLoginBean> findAll() {
        String query = "from " + getDomainClassName() + " aul order by aul.loginAttemptDate desc ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        return (ArrayList<AuditUserLoginBean>) q.getResultList();
    }

    public int getCountWithFilter(final AuditUserLoginFilter filter) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AuditUserLoginBean> root = cq.from(domainClass());
        cq.select(cb.count(root));
        filter.execute(cb, cq, root);
        Long count = getEntityManager().createQuery(cq).getResultList().stream().findFirst().orElse(0L);
        return count.intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<AuditUserLoginBean> getWithFilterAndSort(final AuditUserLoginFilter filter, final AuditUserLoginSort sort, final int rowStart,
            final int rowEnd) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<AuditUserLoginBean> cq = cb.createQuery(domainClass());
        Root<AuditUserLoginBean> root = cq.from(domainClass());
        cq.select(root);
        filter.execute(cb, cq, root);
        sort.execute(cb, cq, root);
        Query query = getEntityManager().createQuery(cq);
        query.setFirstResult(rowStart);
        query.setMaxResults(rowEnd - rowStart);
        return (ArrayList<AuditUserLoginBean>) query.getResultList();
    }
}
