package org.akaza.openclinica.service.extract;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.akaza.openclinica.bean.submit.crfdata.ExportStudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.Subject;
import org.akaza.openclinica.domain.datamap.SubjectGroupMap;
import org.junit.Before;
import org.junit.Test;

public class DateCalculationIntegrationTest {

    private GenerateClinicalDataServiceImpl service;
    private StudyDao mockStudyDao;
    private StudySubjectDao mockStudySubjectDao;
    
    @Before
    public void setUp() {
        service = new GenerateClinicalDataServiceImpl();
        mockStudyDao = mock(StudyDao.class);
        mockStudySubjectDao = mock(StudySubjectDao.class);
        
        service.setStudyDao(mockStudyDao);
        service.setStudySubjectDao(mockStudySubjectDao);
        service.setCollectAudits(false);
        service.setCollectDns(false);
    }

    private Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Test
    public void testExportPipelineCalculatesAgeCorrectly() {
        Study mockStudy = new Study();
        mockStudy.setOc_oid("S_TEST");

        Subject mockSubject = new Subject();
        mockSubject.setDateOfBirth(createDate(1990, 5, 15));
        mockSubject.setGender('M');

        StudySubject mockStudySubject = new StudySubject();
        mockStudySubject.setOcOid("SS_TEST");
        mockStudySubject.setSubject(mockSubject);
        mockStudySubject.setStudy(mockStudy);
        mockStudySubject.setSubjectGroupMaps(new ArrayList<SubjectGroupMap>());
        
        mockStudySubject.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);

        StudyEventDefinition sed = new StudyEventDefinition();
        sed.setOc_oid("SE_TEST");

        StudyEvent mockEvent = new StudyEvent();
        mockEvent.setDateStart(createDate(2020, 5, 14)); // 1 day before birthday
        mockEvent.setStartTimeFlag(false);
        mockEvent.setEndTimeFlag(false);
        mockEvent.setEventCrfs(new ArrayList());
        mockEvent.setStudyEventDefinition(sed);
        mockEvent.setStudySubject(mockStudySubject);
        mockEvent.setSampleOrdinal(1);
        mockEvent.setSubjectEventStatusId(1);

        List<StudyEvent> events = new ArrayList<StudyEvent>();
        events.add(mockEvent);

        when(mockStudyDao.findByColumnName("S_TEST", "oc_oid")).thenReturn(mockStudy);
        when(mockStudySubjectDao.findByColumnName("SS_TEST", "ocOid")).thenReturn(mockStudySubject);
        when(mockStudySubjectDao.fetchListSEs("SS_TEST")).thenReturn((ArrayList) events);

        OdmClinicalDataBean data = service.getClinicalData("S_TEST", "SS_TEST");

        List<ExportSubjectDataBean> subjects = data.getExportSubjectData();
        assertEquals(1, subjects.size());
        ExportSubjectDataBean subjectData = subjects.get(0);
        
        List<ExportStudyEventDataBean> eventData = subjectData.getExportStudyEventData();
        assertEquals(1, eventData.size());
        
        // Event is 1 day before 30th birthday, so age should be 29
        assertEquals(Integer.valueOf(29), eventData.get(0).getAgeAtEvent());
    }
}
