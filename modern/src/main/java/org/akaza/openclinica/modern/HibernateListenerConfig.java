package org.akaza.openclinica.modern;

import org.akaza.openclinica.core.interceptor.AuditEventListener;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import org.hibernate.SessionFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration("hibernateListenerConfig")
public class HibernateListenerConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public PlatformTransactionManager transactionManager() {
        EntityManagerFactory emf = applicationContext.getBean("entityManagerFactory", EntityManagerFactory.class);
        return new JpaTransactionManager(emf);
    }

    @Bean
    public SessionFactory sessionFactory() {
        EntityManagerFactory emf = applicationContext.getBean("entityManagerFactory", EntityManagerFactory.class);
        return emf.unwrap(SessionFactory.class);
    }

    @Component
    public static class AuditListenerRegistrar implements ApplicationListener<ContextRefreshedEvent> {
        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            EntityManagerFactory entityManagerFactory = event.getApplicationContext().getBean("entityManagerFactory", EntityManagerFactory.class);
            AuditEventListener auditEventListener = null;
            try {
                auditEventListener = event.getApplicationContext().getBean(AuditEventListener.class);
            } catch (Exception e) {
                // Ignore if not present
            }
            if (auditEventListener == null) return;
            
            SessionFactoryImpl sessionFactoryImpl = entityManagerFactory.unwrap(SessionFactoryImpl.class);
            EventListenerRegistry registry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);
            
            registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(auditEventListener);
            registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(auditEventListener);
            registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(auditEventListener);
            registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(auditEventListener);
        }
    }
}
