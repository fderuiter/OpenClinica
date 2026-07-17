package org.akaza.openclinica.service.subject;

import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.repository.UnifiedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Date;

@Component("enrollmentManager")
public class EnrollmentManager {
    private CRFDAO _cRFDAO;
    private CRFVersionDAO _cRFVersionDAO;
    private ItemDAO _itemDAO;

    private StudyDAO _studyDAO;
    private StudySubjectDAO _studySubjectDAO;
    private SubjectDAO _subjectDAO;

    private StudyParameterValueDAO _studyParameterValueDAO;


    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final UnifiedRepository unifiedRepository;
    private final StudyParameterValueDAO studyParameterValueDAO;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EnrollmentManager(DataSource dataSource, StudyParameterValueDAO _studyParameterValueDAO, StudyDAO _studyDAO, StudySubjectDAO _studySubjectDAO, SubjectDAO _subjectDAO, CRFDAO _cRFDAO, CRFVersionDAO _cRFVersionDAO, ItemDAO _itemDAO) {
        this._cRFDAO = _cRFDAO;
        this._cRFVersionDAO = _cRFVersionDAO;
        this._itemDAO = _itemDAO;

        this._studyDAO = _studyDAO;
        this._studySubjectDAO = _studySubjectDAO;
        this._subjectDAO = _subjectDAO;

        this._studyParameterValueDAO = _studyParameterValueDAO;

        this.unifiedRepository = new UnifiedRepository(dataSource);
        this.studyParameterValueDAO = this._studyParameterValueDAO;
        if (dataSource != null) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        } else {
            this.jdbcTemplate = null;
        }
    }
    
    @Transactional
    public String enrollSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        if (subjectBean.getUniqueIdentifier() != null && subjectBean.getUniqueIdentifier().trim().length() > 0 && 
                unifiedRepository.getSubjectBeanByUniqueIdentifier(subjectBean.getUniqueIdentifier()).getId() != 0) {
            String label = subjectBean.getLabel();
            subjectBean = unifiedRepository.getSubjectBeanByUniqueIdentifier(subjectBean.getUniqueIdentifier());
            subjectBean.setLabel(label);
        } else {
            subjectBean.setStatus(Status.AVAILABLE);
            subjectBean = unifiedRepository.createSubjectBean(subjectBean);
        }
        
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setSecondaryLabel(secondaryId);
        studySubject.setOwner(subjectBean.getOwner());
        studySubject.setEnrollmentDate(enrollmentDate);
        studySubject.setSubjectId(subjectBean.getId());
        studySubject.setStudyId(studyBean.getId());
        studySubject.setStatus(Status.AVAILABLE);
        
        int handleStudyId = studyBean.getParentStudyId() > 0 ? studyBean.getParentStudyId() : studyBean.getId();
        StudyParameterValueBean subjectIdGenerationParameter = studyParameterValueDAO.findByHandleAndStudy(handleStudyId, "subjectIdGeneration");
        String idSetting = subjectIdGenerationParameter.getValue();
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            String nextLabel = generateAtomicLabel();
            studySubject.setLabel(nextLabel);
        } else {
            studySubject.setLabel(subjectBean.getLabel());
            subjectBean.setLabel(null);
        }
        
        unifiedRepository.createStudySubjectBean(studySubject);
        return studySubject.getLabel();
    }
    
    public String generateSubjectId(StudyBean studyBean) {
        int handleStudyId = studyBean.getParentStudyId() > 0 ? studyBean.getParentStudyId() : studyBean.getId();
        StudyParameterValueBean subjectIdGenerationParameter = studyParameterValueDAO.findByHandleAndStudy(handleStudyId, "subjectIdGeneration");
        String idSetting = subjectIdGenerationParameter.getValue();
        if ("auto editable".equals(idSetting) || "auto non-editable".equals(idSetting)) {
            return generateAtomicLabel();
        } else {
            return null;
        }
    }

    @Transactional
    public String generateAtomicLabel() {
        try {
            Integer currentValue = jdbcTemplate.queryForObject("SELECT label_value FROM subject_label_sequence FOR UPDATE", Integer.class);
            if (currentValue == null || currentValue == 0) {
                currentValue = unifiedRepository.findTheGreatestStudySubjectLabel();
                if (currentValue < 0) {
                    currentValue = 0;
                }
            }
            int nextValue = currentValue + 1;
            jdbcTemplate.update("UPDATE subject_label_sequence SET label_value = ?", nextValue);
            return String.valueOf(nextValue);
        } catch (Exception e) {
            logger.error("Failed to generate subject label", e);
            throw new RuntimeException("Failed to generate subject label", e);
        }
    }
}
