package org.akaza.openclinica.dao.core;

public interface SQLDialect {
    String getTrueLiteral();
    String getFalseLiteral();
    String dateConstraint(String startDate, String endDate);
    String resolveDbFolder();
    boolean isOracle();
    boolean isPostgres();
}
