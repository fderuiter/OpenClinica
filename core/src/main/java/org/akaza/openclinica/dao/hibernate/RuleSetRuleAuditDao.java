package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleSetBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.rule.RuleSetRuleAuditBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class RuleSetRuleAuditDao extends AbstractDomainDao<RuleSetRuleAuditBean> {

    @Override
    public Class<RuleSetRuleAuditBean> domainClass() {
        return RuleSetRuleAuditBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetRuleAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        String query = "from " + getDomainClassName() + " ruleSetRuleAudit  where ruleSetRuleAudit.ruleSetRuleBean.ruleSetBean = :ruleSet  ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("ruleSet", ruleSet);
        return (ArrayList<RuleSetRuleAuditBean>) q.getResultList();
    }
}
