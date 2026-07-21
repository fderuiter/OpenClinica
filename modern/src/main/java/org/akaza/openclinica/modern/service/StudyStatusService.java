package org.akaza.openclinica.modern.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService;
import org.akaza.openclinica.service.clinical.exception.ClinicalWorkflowException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional("transactionManager")
public class StudyStatusService {

    @PersistenceContext
    private EntityManager entityManager;

    @PreAuthorize("@studySecurityValidator.hasAdminOrCoordinatorRole(#uniqueProtocolID)")
    public void updateStudyStatus(String uniqueProtocolID, int statusId, String newStatus, UserAccountBean userBean) {
        
        // Find study by unique identifier
        Study parentStudy = null;
        try {
            parentStudy = (Study) entityManager.createQuery("SELECT s FROM Study s WHERE s.uniqueIdentifier = :uid")
                    .setParameter("uid", uniqueProtocolID)
                    .getSingleResult();
        } catch (Exception e) {
            throw new IllegalArgumentException("Study not found", e);
        }
        
        Status status = Status.getByCode(statusId);
        
        // Update parent study
        parentStudy.setOldStatusId(parentStudy.getStatus() != null ? parentStudy.getStatus().getCode() : null);
        parentStudy.setStatus(status);
        entityManager.merge(parentStudy);
        
        // Update sites
        List<Study> sites = entityManager.createQuery("SELECT s FROM Study s WHERE s.study = :parent", Study.class)
                .setParameter("parent", parentStudy)
                .getResultList();
        for (Study site : sites) {
            site.setOldStatusId(site.getStatus() != null ? site.getStatus().getCode() : null);
            site.setStatus(status);
            entityManager.merge(site);
        }

        // Update study event definitions (only for parent study)
        List<StudyEventDefinition> defs = entityManager.createQuery("SELECT d FROM StudyEventDefinition d WHERE d.study = :study", StudyEventDefinition.class)
            .setParameter("study", parentStudy)
            .getResultList();
        for (StudyEventDefinition def : defs) {
            def.setStatus(status);
            entityManager.merge(def);
            
            // Update event definition CRFs
            List<EventDefinitionCrf> edcs = entityManager.createQuery("SELECT e FROM EventDefinitionCrf e WHERE e.studyEventDefinition = :def", EventDefinitionCrf.class)
                .setParameter("def", def)
                .getResultList();
            for (EventDefinitionCrf edc : edcs) {
                edc.setStatusId(statusId);
                entityManager.merge(edc);
            }
        }
        
        UnifiedWorkflowEnforcementService workflowService = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean(UnifiedWorkflowEnforcementService.class);
        UserAccount userAccount = entityManager.find(UserAccount.class, userBean.getId());

        // Update subjects and related entities (for parent and all sites)
        List<Study> allStudies = new ArrayList<>();
        allStudies.add(parentStudy);
        allStudies.addAll(sites);
        
        for (Study s : allStudies) {
            List<StudySubject> subjects = entityManager.createQuery("SELECT ss FROM StudySubject ss WHERE ss.study = :study", StudySubject.class)
                .setParameter("study", s)
                .getResultList();
            for (StudySubject subject : subjects) {
                subject.setStatus(status);
                entityManager.merge(subject);
                
                List<StudyEvent> events = entityManager.createQuery("SELECT e FROM StudyEvent e WHERE e.studySubject = :subject", StudyEvent.class)
                    .setParameter("subject", subject)
                    .getResultList();
                for (StudyEvent event : events) {
                    event.setStatusId(statusId);
                    
                    List<EventCrf> eventCrfs = entityManager.createQuery("SELECT e FROM EventCrf e WHERE e.studyEvent = :event", EventCrf.class)
                        .setParameter("event", event)
                        .getResultList();
                    for (EventCrf eventCrf : eventCrfs) {
                        eventCrf.setStatusId(statusId);
                        
                        List<ItemData> itemDatas = entityManager.createQuery("SELECT i FROM ItemData i WHERE i.eventCrf = :eventCrf", ItemData.class)
                            .setParameter("eventCrf", eventCrf)
                            .getResultList();
                        for (ItemData itemData : itemDatas) {
                            itemData.setStatus(status);
                            try {
                                workflowService.saveItemData(itemData, eventCrf, s, userAccount, subject);
                            } catch (ClinicalWorkflowException ex) {
                                throw new ClinicalWorkflowException("ItemData " + itemData.getItemDataId() + " validation failed: " + ex.getMessage());
                            }
                        }
                        
                        try {
                            workflowService.saveEventCrf(eventCrf);
                        } catch (ClinicalWorkflowException ex) {
                            throw new ClinicalWorkflowException("EventCRF " + eventCrf.getEventCrfId() + " validation failed: " + ex.getMessage());
                        }
                    }
                    
                    try {
                        StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(true, false);
                        StudyEventContainer container = new StudyEventContainer(event, changeDetails);
                        workflowService.saveStudyEvent(container);
                    } catch (ClinicalWorkflowException ex) {
                        throw new ClinicalWorkflowException("StudyEvent " + event.getStudyEventId() + " validation failed: " + ex.getMessage());
                    }
                }
            }
        }
    }
}
