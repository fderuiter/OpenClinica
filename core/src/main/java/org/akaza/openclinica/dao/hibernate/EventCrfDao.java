package org.akaza.openclinica.dao.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.EventCrf;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class EventCrfDao extends AbstractDomainDao<EventCrf> {

    @Override
    Class<EventCrf> domainClass() {
        // TODO Auto-generated method stub
        return EventCrf.class;
    }

    public EventCrf findByStudyEventIdStudySubjectIdCrfVersionId(int study_event_id, int study_subject_id, int crf_version_id) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crfVersionId = :crfversionid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyeventid", study_event_id);
        q.setParameter("studysubjectid", study_subject_id);
        q.setParameter("crfversionid", crf_version_id);
        return (EventCrf) q.getResultList().stream().findFirst().orElse(null);
    }

    public EventCrf findByStudyEventIdStudySubjectIdCrfId(int study_event_id, int study_subject_id, int crf_id) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crf.crfId = :crfid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyeventid", study_event_id);
        q.setParameter("studysubjectid", study_subject_id);
        q.setParameter("crfid", crf_id);
        return (EventCrf) q.getResultList().stream().findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
	public List<EventCrf> findByStudyEventIdStudySubjectId(Integer studyEventId, String studySubjectOid) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.ocOid= :studysubjectoid";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyeventid", studyEventId);
        q.setParameter("studysubjectoid", studySubjectOid);
        return q.getResultList();
	}
    
    @SuppressWarnings("unchecked")
    public List<EventCrf> findByStudyEventStatus(Integer studyEventId, Integer statusCode) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.statusId = :statusid";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyeventid", studyEventId);
        q.setParameter("statusid", statusCode);
        return q.getResultList();
    }
    
        
}
