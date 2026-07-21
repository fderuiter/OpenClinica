package org.akaza.openclinica.modern.service;

import org.akaza.openclinica.modern.model.ConfigurationDraft;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.UUID;
import java.util.List;

@Service
@Transactional("transactionManager")
public class ConfigurationDraftService {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Value("${draft.expiration.hours:24}")
    private int draftExpirationHours;

    public ConfigurationDraft saveDraft(String userName, String draftType, String draftData) {
        String id = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + (draftExpirationHours * 3600000L));

        ConfigurationDraft draft = new ConfigurationDraft();
        draft.setId(id);
        draft.setUserName(userName);
        draft.setDraftType(draftType);
        draft.setDraftData(draftData);
        draft.setCreatedAt(now);
        draft.setExpiresAt(expiresAt);
        
        entityManager.persist(draft);
        
        return draft;
    }

    @Transactional(value="transactionManager", readOnly=true)
    public ConfigurationDraft getDraft(String id) {
        return entityManager.find(ConfigurationDraft.class, id);
    }

    public void updateDraft(String id, String draftData) {
        ConfigurationDraft draft = entityManager.find(ConfigurationDraft.class, id);
        if (draft != null) {
            draft.setDraftData(draftData);
            entityManager.merge(draft);
        }
    }
    
    @Transactional(value="transactionManager", readOnly=true)
    public List<ConfigurationDraft> getDraftsByType(String draftType) {
        return entityManager.createQuery("SELECT d FROM ConfigurationDraft d WHERE d.draftType = :draftType", ConfigurationDraft.class)
            .setParameter("draftType", draftType)
            .getResultList();
    }

    public ConfigurationDraft saveDraftWithId(String id, String userName, String draftType, String draftData) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + (draftExpirationHours * 3600000L));

        ConfigurationDraft draft = entityManager.find(ConfigurationDraft.class, id);
        if (draft != null) {
            draft.setUserName(userName);
            draft.setDraftType(draftType);
            draft.setDraftData(draftData);
            draft.setExpiresAt(expiresAt);
            entityManager.merge(draft);
            return draft;
        } else {
            draft = new ConfigurationDraft();
            draft.setId(id);
            draft.setUserName(userName);
            draft.setDraftType(draftType);
            draft.setDraftData(draftData);
            draft.setCreatedAt(now);
            draft.setExpiresAt(expiresAt);
            entityManager.persist(draft);
            return draft;
        }
    }

    public void deleteDraft(String id) {
        ConfigurationDraft draft = entityManager.find(ConfigurationDraft.class, id);
        if (draft != null) {
            entityManager.remove(draft);
        }
    }
}
