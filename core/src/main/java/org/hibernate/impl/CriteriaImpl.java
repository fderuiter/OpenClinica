package org.hibernate.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CriteriaImpl implements Criteria {
    private EntityManager em;
    private Class<?> entityClass;
    private List<Criterion> criteriaList = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();
    private int firstResult = -1;
    private int maxResults = -1;
    private Projection projection;

    public CriteriaImpl(EntityManager em, Class<?> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    public Criteria add(Criterion criterion) {
        criteriaList.add(criterion);
        return this;
    }

    public Criteria addOrder(Order order) {
        orders.add(order);
        return this;
    }

    public Criteria setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    public Criteria setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public Criteria setProjection(Projection projection) {
        this.projection = projection;
        return this;
    }

    public List getResultList() {
        return list();
    }

    public Object uniqueResult() {
        List results = list();
        return results.isEmpty() ? null : results.get(0);
    }

    public List list() {
        StringBuilder jpql = new StringBuilder();
        if (projection != null) {
            jpql.append("select count(do) from ").append(entityClass.getName()).append(" do ");
        } else {
            jpql.append("select do from ").append(entityClass.getName()).append(" do ");
        }

        Map<String, Object> params = new HashMap<>();
        if (!criteriaList.isEmpty()) {
            jpql.append("where ");
            for (int i = 0; i < criteriaList.size(); i++) {
                if (i > 0) jpql.append(" and ");
                jpql.append(criteriaList.get(i).toSqlString());
                params.putAll(criteriaList.get(i).getParameters());
            }
        }

        if (!orders.isEmpty() && projection == null) {
            jpql.append(" order by ");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) jpql.append(", ");
                jpql.append("do.").append(orders.get(i).toString());
            }
        }

        Query query = em.createQuery(jpql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        if (firstResult >= 0) query.setFirstResult(firstResult);
        if (maxResults >= 0) query.setMaxResults(maxResults);

        return query.getResultList();
    }
}
