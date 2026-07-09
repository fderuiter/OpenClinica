package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class RuleDao extends AbstractDomainDao<RuleBean> {

    @Override
    public Class<RuleBean> domainClass() {
        return RuleBean.class;
    }

    public RuleBean findByOid(RuleBean ruleBean) {
        String query = "from " + getDomainClassName() + " rule  where rule.oid = :oid and  rule.studyId = :studyId ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("oid", ruleBean.getOid());
        q.setParameter("studyId", ruleBean.getStudyId());
        return (RuleBean) q.getResultList().stream().findFirst().orElse(null);
    }

    public RuleBean findByOid(String oid, Integer studyId) {
        String query = "from " + getDomainClassName() + " rule  where rule.oid = :oid and  rule.studyId = :studyId ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("oid", oid);
        q.setParameter("studyId", studyId);
        return (RuleBean) q.getResultList().stream().findFirst().orElse(null);
    }

}
