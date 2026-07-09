package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleSetAuditBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class RuleSetAuditDao extends AbstractDomainDao<RuleSetAuditBean> {

    @Override
    public Class<RuleSetAuditBean> domainClass() {
        return RuleSetAuditBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        String query = "from " + getDomainClassName() + " ruleSetAudit  where ruleSetAudit.ruleSetBean = :ruleSet  ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("ruleSet", ruleSet);
        return (ArrayList<RuleSetAuditBean>) q.getResultList();
    }
}
