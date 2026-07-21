package org.akaza.openclinica.service.subject;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.repository.UnifiedRepository;
import org.akaza.openclinica.repository.SubjectRepository;
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

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final UnifiedRepository unifiedRepository;
    private final SubjectRepository subjectRepository;
    private final StudyParameterValueDAO studyParameterValueDAO;
    private final JdbcTemplate jdbcTemplate;

    public EnrollmentManager(DataSource dataSource) {
        this(dataSource, null);
    }

    @Autowired
    public EnrollmentManager(DataSource dataSource, SubjectRepository subjectRepository) {
        this.unifiedRepository = new UnifiedRepository(dataSource);
        this.subjectRepository = subjectRepository;
        this.studyParameterValueDAO = new StudyParameterValueDAO(dataSource);
        if (dataSource != null) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        } else {
            this.jdbcTemplate = null;
        }
    }
    
    @Transactional
    public String enrollSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        if (subjectBean.getUniqueIdentifier() != null && subjectBean.getUniqueIdentifier().trim().length() > 0 && 
                getSubjectBeanByUniqueIdentifier(subjectBean.getUniqueIdentifier()).getId() != 0) {
            String label = subjectBean.getLabel();
            subjectBean = getSubjectBeanByUniqueIdentifier(subjectBean.getUniqueIdentifier());
            subjectBean.setLabel(label);
        } else {
            subjectBean.setStatus(Status.AVAILABLE);
            subjectBean = createSubjectBean(subjectBean);
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
        
        createStudySubjectBean(studySubject);
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
                currentValue = findTheGreatestStudySubjectLabel();
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
    private SubjectBean getSubjectBeanByUniqueIdentifier(String identifier) {
        return subjectRepository != null ? subjectRepository.getSubjectBeanByUniqueIdentifier(identifier) : unifiedRepository.getSubjectBeanByUniqueIdentifier(identifier);
    }
    
    private SubjectBean createSubjectBean(SubjectBean bean) {
        return subjectRepository != null ? subjectRepository.createSubjectBean(bean) : unifiedRepository.createSubjectBean(bean);
    }
    
    private StudySubjectBean createStudySubjectBean(StudySubjectBean bean) {
        return subjectRepository != null ? subjectRepository.createStudySubjectBean(bean) : unifiedRepository.createStudySubjectBean(bean);
    }
    
    private int findTheGreatestStudySubjectLabel() {
        return subjectRepository != null ? subjectRepository.findTheGreatestStudySubjectLabel() : unifiedRepository.findTheGreatestStudySubjectLabel();
    }
}
