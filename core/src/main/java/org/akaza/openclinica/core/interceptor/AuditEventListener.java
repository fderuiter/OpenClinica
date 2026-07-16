package org.akaza.openclinica.core.interceptor;

import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.AuditLogEventType;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.StatelessSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.Arrays;

import org.hibernate.persister.entity.EntityPersister;

public class AuditEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, PreInsertEventListener {
    
    private AuditHashService hashService;
    
    public void setHashService(AuditHashService hashService) {
        this.hashService = hashService;
    }
    
    private final Object lock = new Object();

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        if (event.getEntity() instanceof AuditLogEvent) {
            AuditLogEvent auditEvent = (AuditLogEvent) event.getEntity();
            synchronized (lock) {
                StatelessSession session = event.getPersister().getFactory().openStatelessSession();
                try {
                    Query<AuditLogEvent> q = session.createQuery("FROM AuditLogEvent ORDER BY auditId DESC", AuditLogEvent.class);
                    q.setMaxResults(1);
                    AuditLogEvent prev = q.uniqueResult();
                    String prevHash = prev != null ? prev.getChainHash() : null;
                    
                    String newHash = hashService.computeHash(auditEvent, prevHash);
                    auditEvent.setChainHash(newHash);
                    
                    // Update the state array so Hibernate saves the new value
                    String[] propertyNames = event.getPersister().getPropertyNames();
                    int chainHashIndex = Arrays.asList(propertyNames).indexOf("chainHash");
                    if (chainHashIndex != -1) {
                        event.getState()[chainHashIndex] = newHash;
                    }
                } finally {
                    session.close();
                }
            }
        }
        return false; // Return false to not veto the insert
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        handleAudit(event.getEntity(), "INSERT", event.getPersister().getFactory().openStatelessSession());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        handleAudit(event.getEntity(), "UPDATE", event.getPersister().getFactory().openStatelessSession());
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        handleAudit(event.getEntity(), "DELETE", event.getPersister().getFactory().openStatelessSession());
    }

    private void handleAudit(Object entity, String action, StatelessSession session) {
        if (entity instanceof AuditLogEvent) {
            session.close();
            return;
        }
        
        String auditTable = null;
        Integer entityId = null;
        if (entity instanceof ItemData) {
            auditTable = "item_data";
            entityId = ((ItemData) entity).getItemDataId();
        } else if (entity instanceof EventCrf) {
            auditTable = "event_crf";
            entityId = ((EventCrf) entity).getEventCrfId();
        } else if (entity instanceof StudySubject) {
            auditTable = "study_subject";
            entityId = ((StudySubject) entity).getStudySubjectId();
        }
        
        if (auditTable == null) {
            session.close();
            return;
        }

        try {
            AuditLogEvent auditEvent = new AuditLogEvent();
            auditEvent.setAuditDate(new Date());
            auditEvent.setAuditTable(auditTable);
            auditEvent.setEntityId(entityId);
            auditEvent.setEntityName(auditTable);
            auditEvent.setReasonForChange(action);
            
            AuditLogEventType eventType = new AuditLogEventType();
            eventType.setAuditLogEventTypeId(1);
            auditEvent.setAuditLogEventType(eventType);
            
            // onPreInsert doesn't fire for StatelessSession.insert(), so we calculate here as well
            // Or we can use regular Session to fire events.
            synchronized (lock) {
                Query<AuditLogEvent> q = session.createQuery("FROM AuditLogEvent ORDER BY auditId DESC", AuditLogEvent.class);
                q.setMaxResults(1);
                AuditLogEvent prev = q.uniqueResult();
                String prevHash = prev != null ? prev.getChainHash() : null;
                
                String newHash = hashService.computeHash(auditEvent, prevHash);
                auditEvent.setChainHash(newHash);
                session.insert(auditEvent);
            }
        } finally {
            session.close();
        }
    }
}
