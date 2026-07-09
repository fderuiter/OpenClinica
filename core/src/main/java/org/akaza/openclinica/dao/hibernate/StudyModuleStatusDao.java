package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.managestudy.StudyModuleStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.rule.RuleBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * @author: Shamim
 * Date: Feb 18, 2009
 * Time: 8:01:42 PM
 */
public class StudyModuleStatusDao extends AbstractDomainDao<StudyModuleStatus> {
    @Override
    Class<StudyModuleStatus> domainClass() {
        return StudyModuleStatus.class;
    }

    public StudyModuleStatus findByStudyId(int studyId) {
        String query = "from " + getDomainClassName() + " sms  where sms.studyId = :studyId ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyId", studyId);
        return (StudyModuleStatus) q.getResultList().stream().findFirst().orElse(null);
    }

}
