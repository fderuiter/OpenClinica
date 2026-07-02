package org.akaza.openclinica.service.subject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.junit.Before;
import org.junit.Test;

public class SubjectServiceTest {

    private SubjectService subjectService;
    private StudyParameterValueBean mockParamToReturn;
    private int mockLabelToReturn;

    @Before
    public void setUp() {
        org.akaza.openclinica.i18n.util.ResourceBundleProvider.updateLocale(new java.util.Locale("en", "US"));
        subjectService = new SubjectService((javax.sql.DataSource) null);
        
        EnrollmentManager em = new EnrollmentManager(null) {
            @Override
            public String generateAtomicLabel() {
                return String.valueOf(mockLabelToReturn + 1);
            }
        };
        
        // Use reflection or just inject if we can
        // But EnrollmentManager doesn't have setters for DAOs, we can subclass it
        em = new EnrollmentManager(null) {
            @Override
            public String generateSubjectId(StudyBean studyBean) {
                int handleStudyId = studyBean.getParentStudyId() > 0 ? studyBean.getParentStudyId() : studyBean.getId();
                StudyParameterValueBean subjectIdGenerationParameter = mockParamToReturn;
                String idSetting = subjectIdGenerationParameter.getValue();
                if ("auto editable".equals(idSetting) || "auto non-editable".equals(idSetting)) {
                    return String.valueOf(mockLabelToReturn + 1);
                } else {
                    return null;
                }
            }
        };
        subjectService.setEnrollmentManager(em);
        
        subjectService.unifiedRepository = new org.akaza.openclinica.repository.UnifiedRepository(null) {
            @Override
            public int findTheGreatestStudySubjectLabel() {
                return mockLabelToReturn;
            }
        };
        
        subjectService.studyParameterValueDAO = new StudyParameterValueDAO(null) {
            @Override
            public StudyParameterValueBean findByHandleAndStudy(int studyId, String parameter) {
                return mockParamToReturn;
            }
        };
    }

    @Test
    public void testGenerateSubjectId_AutoEditable() {
        StudyBean study = new StudyBean();
        study.setId(1);
        study.setParentStudyId(0);

        mockParamToReturn = new StudyParameterValueBean();
        mockParamToReturn.setValue("auto editable");
        mockLabelToReturn = 100;

        String result = subjectService.generateSubjectId(study);
        assertEquals("101", result);
    }

    @Test
    public void testGenerateSubjectId_Manual() {
        StudyBean study = new StudyBean();
        study.setId(1);
        study.setParentStudyId(0);

        mockParamToReturn = new StudyParameterValueBean();
        mockParamToReturn.setValue("manual");

        String result = subjectService.generateSubjectId(study);
        assertNull(result);
    }
}
