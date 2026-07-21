package org.akaza.openclinica.repository;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.SubjectDao;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.Subject;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class SubjectRepository {

    private final SubjectDao subjectDaoHibernate;
    private final StudySubjectDao studySubjectDaoHibernate;
    private final SubjectDAO subjectDaoJdbc;
    private final StudySubjectDAO studySubjectDaoJdbc;

    @Autowired
    public SubjectRepository(DataSource dataSource, SubjectDao subjectDaoHibernate, StudySubjectDao studySubjectDaoHibernate) {
        this.subjectDaoHibernate = subjectDaoHibernate;
        this.studySubjectDaoHibernate = studySubjectDaoHibernate;
        this.subjectDaoJdbc = new SubjectDAO(dataSource);
        this.studySubjectDaoJdbc = new StudySubjectDAO(dataSource);
    }

    public StudySubjectBean getStudySubjectBean(Integer id) {
        return (StudySubjectBean) studySubjectDaoJdbc.findByPK(id);
    }

    public StudySubject getStudySubjectEntity(Integer id) {
        return studySubjectDaoHibernate.findById(id);
    }

    public StudySubject getStudySubjectEntityByOid(String oid) {
        return studySubjectDaoHibernate.findByOcOID(oid);
    }

    public SubjectBean getSubjectBeanByUniqueIdentifier(String identifier) {
        return subjectDaoJdbc.findByUniqueIdentifier(identifier);
    }

    public List<StudySubjectBean> findAllStudySubjectBeansByStudy(StudyBean study) {
        return studySubjectDaoJdbc.findAllByStudy(study);
    }

    public int findTheGreatestStudySubjectLabel() {
        return studySubjectDaoJdbc.findTheGreatestLabel();
    }

    public StudySubjectBean getStudySubjectBeanByLabelAndStudy(String label, StudyBean study) {
        return studySubjectDaoJdbc.findByLabelAndStudy(label, study);
    }

    @Transactional
    public SubjectBean createSubjectBean(SubjectBean subjectBean) {
        return save(subjectBean);
    }

    @Transactional
    public StudySubjectBean createStudySubjectBean(StudySubjectBean studySubject) {
        return save(studySubject);
    }

    @Transactional
    public SubjectBean save(SubjectBean bean) {
        Subject entity = new Subject();
        if (bean.getId() > 0) {
            entity.setSubjectId(bean.getId());
        }
        
        entity.setDateOfBirth(bean.getDateOfBirth());
        if (bean.getGender() != 0) {
            entity.setGender(bean.getGender());
        }
        entity.setUniqueIdentifier(bean.getUniqueIdentifier());
        entity.setDobCollected(bean.isDobCollected());
        entity.setDateCreated(bean.getCreatedDate());
        entity.setDateUpdated(bean.getUpdatedDate());
        
        if (bean.getStatus() != null) {
            entity.setStatus(Status.getByCode(bean.getStatus().getId()));
        }

        if (bean.getOwner() != null) {
            UserAccount owner = new UserAccount();
            owner.setUserId(bean.getOwner().getId());
            entity.setUserAccount(owner);
        }

        if (bean.getUpdater() != null) {
            entity.setUpdateId(bean.getUpdater().getId());
        }

        if (bean.getUpdater() != null) {
            entity.setUpdateId(bean.getUpdater().getId());
        }

        entity = subjectDaoHibernate.saveOrUpdate(entity);
        subjectDaoHibernate.getEntityManager().flush();
        subjectDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getSubjectId());
        return bean;
    }

    @Transactional
    public StudySubjectBean save(StudySubjectBean bean) {
        StudySubject entity = new StudySubject();
        if (bean.getId() > 0) {
            entity.setStudySubjectId(bean.getId());
        }
        
        entity.setLabel(bean.getLabel());
        entity.setSecondaryLabel(bean.getSecondaryLabel());
        entity.setEnrollmentDate(bean.getEnrollmentDate());
        entity.setDateCreated(bean.getCreatedDate());
        entity.setDateUpdated(bean.getUpdatedDate());
        entity.setOcOid(bean.getOid());
        
        if (bean.getStatus() != null) {
            entity.setStatus(Status.getByCode(bean.getStatus().getId()));
        }

        if (bean.getStudyId() > 0) {
            Study study = new Study();
            study.setStudyId(bean.getStudyId());
            entity.setStudy(study);
        }

        if (bean.getSubjectId() > 0) {
            Subject subject = new Subject();
            subject.setSubjectId(bean.getSubjectId());
            entity.setSubject(subject);
        }

        if (bean.getOwner() != null) {
            UserAccount owner = new UserAccount();
            owner.setUserId(bean.getOwner().getId());
            entity.setUserAccount(owner);
        }

        if (bean.getUpdater() != null) {
            entity.setUpdateId(bean.getUpdater().getId());
        }

        if (bean.getUpdater() != null) {
            entity.setUpdateId(bean.getUpdater().getId());
        }

        entity = studySubjectDaoHibernate.saveOrUpdate(entity);
        studySubjectDaoHibernate.getEntityManager().flush();
        studySubjectDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getStudySubjectId());
        return bean;
    }
}
