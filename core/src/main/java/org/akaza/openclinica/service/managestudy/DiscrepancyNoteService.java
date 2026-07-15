/* 
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * OpenClinica is distributed under the
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.service.managestudy;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.ApplicationContextProvider;
import org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import org.akaza.openclinica.dao.hibernate.DiscrepancyNoteTypeDao;
import org.akaza.openclinica.dao.hibernate.ResolutionStatusDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.akaza.openclinica.domain.datamap.DiscrepancyNoteType;
import org.akaza.openclinica.domain.datamap.ResolutionStatus;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.user.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;

@Service("discrepancyNoteService")
public class DiscrepancyNoteService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    DataSource ds;
    private DiscrepancyNoteDao discrepancyNoteDao;
    private StudyDao studyDao;
    private UserAccountDao userAccountDao;
    private ResolutionStatusDao resolutionStatusDao;
    private DiscrepancyNoteTypeDao discrepancyNoteTypeDao;

    public DiscrepancyNoteService() {
    }

    public DiscrepancyNoteService(DataSource ds) {
        this.ds = ds;
    }

    @Transactional
    public void saveFieldNotes(final String description, final int entityId, final String entityType, final StudyBean sb, final UserAccountBean ub) {
        DiscrepancyNote parent = createDiscrepancyNote(description, entityId, entityType, sb, ub, null);
        createDiscrepancyNote(description, entityId, entityType, sb, ub, parent);
    }

    @Transactional
    public void saveFieldNotesAndRunLog(final String description, final int entityId, final String entityType, final StudyBean sb, final UserAccountBean ub, RuleActionRunLogBean ruleActionRunLog, RuleActionRunLogDao ruleActionRunLogDao) {
        saveFieldNotes(description, entityId, entityType, sb, ub);
        ruleActionRunLogDao.saveOrUpdate(ruleActionRunLog);
    }

    private DiscrepancyNote createDiscrepancyNote(String description, int entityId, String entityType, StudyBean sb, UserAccountBean ub,
            DiscrepancyNote parentNote) {
        DiscrepancyNote dnb = new DiscrepancyNote();
        
        Study study = getStudyDao().findById(sb.getId());
        UserAccount owner = getUserAccountDao().findById(ub.getId());
        ResolutionStatus rs = getResolutionStatusDao().findById(1);
        DiscrepancyNoteType dt = getDiscrepancyNoteTypeDao().findById(1);
        
        dnb.setStudy(study);
        dnb.setEntityType(entityType);
        dnb.setDescription(description);
        dnb.setResolutionStatus(rs);
        dnb.setDiscrepancyNoteType(dt);
        dnb.setUserAccountByOwnerId(owner);
        dnb.setDateCreated(new java.util.Date());
        
        if (parentNote != null) {
            dnb.setParentDiscrepancyNote(parentNote);
        }
        
        dnb = getDiscrepancyNoteDao().saveOrUpdate(dnb);
        getDiscrepancyNoteDao().createMapping(dnb, entityId, "value", entityType);
        return dnb;
    }

    private DiscrepancyNoteDao getDiscrepancyNoteDao() {
        if (discrepancyNoteDao == null) {
            discrepancyNoteDao = (DiscrepancyNoteDao) ApplicationContextProvider.getApplicationContext().getBean("discrepancyNoteDao");
        }
        return discrepancyNoteDao;
    }

    private StudyDao getStudyDao() {
        if (studyDao == null) {
            studyDao = (StudyDao) ApplicationContextProvider.getApplicationContext().getBean("studyDao");
        }
        return studyDao;
    }

    private UserAccountDao getUserAccountDao() {
        if (userAccountDao == null) {
            userAccountDao = (UserAccountDao) ApplicationContextProvider.getApplicationContext().getBean("userAccountDao");
        }
        return userAccountDao;
    }

    private ResolutionStatusDao getResolutionStatusDao() {
        if (resolutionStatusDao == null) {
            resolutionStatusDao = (ResolutionStatusDao) ApplicationContextProvider.getApplicationContext().getBean("resolutionStatusDao");
        }
        return resolutionStatusDao;
    }

    private DiscrepancyNoteTypeDao getDiscrepancyNoteTypeDao() {
        if (discrepancyNoteTypeDao == null) {
            discrepancyNoteTypeDao = (DiscrepancyNoteTypeDao) ApplicationContextProvider.getApplicationContext().getBean("discrepancyNoteTypeDao");
        }
        return discrepancyNoteTypeDao;
    }

}
