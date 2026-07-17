package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;

@Component
public class ActionProcessorFacade {
    private StudyDAO _studyDAO;
    private StudyParameterValueDAO _studyParameterValueDAO;

    @Autowired
    public ActionProcessorFacade(StudyDAO _studyDAO, StudyParameterValueDAO _studyParameterValueDAO) {
        this._studyDAO = _studyDAO;
        this._studyParameterValueDAO = _studyParameterValueDAO;
    }


    public static ActionProcessor getActionProcessor(ActionType actionType, DataSource ds, JavaMailSenderImpl mailSender,
            DynamicsMetadataService itemMetadataService, RuleSetBean ruleSet, RuleActionRunLogDao ruleActionRunLogDao, RuleSetRuleBean ruleSetRule)
            throws OpenClinicaSystemException {
        switch (actionType) {
        case FILE_DISCREPANCY_NOTE:
            return new DiscrepancyNoteActionProcessor(ds, ruleActionRunLogDao, ruleSetRule);
        case EMAIL:
            return new EmailActionProcessor(ds, mailSender, ruleActionRunLogDao, ruleSetRule);
        case NOTIFICATION:
            return new NotificationActionProcessor(ds, mailSender, ruleSetRule);
        case SHOW:
            return new ShowActionProcessor(ds, itemMetadataService, ruleSet);
        case HIDE:
            return new HideActionProcessor(ds, itemMetadataService, ruleSet);
        case INSERT:
            return new InsertActionProcessor(ds, itemMetadataService, ruleActionRunLogDao, ruleSet, ruleSetRule);
        case RANDOMIZE:
            return new RandomizeActionProcessor(ds, itemMetadataService, ruleActionRunLogDao, ruleSet, ruleSetRule, new org.akaza.openclinica.dao.managestudy.StudyDAO(ds), new org.akaza.openclinica.dao.service.StudyParameterValueDAO(ds));
        default:
            throw new OpenClinicaSystemException("actionType", "Unrecognized action type!");
        }
    }
}
