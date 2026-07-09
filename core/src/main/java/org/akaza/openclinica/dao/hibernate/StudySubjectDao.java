package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.bean.oid.OidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.bean.oid.StudySubjectOidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.Study;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.StudySubject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class StudySubjectDao extends AbstractDomainDao<StudySubject> {

    @Override
    Class<StudySubject> domainClass() {
        // TODO Auto-generated method stub
        return StudySubject.class;
    }
    
    @SuppressWarnings("unchecked")
    public List<StudySubject> findAllByStudy(Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.study.studyId = :studyid";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyid", studyId);
        return (List<StudySubject>) q.getResultList();
      
    }

    public StudySubject findByOcOID(String OCOID) {
        
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("OCOID", OCOID);
        return (StudySubject) q.getResultList().stream().findFirst().orElse(null);
    }

    public StudySubject findByLabelAndStudy(String embeddedStudySubjectId, Study study) {
        
        String query = "from " + getDomainClassName() + " do  where do.study.studyId = :studyid and do.label = :label";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyid", study.getStudyId());
        q.setParameter("label", embeddedStudySubjectId);
        return (StudySubject) q.getResultList().stream().findFirst().orElse(null);
    }

    public StudySubject findByLabelAndStudyOrParentStudy(String embeddedStudySubjectId, Study study) {
        
        String query = "from " + getDomainClassName() + " do  where (do.study.studyId = :studyid or do.study.study.studyId = :studyid) and do.label = :label";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyid", study.getStudyId());
        q.setParameter("label", embeddedStudySubjectId);
        return (StudySubject) q.getResultList().stream().findFirst().orElse(null);
    }

    public ArrayList<StudySubject> findByLabelAndParentStudy(String embeddedStudySubjectId, Study parentStudy) {
        
        String query = "from " + getDomainClassName() + " do  where do.study.study.studyId = :studyid and do.label = :label";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyid", parentStudy.getStudyId());
        q.setParameter("label", embeddedStudySubjectId);
        return (ArrayList<StudySubject>) q.getResultList();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ArrayList<StudyEvent> fetchListSEs(String id) {
        String query = " from StudyEvent se where se.studySubject.ocOid = :id order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("id", id.toString());

        return (ArrayList<StudyEvent>) q.getResultList();

    }
    public String getValidOid(StudySubject studySubject, ArrayList<String> oidList) {
    OidGenerator oidGenerator = new StudySubjectOidGenerator();
        String oid = getOid(studySubject);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(StudySubject studySubject) {
        OidGenerator oidGenerator = new StudySubjectOidGenerator();
        String oid;
        try {
            oid = studySubject.getOcOid() != null ? studySubject.getOcOid() : oidGenerator.generateOid(studySubject.getLabel());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public int findTheGreatestLabelByStudy(Integer studyId) {
        
        String query = "from " + getDomainClassName() + " do  where (do.study.studyId = :studyid or do.study.study.studyId = :studyid)";

        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("studyid", studyId);
        List<StudySubject> allStudySubjects = (ArrayList<StudySubject>) q.getResultList();
        
        int greatestLabel = 0;
        for (StudySubject subject:allStudySubjects) {
            int labelInt = 0;
            try {
                labelInt = Integer.parseInt(subject.getLabel());
            } catch (NumberFormatException ne) {
                labelInt = 0;
            }
            if (labelInt > greatestLabel) {
                greatestLabel = labelInt;
            }
        }
        return greatestLabel;
    }

}
