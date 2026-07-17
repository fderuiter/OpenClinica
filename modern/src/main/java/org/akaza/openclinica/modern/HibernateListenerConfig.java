package org.akaza.openclinica.modern;

import org.akaza.openclinica.core.interceptor.AuditEventListener;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration("hibernateListenerConfig")
public class HibernateListenerConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired(required=false)
    private AuditEventListener auditEventListener;

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public SessionFactory sessionFactory() {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }

    @PostConstruct
    public void registerListeners() {
        if (auditEventListener == null) return;
        SessionFactoryImpl sessionFactoryImpl = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);
        
        registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(auditEventListener);
        registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(auditEventListener);
        registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(auditEventListener);
        registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(auditEventListener);
    }
}
