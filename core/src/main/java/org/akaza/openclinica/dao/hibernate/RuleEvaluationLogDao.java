package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleEvaluationLogBean;

public class RuleEvaluationLogDao extends AbstractDomainDao<RuleEvaluationLogBean> {

    @Override
    public Class<RuleEvaluationLogBean> domainClass() {
        return RuleEvaluationLogBean.class;
    }
}
