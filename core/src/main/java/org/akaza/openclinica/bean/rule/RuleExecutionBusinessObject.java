package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.rule.RuleSetDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Date;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.rule.RuleDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.service.managestudy.DiscrepancyNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author Krikor Krumlian
 */

@Component
public class RuleExecutionBusinessObject {
    private EventCRFDAO _eventCRFDAO;
    private RuleDAO _ruleDAO;
    private RuleSetDAO _ruleSetDAO;


    private final SessionManager sm;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected StudyBean currentStudy;
    protected UserAccountBean ub;

    @Autowired
    public RuleExecutionBusinessObject(SessionManager sm, StudyBean currentStudy, UserAccountBean ub, EventCRFDAO _eventCRFDAO, RuleDAO _ruleDAO, RuleSetDAO _ruleSetDAO) {
        this._eventCRFDAO = _eventCRFDAO;
        this._ruleDAO = _ruleDAO;
        this._ruleSetDAO = _ruleSetDAO;

        this.sm = sm;
        this.currentStudy = currentStudy;
        this.ub = ub;
    }

    public void runRule(int eventCrfId) {
        // int eventCrfId = 11;
        EventCRFBean eventCrfBean = getEventCRFBean(eventCrfId);
        RuleSetBean ruleSetBean = getRuleSetBean(eventCrfBean);
        ArrayList<RuleBean> rules = getRuleBeans(ruleSetBean);
        for (RuleBean rule : rules) {
            initializeRule(rule);
        }
    }

    public void initializeRule(RuleBean rule) {
        // source data
        // ItemDataBean sourceItemDataBean = rule.getSourceItemDataBean();
        ItemDataBean sourceItemDataBean = null;

        // target data
        // ItemDataBean targetItemDataBean = rule.getTargetItemDataBean();
        ItemDataBean targetItemDataBean = null;

        // fireRules on source & target
        // TODO KK FIX HERE
        boolean sourceResult = true;// fireRule(sourceItemDataBean,rule.getSourceItemValue(),sourceItemFormMetadataBean,rule.getSourceOperator());
        boolean targetResult = true;// fireRule(targetItemDataBean,rule.getTargetItemValue(),targetItemFormMetadataBean,rule.getTargetOperator());

        if (sourceResult && targetResult) {
            // We are good
        }
        if (sourceResult == true && targetResult == false) {
            // file a descrepancy Note
            createDiscrepancyNote(rule.toString(), targetItemDataBean, sourceItemDataBean);
        }

    }

    private void createDiscrepancyNote(String description, ItemDataBean targetItemDataBean, ItemDataBean sourceItemDataBean) {
        org.akaza.openclinica.service.managestudy.DiscrepancyNoteService discrepancyNoteService = (org.akaza.openclinica.service.managestudy.DiscrepancyNoteService) org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("discrepancyNoteService");
        discrepancyNoteService.saveFieldNotes(description, targetItemDataBean.getId(), "itemData", currentStudy, ub);
    }

    // These are dao mostly calls see how to reduce redundancy
    private EventCRFBean getEventCRFBean(int eventCrfBeanId) {
        EventCRFDAO eventCrfDao = this._eventCRFDAO;
        return eventCrfBeanId > 0 ? (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId) : null;
    }

    private RuleSetBean getRuleSetBean(EventCRFBean eventCrfBean) {
        // RuleSetDAO ruleSetDao = this._ruleSetDAO;
        return null;
    }

    private ArrayList<RuleBean> getRuleBeans(RuleSetBean ruleSet) {
        RuleDAO ruleDao = this._ruleDAO;
        return ruleSet != null ? ruleDao.findByRuleSet(ruleSet) : new ArrayList<RuleBean>();
    }

}
