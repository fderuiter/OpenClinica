package org.akaza.openclinica.service;

import java.util.Date;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

public class DateCalculationService {

    public int calculateAgeAtEvent(Date birthDate, Date eventDate) {
        Period agePeriod = calculateAgePeriodAtEvent(birthDate, eventDate);
        if (agePeriod == null) {
            return -1;
        }
        return agePeriod.getYears();
    }

    public Period calculateAgePeriodAtEvent(Date birthDate, Date eventDate) {
        if (birthDate == null || eventDate == null) {
            return null;
        }
        if (birthDate.after(eventDate)) {
            return null;
        }

        LocalDate birthLocal = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate eventLocal = eventDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return Period.between(birthLocal, eventLocal);
    }
}
