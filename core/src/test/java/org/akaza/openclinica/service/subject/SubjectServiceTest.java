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
