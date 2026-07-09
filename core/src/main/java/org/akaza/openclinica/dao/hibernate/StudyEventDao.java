package org.akaza.openclinica.dao.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.StudyEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.context.ApplicationEventPublisher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.context.ApplicationEventPublisherAware;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Propagation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class StudyEventDao extends AbstractDomainDao<StudyEvent> implements ApplicationEventPublisherAware{

	private ApplicationEventPublisher eventPublisher;
	private StudyEventChangeDetails changeDetails;

	public Class<StudyEvent> domainClass(){
		return StudyEvent.class;
	}
	public StudyEvent fetchByStudyEventDefOID(String oid,Integer studySubjectId){
		String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
		 jakarta.persistence.Query q = getEntityManager().createQuery(query);
         q.setParameter("studySubjectId", studySubjectId);
         q.setParameter("oid", oid);

         StudyEvent se = (StudyEvent) q.getResultList().stream().findFirst().orElse(null);
        // this.eventPublisher.publishEvent(new OnStudyEventUpdated(se));
         return se;


	}
	
	@Transactional
	public StudyEvent fetchByStudyEventDefOIDAndOrdinal(String oid,Integer ordinal,Integer studySubjectId){
		String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid and se.sampleOrdinal = :ordinal order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
		 jakarta.persistence.Query q = getEntityManager().createQuery(query);
         q.setParameter("studySubjectId", studySubjectId);
         q.setParameter("oid", oid);
         q.setParameter("ordinal", ordinal);
         StudyEvent se = (StudyEvent) q.getResultList().stream().findFirst().orElse(null);
        // this.eventPublisher.publishEvent(new OnStudyEventUpdated(se));
         return se;
	}

    @Transactional(propagation = Propagation.NEVER)
    public StudyEvent fetchByStudyEventDefOIDAndOrdinalTransactional(String oid,Integer ordinal,Integer studySubjectId){
        String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid and se.sampleOrdinal = :ordinal order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studySubjectId", studySubjectId);
        q.setParameter("oid", oid);
        q.setParameter("ordinal", ordinal);
        StudyEvent se = (StudyEvent) q.getResultList().stream().findFirst().orElse(null);
        // this.eventPublisher.publishEvent(new OnStudyEventUpdated(se));
        return se;
    }

    public Integer findMaxOrdinalByStudySubjectStudyEventDefinition(int studySubjectId, int studyEventDefinitionId) {
        String query = "select max(sample_ordinal) from study_event where study_subject_id = " + studySubjectId + " and study_event_definition_id = " + studyEventDefinitionId;
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query);
        Number result = (Number) q.getResultList().stream().findFirst().orElse(null);
        if (result == null) return 0;
        else return result.intValue();
    }


    @Transactional
	public List<StudyEvent> fetchListByStudyEventDefOID(String oid,Integer studySubjectId){
		List<StudyEvent> eventList = null;

		String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
		 jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studySubjectId", studySubjectId);
        q.setParameter("oid", oid);

        eventList = (List<StudyEvent>) q.getResultList();
        return eventList;

	}

	@Transactional
    public StudyEvent saveOrUpdate(StudyEventContainer container) {
        StudyEvent event = saveOrUpdate((StudyEvent) container.getEvent());
        this.eventPublisher.publishEvent(new OnStudyEventUpdated(container));
        return event;
    }

   public StudyEvent saveOrUpdateTransactional(StudyEventContainer container) {
        StudyEvent event = saveOrUpdate((StudyEvent) container.getEvent());
        this.eventPublisher.publishEvent(new OnStudyEventUpdated(container));
        return event;
    }

	@Override
	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
 this.eventPublisher = applicationEventPublisher;
	}

	public void setChangeDetails(StudyEventChangeDetails changeDetails) {
		this.changeDetails = changeDetails;
	}
	
	@Transactional
    public StudyEvent findByStudyEventId(int studyEventId) {
        String query = "from " + getDomainClassName() + " study_event  where study_event.studyEventId = :studyeventid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyeventid", studyEventId);
        return (StudyEvent) q.getResultList().stream().findFirst().orElse(null);
    }

}
