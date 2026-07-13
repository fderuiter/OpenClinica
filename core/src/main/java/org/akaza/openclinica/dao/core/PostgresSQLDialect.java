package org.akaza.openclinica.dao.core;

public class PostgresSQLDialect implements SQLDialect {
    public String getTrueLiteral() { return " true "; }
    public String getFalseLiteral() { return " false "; }
    public String dateConstraint(String startDate, String endDate) {
        return "(study_subject.enrollment_date is NULL OR ((date(study_subject.enrollment_date) >= date('" + startDate + "')) and (date(study_subject.enrollment_date) <= date('" + endDate + "'))))";
    }
    public String resolveDbFolder() { return "postgres"; }
    public boolean isOracle() { return false; }
    public boolean isPostgres() { return true; }
}
