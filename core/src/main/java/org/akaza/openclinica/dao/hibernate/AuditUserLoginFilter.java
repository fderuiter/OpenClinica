package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditUserLoginFilter {

    List<Filter> filters = new ArrayList<>();

    public void addFilter(String property, Object value) {
        filters.add(new Filter(property, value));
    }

    public void execute(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<AuditUserLoginBean> root) {
        List<Predicate> predicates = new ArrayList<>();
        for (Filter filter : filters) {
            Predicate p = buildPredicate(cb, root, filter.getProperty(), filter.getValue());
            if (p != null) predicates.add(p);
        }
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }
    }

    private Predicate buildPredicate(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String property, Object value) {
        if (value != null) {
            if (property.equals("loginStatus")) {
                return cb.equal(root.get(property), LoginStatus.getByName((String) value));
            } else if (property.equals("loginAttemptDate")) {
                Predicate p = onlyYearAndMonthAndDayAndHourAndMinute(cb, root, String.valueOf(value));
                if (p != null) return p;
                p = onlyYearAndMonthAndDayAndHour(cb, root, String.valueOf(value));
                if (p != null) return p;
                p = onlyYearAndMonthAndDay(cb, root, String.valueOf(value));
                if (p != null) return p;
                p = onlyYearAndMonth(cb, root, String.valueOf(value));
                if (p != null) return p;
                return onlyYear(cb, root, String.valueOf(value));
            } else {
                return cb.like(cb.lower(root.get(property)), "%" + value.toString().toLowerCase() + "%");
            }
        }
        return null;
    }

    private Predicate onlyYear(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String value) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy");
            Date startDate = format.parse(value);
            DateTime dt = new DateTime(startDate.getTime());
            dt = dt.plusYears(1);
            Date endDate = dt.toDate();
            return cb.between(root.get("loginAttemptDate"), startDate, endDate);
        } catch (Exception e) {
            return null;
        }
    }

    private Predicate onlyYearAndMonth(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String value) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM");
            Date startDate = format.parse(value);
            DateTime dt = new DateTime(startDate.getTime());
            dt = dt.plusMonths(1);
            Date endDate = dt.toDate();
            return cb.between(root.get("loginAttemptDate"), startDate, endDate);
        } catch (Exception e) {
            return null;
        }
    }

    private Predicate onlyYearAndMonthAndDay(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String value) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = format.parse(value);
            DateTime dt = new DateTime(startDate.getTime());
            dt = dt.plusDays(1);
            Date endDate = dt.toDate();
            return cb.between(root.get("loginAttemptDate"), startDate, endDate);
        } catch (Exception e) {
            return null;
        }
    }

    private Predicate onlyYearAndMonthAndDayAndHour(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String value) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
            Date startDate = format.parse(value);
            DateTime dt = new DateTime(startDate.getTime());
            dt = dt.plusHours(1);
            Date endDate = dt.toDate();
            return cb.between(root.get("loginAttemptDate"), startDate, endDate);
        } catch (Exception e) {
            return null;
        }
    }

    private Predicate onlyYearAndMonthAndDayAndHourAndMinute(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String value) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date startDate = format.parse(value);
            DateTime dt = new DateTime(startDate.getTime());
            dt = dt.plusMinutes(1);
            Date endDate = dt.toDate();
            return cb.between(root.get("loginAttemptDate"), startDate, endDate);
        } catch (Exception e) {
            return null;
        }
    }

    private static class Filter {
        private final String property;
        private final Object value;

        public Filter(String property, Object value) {
            this.property = property;
            this.value = value;
        }

        public String getProperty() {
            return property;
        }

        public Object getValue() {
            return value;
        }
    }

}
