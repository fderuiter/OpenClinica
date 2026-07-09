package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;

import java.util.ArrayList;
import java.util.List;

public class AuditUserLoginSort {
    List<Sort> sorts = new ArrayList<>();

    public void addSort(String property, String order) {
        sorts.add(new Sort(property, order));
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public void execute(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<AuditUserLoginBean> root) {
        List<Order> orders = new ArrayList<>();
        for (Sort sort : sorts) {
            if (sort.getOrder().equals(Sort.ASC)) {
                orders.add(cb.asc(root.get(sort.getProperty())));
            } else if (sort.getOrder().equals(Sort.DESC)) {
                orders.add(cb.desc(root.get(sort.getProperty())));
            }
        }
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }
    }

    public static class Sort {
        public final static String ASC = "asc";
        public final static String DESC = "desc";

        private final String property;
        private final String order;

        public Sort(String property, String order) {
            this.property = property;
            this.order = order;
        }

        public String getProperty() {
            return property;
        }

        public String getOrder() {
            return order;
        }
    }
}
