package org.akaza.openclinica.core.util;

import java.text.AttributedCharacterIterator;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A thread-safe proxy for SimpleDateFormat that delegates all operations to a
 * ThreadLocal SimpleDateFormat instance. This allows legacy code expecting a 
 * SimpleDateFormat object to operate concurrently without race conditions.
 */
public class ThreadSafeProxySimpleDateFormat extends SimpleDateFormat {

    private final ThreadLocal<SimpleDateFormat> threadLocalFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM/dd/yyyy");
        }
    };

    public ThreadSafeProxySimpleDateFormat() {
        super("MM/dd/yyyy");
    }

    public void setFormat(SimpleDateFormat df) {
        threadLocalFormat.set(df);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
        return threadLocalFormat.get().format(date, toAppendTo, pos);
    }

    @Override
    public Date parse(String text, ParsePosition pos) {
        return threadLocalFormat.get().parse(text, pos);
    }

    @Override
    public Date parse(String source) throws ParseException {
        return threadLocalFormat.get().parse(source);
    }

    @Override
    public void applyPattern(String pattern) {
        threadLocalFormat.get().applyPattern(pattern);
    }

    @Override
    public void applyLocalizedPattern(String pattern) {
        threadLocalFormat.get().applyLocalizedPattern(pattern);
    }

    @Override
    public String toPattern() {
        return threadLocalFormat.get().toPattern();
    }

    @Override
    public String toLocalizedPattern() {
        return threadLocalFormat.get().toLocalizedPattern();
    }

    @Override
    public void set2DigitYearStart(Date startDate) {
        threadLocalFormat.get().set2DigitYearStart(startDate);
    }

    @Override
    public Date get2DigitYearStart() {
        return threadLocalFormat.get().get2DigitYearStart();
    }

    @Override
    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
        threadLocalFormat.get().setDateFormatSymbols(newFormatSymbols);
    }

    @Override
    public DateFormatSymbols getDateFormatSymbols() {
        return threadLocalFormat.get().getDateFormatSymbols();
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        threadLocalFormat.get().setTimeZone(zone);
    }

    @Override
    public TimeZone getTimeZone() {
        return threadLocalFormat.get().getTimeZone();
    }

    @Override
    public void setLenient(boolean lenient) {
        threadLocalFormat.get().setLenient(lenient);
    }

    @Override
    public boolean isLenient() {
        return threadLocalFormat.get().isLenient();
    }

    @Override
    public void setCalendar(Calendar newCalendar) {
        threadLocalFormat.get().setCalendar(newCalendar);
    }

    @Override
    public Calendar getCalendar() {
        return threadLocalFormat.get().getCalendar();
    }

    @Override
    public void setNumberFormat(NumberFormat newNumberFormat) {
        threadLocalFormat.get().setNumberFormat(newNumberFormat);
    }

    @Override
    public NumberFormat getNumberFormat() {
        return threadLocalFormat.get().getNumberFormat();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ThreadSafeProxySimpleDateFormat) {
            return threadLocalFormat.get().equals(((ThreadSafeProxySimpleDateFormat) obj).threadLocalFormat.get());
        }
        return threadLocalFormat.get().equals(obj);
    }

    @Override
    public int hashCode() {
        return threadLocalFormat.get().hashCode();
    }

    @Override
    public Object clone() {
        return threadLocalFormat.get().clone();
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return threadLocalFormat.get().formatToCharacterIterator(obj);
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return threadLocalFormat.get().format(obj, toAppendTo, pos);
    }
}
