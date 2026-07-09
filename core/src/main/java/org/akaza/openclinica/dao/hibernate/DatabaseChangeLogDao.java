package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.technicaladmin.DatabaseChangeLogBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.SessionFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class DatabaseChangeLogDao {

    private SessionFactory sessionFactory;

    public String getDomainClassName() {
        return domainClass().getName();
    }

    public Class<DatabaseChangeLogBean> domainClass() {
        return DatabaseChangeLogBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<DatabaseChangeLogBean> findAll() {
        String query = "from " + getDomainClassName() + " dcl order by dcl.id desc ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        return (ArrayList<DatabaseChangeLogBean>) q.getResultList();
    }

    public DatabaseChangeLogBean findById(String id, String author, String fileName) {
        String query = "from " + getDomainClassName() + " do  where do.id = :id and do.author = :author and do.fileName = :fileName ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("id", id);
        q.setParameter("author", author);
        q.setParameter("fileName", fileName);
        return (DatabaseChangeLogBean) q.getResultList().stream().findFirst().orElse(null);
    }

    public Long count() {
        return (Long) getEntityManager().createQuery("select count(*) from " + domainClass().getName()).getResultList().stream().findFirst().orElse(null);
    }

    /**
     * @return the sessionFactory
     */
    public EntityManager getEntityManager() { return sessionFactory.getCurrentSession(); }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @param sessionFactory
     *            the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @return Session Object
     */
}
