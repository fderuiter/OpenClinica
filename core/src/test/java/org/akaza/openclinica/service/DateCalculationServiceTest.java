package org.akaza.openclinica.service;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

public class DateCalculationServiceTest {

    private DateCalculationService service = new DateCalculationService();

    private Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Test
    public void testNormalBirthday() {
        Date birthDate = createDate(1990, 5, 15);
        
        // Before birthday
        Date event1 = createDate(2020, 5, 14);
        assertEquals(29, service.calculateAgeAtEvent(birthDate, event1));
        
        // On birthday
        Date event2 = createDate(2020, 5, 15);
        assertEquals(30, service.calculateAgeAtEvent(birthDate, event2));
        
        // After birthday
        Date event3 = createDate(2020, 5, 16);
        assertEquals(30, service.calculateAgeAtEvent(birthDate, event3));
    }

    @Test
    public void testLeapYearBirthday() {
        Date birthDate = createDate(2004, 2, 29); // Leap year
        
        // Non-leap year events
        Date event1 = createDate(2005, 2, 27);
        assertEquals(0, service.calculateAgeAtEvent(birthDate, event1));
        
        Date event2 = createDate(2005, 3, 1);
        assertEquals(1, service.calculateAgeAtEvent(birthDate, event2));
        
        // Leap year events
        Date event3 = createDate(2008, 2, 27);
        assertEquals(3, service.calculateAgeAtEvent(birthDate, event3));
        
        Date event4 = createDate(2008, 2, 29);
        assertEquals(4, service.calculateAgeAtEvent(birthDate, event4));
    }

    @Test
    public void testFutureEvent() {
        Date birthDate = createDate(1990, 5, 15);
        Date event = createDate(1980, 5, 15);
        assertEquals(-1, service.calculateAgeAtEvent(birthDate, event));
    }
}
