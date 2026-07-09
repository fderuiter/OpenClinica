package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.HashMap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Map;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class RuleActionRunLogDao extends AbstractDomainDao<RuleActionRunLogBean> {

    @Override
    public Class<RuleActionRunLogBean> domainClass() {
        return RuleActionRunLogBean.class;
    }

    @Transactional
    public Integer findCountByRuleActionRunLogBean(RuleActionRunLogBean ruleActionRunLog) {
        StringBuilder sb = new StringBuilder("select count(r) from " + getDomainClassName() + " r where 1=1");
        Map<String, Object> params = new HashMap<>();

        if (ruleActionRunLog.getActionType() != null) {
            sb.append(" and r.actionType = :actionType");
            params.put("actionType", ruleActionRunLog.getActionType());
        }
        if (ruleActionRunLog.getItemDataId() != null) {
            sb.append(" and r.itemDataId = :itemDataId");
            params.put("itemDataId", ruleActionRunLog.getItemDataId());
        }
        if (ruleActionRunLog.getValue() != null) {
            sb.append(" and r.value = :val");
            params.put("val", ruleActionRunLog.getValue());
        }
        if (ruleActionRunLog.getRuleOid() != null) {
            sb.append(" and r.ruleOid = :ruleOid");
            params.put("ruleOid", ruleActionRunLog.getRuleOid());
        }

        Query q = getEntityManager().createQuery(sb.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }

        Long count = (Long) q.getSingleResult();
        return count.intValue();
    }

    public void delete(int itemDataId){
        String query = " delete from " + getDomainClassName() +  "  where itemDataId =:itemDataId ";
        Query q = getEntityManager().createQuery(query);
        q.setParameter("itemDataId", itemDataId);
        q.executeUpdate();
    }

}
