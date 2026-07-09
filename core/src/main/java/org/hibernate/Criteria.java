package org.hibernate;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;

import java.util.List;

public interface Criteria {
    Criteria add(Criterion criterion);
    Criteria addOrder(Order order);
    Criteria setFirstResult(int firstResult);
    Criteria setMaxResults(int maxResults);
    Criteria setProjection(Projection projection);
    List getResultList();
    Object uniqueResult();
    List list();
}
