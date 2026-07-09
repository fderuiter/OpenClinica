package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;


public class StudyParameterValueDao extends AbstractDomainDao<StudyParameterValue> {
	
    @Override
    public Class<StudyParameterValue> domainClass() {
        return StudyParameterValue.class;
    }

	public StudyParameterValue findByStudyIdParameter(int studyId, String parameter) {
        String query = "from " + getDomainClassName() + " study_parameter_value where study_parameter_value.study.studyId = :studyid and study_parameter_value.studyParameter = :parameter ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyid", studyId);
        q.setParameter("parameter", parameter);
        return (StudyParameterValue) q.getResultList().stream().findFirst().orElse(null);
    }
}
