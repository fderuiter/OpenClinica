package org.akaza.openclinica.dao.core;

public class OracleSQLDialect implements SQLDialect {
    public String getTrueLiteral() { return " 1 "; }
    public String getFalseLiteral() { return " 0 "; }
    public String dateConstraint(String startDate, String endDate) {
        return " trunc(study_subject.enrollment_date) >= to_date('" + startDate + "') and trunc(study_subject.enrollment_date) <= to_date('" + endDate + "')";
    }
    public String resolveDbFolder() { return "oracle"; }
    public boolean isOracle() { return true; }
    public boolean isPostgres() { return false; }
}
