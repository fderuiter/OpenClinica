package org.akaza.openclinica.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

public class EventServiceTest {

    private EventService eventService;
    private int mockMaxOrdinalToReturn;
    private ArrayList mockEventsToReturn;

    @Before
    public void setUp() {
        org.akaza.openclinica.i18n.util.ResourceBundleProvider.updateLocale(new java.util.Locale("en", "US"));
        eventService = new EventService((javax.sql.DataSource) null);
        
        eventService.studyEventDao = new StudyEventDAO(null) {
            @Override
            public int getMaxSampleOrdinal(StudyEventDefinitionBean sed, StudySubjectBean ss) {
                return mockMaxOrdinalToReturn;
            }
            
            @Override
            public ArrayList findAllByDefinitionAndSubject(StudyEventDefinitionBean sed, StudySubjectBean ss) {
                return mockEventsToReturn;
            }
        };
    }

    @Test
    public void testGetMaxSampleOrdinal() {
        StudyEventDefinitionBean def = new StudyEventDefinitionBean();
        StudySubjectBean sub = new StudySubjectBean();
        
        mockMaxOrdinalToReturn = 5;
        
        int result = eventService.getMaxSampleOrdinal(def, sub);
        assertEquals(5, result);
    }

    @Test
    public void testCanSubjectScheduleAnEvent_Repeating() {
        StudyEventDefinitionBean def = new StudyEventDefinitionBean();
        def.setRepeating(true);
        StudySubjectBean sub = new StudySubjectBean();
        
        boolean result = eventService.canSubjectScheduleAnEvent(def, sub);
        assertTrue(result);
    }

    @Test
    public void testCanSubjectScheduleAnEvent_NotRepeating_HasEvents() {
        StudyEventDefinitionBean def = new StudyEventDefinitionBean();
        def.setRepeating(false);
        StudySubjectBean sub = new StudySubjectBean();
        
        mockEventsToReturn = new ArrayList();
        mockEventsToReturn.add(new Object());
        
        boolean result = eventService.canSubjectScheduleAnEvent(def, sub);
        assertFalse(result);
    }
}
