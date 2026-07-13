package org.akaza.openclinica.dao.hibernate;

import java.io.Serializable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.CompositeIdDomainObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Deprecated
public abstract class CompositeIdAbstractDomainDao<T extends CompositeIdDomainObject> {

    @PersistenceContext
    private EntityManager entityManager;

    abstract Class<T> domainClass();

    public String getDomainClassName() {
        return domainClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public ArrayList<T> findAll() {
        String query = "from " + getDomainClassName() + " do";
        Query q = getEntityManager().createQuery(query);
        return new ArrayList<T>((List<T>) q.getResultList());
    }
    
    @Transactional
    public T saveOrUpdate(T domainObject) {
        if (domainObject.getId() == null) {
            getEntityManager().persist(domainObject);
            return domainObject;
        } else {
            return getEntityManager().merge(domainObject);
        }
    }

    @Transactional
    public Serializable save(T domainObject) {
        getEntityManager().persist(domainObject);
        return (Serializable) domainObject.getId();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public T findByColumnName(Object id,String key) {
        String query = "from " + getDomainClassName() + " do where do."+key +"= :param0";
        Query q = getEntityManager().createQuery(query);
        q.setParameter("param0", id);
        List<T> results = q.getResultList();
        if (results.isEmpty()) return null;
        return results.get(0);
    } 
    
    public Long count() {
        return (Long) getEntityManager().createQuery("select count(do) from " + domainClass().getName() + " do").getSingleResult();
    }

    public org.hibernate.Criteria createCriteria(Class clazz) {
        return new org.hibernate.impl.CriteriaImpl(getEntityManager(), clazz);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
