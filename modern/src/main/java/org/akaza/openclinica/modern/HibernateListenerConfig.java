package org.akaza.openclinica.modern;

import org.akaza.openclinica.core.interceptor.AuditEventListener;
import org.akaza.openclinica.core.interceptor.AuditHashService;
import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

@Configuration
public class HibernateListenerConfig {

    @Autowired
    private EntityManagerFactory emf;

    @Bean
    public AuditHashService auditHashService() {
        return new AuditHashService();
    }

    @Bean
    public AuditEventListener auditEventListener(AuditHashService auditHashService) {
        AuditEventListener listener = new AuditEventListener();
        listener.setHashService(auditHashService);
        return listener;
    }

    @org.springframework.context.event.EventListener
    public void onApplicationEvent(org.springframework.context.event.ContextRefreshedEvent event) {
        org.hibernate.engine.spi.SessionFactoryImplementor sessionFactory = emf.unwrap(org.hibernate.engine.spi.SessionFactoryImplementor.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        AuditEventListener listener = auditEventListener(auditHashService());

        registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(listener);
        registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(listener);
        registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(listener);
        registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(listener);
    }
}
