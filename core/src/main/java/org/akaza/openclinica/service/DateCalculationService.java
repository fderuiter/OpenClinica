package org.akaza.openclinica.service;

import java.util.Calendar;
import java.util.Date;

public class DateCalculationService {

    public int calculateAgeAtEvent(Date birthDate, Date eventDate) {
        if (birthDate == null || eventDate == null) {
            return -1;
        }
        if (birthDate.after(eventDate)) {
            return -1;
        }

        Calendar birthCal = Calendar.getInstance();
        birthCal.setTime(birthDate);
        
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTime(eventDate);
        
        int age = eventCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
        
        birthCal.add(Calendar.YEAR, age);
        
        if (eventCal.before(birthCal)) {
            age--;
        }
        
        return age;
    }
}
