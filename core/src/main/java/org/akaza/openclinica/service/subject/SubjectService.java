package org.akaza.openclinica.service.subject;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

public class SubjectService implements SubjectServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    org.akaza.openclinica.repository.UnifiedRepository unifiedRepository;
    StudyParameterValueDAO studyParameterValueDAO;
    UserAccountDAO userAccountDao;
    DataSource dataSource;
    EnrollmentManager enrollmentManager;

    public void setEnrollmentManager(EnrollmentManager enrollmentManager) {
        this.enrollmentManager = enrollmentManager;
    }

    public SubjectService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SubjectService(SessionManager sessionManager) {
        this.dataSource = sessionManager.getDataSource();
    }

    public List<StudySubjectBean> getStudySubject(StudyBean study) {
        return getUnifiedRepository().findAllStudySubjectBeansByStudy(study);

    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.subject.SubjectServiceInterface#createSubject(org.akaza.openclinica.bean.submit.SubjectBean,
     * org.akaza.openclinica.bean.managestudy.StudyBean)
     */
    public String createSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        if (this.enrollmentManager == null) {
            this.enrollmentManager = new EnrollmentManager(dataSource);
        }
        return this.enrollmentManager.enrollSubject(subjectBean, studyBean, enrollmentDate, secondaryId);
    }

    private StudySubjectBean createStudySubject(SubjectBean subject, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        // Obsolete, left here just in case internal legacy code calls it, though it shouldn't
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setSecondaryLabel(secondaryId);
        studySubject.setOwner(subject.getOwner());
        studySubject.setEnrollmentDate(enrollmentDate);
        studySubject.setSubjectId(subject.getId());
        studySubject.setStudyId(studyBean.getId());
        studySubject.setStatus(Status.AVAILABLE);
        
        int handleStudyId = studyBean.getParentStudyId() > 0 ? studyBean.getParentStudyId() : studyBean.getId();
        StudyParameterValueBean subjectIdGenerationParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectIdGeneration");
        String idSetting = subjectIdGenerationParameter.getValue();
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            int nextLabel = getUnifiedRepository().findTheGreatestStudySubjectLabel() + 1;
            studySubject.setLabel(Integer.toString(nextLabel));
        } else {
        	studySubject.setLabel(subject.getLabel());
        	subject.setLabel(null);
        }
        
        return studySubject;
    }

    public String generateSubjectId(StudyBean studyBean) {
        if (this.enrollmentManager == null) {
            this.enrollmentManager = new EnrollmentManager(dataSource);
        }
        return this.enrollmentManager.generateSubjectId(studyBean);
    }

    public void validateSubjectTransfer(SubjectTransferBean subjectTransferBean) {
        // TODO: Validate here
    }

    /**
     * Getting the first user account from the database. This would be replaced by an authenticated user who is doing the SOAP requests .
     * 
     * @return UserAccountBean
     */
    private UserAccountBean getUserAccount() {

        UserAccountBean user = new UserAccountBean();
        user.setId(1);
        return user;
    }

    
    public StudyParameterValueDAO getStudyParameterValueDAO() {
        return this.studyParameterValueDAO != null ? studyParameterValueDAO : new StudyParameterValueDAO(dataSource);
    }



    /**
     * @return the UserAccountDao
     */
    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }

    /**
     * @return the datasource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param datasource
     *            the datasource to set
     */
    public void setDatasource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    private org.akaza.openclinica.repository.UnifiedRepository getUnifiedRepository() {
        if (this.unifiedRepository == null) {
            this.unifiedRepository = new org.akaza.openclinica.repository.UnifiedRepository(dataSource);
        }
        return this.unifiedRepository;
    }

    public SubjectBean updateSubject(SubjectBean subjectBean, UserAccountBean updater, String reasonForChange, org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean note) {
        org.akaza.openclinica.dao.submit.SubjectDAO sdao = new org.akaza.openclinica.dao.submit.SubjectDAO(dataSource);
        
        SubjectBean oldSubject = (SubjectBean) sdao.findByPK(subjectBean.getId());
        subjectBean.setUpdater(updater);
        subjectBean.setUpdatedDate(new Date());
        SubjectBean updatedSubject = (SubjectBean) sdao.update(subjectBean);
        
        if (note != null && note.getId() > 0) {
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(dataSource);
            Integer auditId = jdbcTemplate.queryForObject("SELECT max(audit_id) FROM audit_log_event WHERE audit_table = 'subject' AND entity_id = ?", Integer.class, updatedSubject.getId());
            if (auditId != null) {
                org.akaza.openclinica.dao.admin.AuditEventDAO auditEventDAO = new org.akaza.openclinica.dao.admin.AuditEventDAO(dataSource);
                auditEventDAO.createAuditEventDiscrepancyNoteLink(auditId, note.getId());
            }
        }
        
        return updatedSubject;
    }
}
