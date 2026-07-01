package org.akaza.openclinica.bean.extract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatasetQueryBuilder {
    private String viewName;
    private List<String> conditions = new ArrayList<>();
    private String orderBy;
    private boolean isOracle = false;

    public DatasetQueryBuilder(String viewName, boolean isOracle) {
        this.viewName = viewName;
        this.isOracle = isOracle;
    }

    public DatasetQueryBuilder addInClause(String column, List<?> values) {
        if (values != null && !values.isEmpty()) {
            String idList = values.toString().replaceAll("\\[|\\]", "");
            conditions.add(column + " in (" + idList + ")");
        }
        return this;
    }

    public DatasetQueryBuilder addDateRangeClause(String column, Date start, Date end) {
        String pattern = isOracle ? "dd-MMM-yyyy" : "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        
        String beginDate = isOracle ? "" : "1900-01-01";
        if (start != null) {
            beginDate = sdf.format(start);
        }
        
        String stopDate = isOracle ? "" : "2100-01-01";
        if (end != null) {
            stopDate = sdf.format(end);
        }

        if (isOracle) {
            conditions.add("(" + column + " >= '" + beginDate + "') and (" + column + " <= '" + stopDate + "')");
        } else {
            conditions.add("(date(" + column + ") >= date('" + beginDate + "')) and (date(" + column + ") <= date('" + stopDate + "'))");
        }
        return this;
    }

    public DatasetQueryBuilder setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("select distinct * from ").append(viewName);
        if (!conditions.isEmpty()) {
            sb.append(" where ");
            sb.append(String.join(" and ", conditions));
        }
        if (orderBy != null) {
            sb.append(" order by ").append(orderBy);
        }
        return sb.toString();
    }
}
