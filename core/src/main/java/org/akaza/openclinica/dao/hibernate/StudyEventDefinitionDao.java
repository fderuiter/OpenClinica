package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;


public class StudyEventDefinitionDao extends AbstractDomainDao<StudyEventDefinition> {
	
    @Override
    public Class<StudyEventDefinition> domainClass() {
        return StudyEventDefinition.class;
    }
    
    public StudyEventDefinition findByStudyEventDefinitionId(int studyEventDefinitionId) {
        String query = "from " + getDomainClassName() + " study_event_definition  where study_event_definition.studyEventDefinitionId = :studyeventdefinitionid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyeventdefinitionid", studyEventDefinitionId);
        return (StudyEventDefinition) q.getResultList().stream().findFirst().orElse(null);
    }

}
