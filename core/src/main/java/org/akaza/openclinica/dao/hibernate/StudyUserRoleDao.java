package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.StudyUserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.user.UserAccount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class StudyUserRoleDao extends CompositeIdAbstractDomainDao<StudyUserRole> {

    @Override
    public Class<StudyUserRole> domainClass() {
        return StudyUserRole.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<StudyUserRole> findAllUserRolesByUserAccount(UserAccount userAccount, int studyId, int parentStudyId) {
        String query = "from " + getDomainClassName()
                + "   where   user_name=:username  AND  status_id=1  AND  ( study_id=:studyId OR study_id=:parentStudyId) ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("username", userAccount.getUserName());
        q.setParameter("studyId", studyId);
        q.setParameter("parentStudyId", parentStudyId);
        return (ArrayList<StudyUserRole>) q.getResultList();
    }

}
