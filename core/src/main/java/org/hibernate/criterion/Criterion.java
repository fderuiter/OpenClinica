package org.hibernate.criterion;
public interface Criterion {
    String toSqlString();
    java.util.Map<String, Object> getParameters();
}
