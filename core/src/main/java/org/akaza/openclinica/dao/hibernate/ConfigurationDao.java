package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.technicaladmin.ConfigurationBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ConfigurationDao extends AbstractDomainDao<ConfigurationBean> {

    @Override
    public Class<ConfigurationBean> domainClass() {
        return ConfigurationBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ConfigurationBean> findAll() {
        String query = "from " + getDomainClassName();
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        return (ArrayList<ConfigurationBean>) q.getResultList();
    }

    @Transactional
    public ConfigurationBean findByKey(String key) {
        String query = "from " + getDomainClassName() + " do where do.key = :key  ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("key", key);
        return (ConfigurationBean) q.getResultList().stream().findFirst().orElse(null);
    }

}
