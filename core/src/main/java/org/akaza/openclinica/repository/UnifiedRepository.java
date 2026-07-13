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
    private org.akaza.openclinica.dao.admin.CRFDAO crfDaoJdbc;
    private org.akaza.openclinica.dao.submit.CRFVersionDAO crfVersionDaoJdbc;
    private org.akaza.openclinica.dao.managestudy.StudyEventDAO studyEventDaoJdbc;
    private org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO studyEventDefinitionDaoJdbc;
    private org.akaza.openclinica.dao.submit.ItemDAO itemDaoJdbc;
    private org.akaza.openclinica.dao.submit.ItemDataDAO itemDataDaoJdbc;
    private org.akaza.openclinica.dao.submit.ItemFormMetadataDAO itemFormMetadataDaoJdbc; 


    @Autowired
    public UnifiedRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.studyDaoJdbc = new StudyDAO(dataSource);
        this.studySubjectDaoJdbc = new StudySubjectDAO(dataSource);
        this.subjectDaoJdbc = new SubjectDAO(dataSource);
        this.crfDaoJdbc = new org.akaza.openclinica.dao.admin.CRFDAO(dataSource);
        this.crfVersionDaoJdbc = new org.akaza.openclinica.dao.submit.CRFVersionDAO(dataSource);
        this.studyEventDaoJdbc = new org.akaza.openclinica.dao.managestudy.StudyEventDAO(dataSource);
        this.studyEventDefinitionDaoJdbc = new org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO(dataSource);
        this.itemDaoJdbc = new org.akaza.openclinica.dao.submit.ItemDAO(dataSource);
        this.itemDataDaoJdbc = new org.akaza.openclinica.dao.submit.ItemDataDAO(dataSource);
        this.itemFormMetadataDaoJdbc = new org.akaza.openclinica.dao.submit.ItemFormMetadataDAO(dataSource);

    }
    
    public void setStudyDaoHibernate(StudyDao studyDaoHibernate) {
        this.studyDaoHibernate = studyDaoHibernate;
    }
    
    public void setStudySubjectDaoHibernate(StudySubjectDao studySubjectDaoHibernate) {
        this.studySubjectDaoHibernate = studySubjectDaoHibernate;
    }

    
    
    
    
    
    
    
    

    public org.akaza.openclinica.bean.admin.CRFBean getCrfBeanByVersionId(Integer versionId) { return crfDaoJdbc.findByVersionId(versionId); }
    public org.akaza.openclinica.bean.admin.CRFBean getCrfBean(Integer id) { return (org.akaza.openclinica.bean.admin.CRFBean) crfDaoJdbc.findByPK(id); }
    public org.akaza.openclinica.bean.admin.CRFBean getCrfBeanByItemOid(String itemOid) { return crfDaoJdbc.findByItemOid(itemOid); }
    public org.akaza.openclinica.bean.submit.CRFVersionBean getCrfVersionBean(Integer id) { return (org.akaza.openclinica.bean.submit.CRFVersionBean) crfVersionDaoJdbc.findByPK(id); }
    public List<org.akaza.openclinica.bean.submit.CRFVersionBean> findAllCrfVersionsByCrf(Integer crfId) { return (List<org.akaza.openclinica.bean.submit.CRFVersionBean>) crfVersionDaoJdbc.findAllByCRF(crfId); }
    public org.akaza.openclinica.bean.managestudy.StudyEventBean getStudyEventBean(Integer id) { return (org.akaza.openclinica.bean.managestudy.StudyEventBean) studyEventDaoJdbc.findByPK(id); }
    public List<org.akaza.openclinica.bean.managestudy.StudyEventBean> findAllStudyEventsByDefinition(Integer defId) { return (List<org.akaza.openclinica.bean.managestudy.StudyEventBean>) studyEventDaoJdbc.findAllByDefinition(defId); }
    public List<org.akaza.openclinica.bean.managestudy.StudyEventBean> findAllStudyEventsByStudyEventDefinitionAndCrfOids(String defOid, String crfOid) { return studyEventDaoJdbc.findAllByStudyEventDefinitionAndCrfOids(defOid, crfOid); }
    public org.akaza.openclinica.bean.submit.ItemBean getItemBean(Integer id) { return (org.akaza.openclinica.bean.submit.ItemBean) itemDaoJdbc.findByPK(id); }
    public org.akaza.openclinica.bean.submit.ItemFormMetadataBean getItemFormMetadataBeanByItemIdAndCRFVersionId(Integer itemId, Integer crfVersionId) { return itemFormMetadataDaoJdbc.findByItemIdAndCRFVersionId(itemId, crfVersionId); }
    public org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean getStudyEventDefinitionBean(Integer id) { return (org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean) studyEventDefinitionDaoJdbc.findByPK(id); }
    public org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean getStudyEventDefinitionBeanByOid(String oid) { return studyEventDefinitionDaoJdbc.findByOid(oid); }

    public org.akaza.openclinica.bean.submit.ItemDataBean getItemDataBeanByItemIdAndEventCRFId(Integer itemId, Integer eventCrfId) { return itemDataDaoJdbc.findByItemIdAndEventCRFId(itemId, eventCrfId); }
    public List<org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean> findAllStudyEventDefinitionsByCrf(org.akaza.openclinica.bean.admin.CRFBean crf) { return (List<org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean>) studyEventDefinitionDaoJdbc.findAllByCrf(crf); }
    public java.util.HashMap findItemDataCountByStudyEventAndOIDs(Integer studyId, String itemOid, String itemGroupOid) { return itemDataDaoJdbc.findCountByStudyEventAndOIDs(studyId, itemOid, itemGroupOid); }

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
