package org.akaza.openclinica.repository;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.ArrayList;

@Repository
public class UnifiedRepository {

    private DataSource dataSource;
    
    @Autowired(required=false)
    private StudyDao studyDaoHibernate;
    
    @Autowired(required=false)
    private StudySubjectDao studySubjectDaoHibernate;

    private StudyDAO studyDaoJdbc;
    private StudySubjectDAO studySubjectDaoJdbc;
    private SubjectDAO subjectDaoJdbc;

    @Autowired
    public UnifiedRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.studyDaoJdbc = new StudyDAO(dataSource);
        this.studySubjectDaoJdbc = new StudySubjectDAO(dataSource);
        this.subjectDaoJdbc = new SubjectDAO(dataSource);
    }
    
    public void setStudyDaoHibernate(StudyDao studyDaoHibernate) {
        this.studyDaoHibernate = studyDaoHibernate;
    }
    
    public void setStudySubjectDaoHibernate(StudySubjectDao studySubjectDaoHibernate) {
        this.studySubjectDaoHibernate = studySubjectDaoHibernate;
    }

    public StudyBean getStudyBean(Integer id) {
        return (StudyBean) studyDaoJdbc.findByPK(id);
    }

    public Study getStudyEntity(Integer id) {
        return studyDaoHibernate.findById(id);
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
    
    // SubjectBean methods
    public SubjectBean getSubjectBeanByUniqueIdentifier(String identifier) {
        return subjectDaoJdbc.findByUniqueIdentifier(identifier);
    }
    
    public SubjectBean createSubjectBean(SubjectBean subjectBean) {
        return subjectDaoJdbc.create(subjectBean);
    }
    
    // StudySubject methods
    public List<StudySubjectBean> findAllStudySubjectBeansByStudy(StudyBean study) {
        return studySubjectDaoJdbc.findAllByStudy(study);
    }
    
    public StudySubjectBean createStudySubjectBean(StudySubjectBean studySubject) {
        return studySubjectDaoJdbc.createWithoutGroup(studySubject);
    }
    
    public int findTheGreatestStudySubjectLabel() {
        return studySubjectDaoJdbc.findTheGreatestLabel();
    }
    
    public StudySubjectBean getStudySubjectBeanByLabelAndStudy(String label, StudyBean study) {
        return studySubjectDaoJdbc.findByLabelAndStudy(label, study);
    }
    
    // Study methods
    public StudyBean getStudyBeanByUniqueIdentifier(String uniqueId) {
        return studyDaoJdbc.findByUniqueIdentifier(uniqueId);
    }
    
    public StudyBean getSiteBeanByUniqueIdentifier(String studyUniqueId, String siteUniqueId) {
        return studyDaoJdbc.findSiteByUniqueIdentifier(studyUniqueId, siteUniqueId);
    }
    
    // Automatic Mappings
    public StudyBean mapToBean(Study study) {
        if (study == null) return null;
        return getStudyBean(study.getStudyId());
    }
    
    public Study mapToEntity(StudyBean studyBean) {
        if (studyBean == null) return null;
        return getStudyEntity(studyBean.getId());
    }
    
    public StudySubjectBean mapToBean(StudySubject studySubject) {
        if (studySubject == null) return null;
        return getStudySubjectBean(studySubject.getStudySubjectId());
    }
    
    public StudySubject mapToEntity(StudySubjectBean studySubjectBean) {
        if (studySubjectBean == null) return null;
        return getStudySubjectEntity(studySubjectBean.getId());
    }
}
