package org.akaza.openclinica.repository;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.SubjectDao;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.Subject;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.ArrayList;

@Repository
public class UnifiedRepository {

    private DataSource dataSource;
    
    @Autowired(required=false)
    private StudyDao studyDaoHibernate;
    
    @Autowired(required=false)
    private StudySubjectDao studySubjectDaoHibernate;

    @Autowired(required=false)
    private SubjectDao subjectDaoHibernate;

    @Autowired(required=false)
    private CrfDao crfDaoHibernate;

    @Autowired(required=false)
    private EventCrfDao eventCrfDaoHibernate;

    
    @Autowired private org.springframework.beans.factory.ObjectFactory<StudyDAO> studyDaoJdbcFactory;
    @Autowired private org.springframework.beans.factory.ObjectFactory<StudySubjectDAO> studySubjectDaoJdbcFactory;
    @Autowired private org.springframework.beans.factory.ObjectFactory<SubjectDAO> subjectDaoJdbcFactory;
    @Autowired private org.springframework.beans.factory.ObjectFactory<org.akaza.openclinica.dao.admin.CRFDAO> crfDaoJdbcFactory;
    @Autowired private org.springframework.beans.factory.ObjectFactory<org.akaza.openclinica.dao.submit.CRFVersionDAO> crfVersionDaoJdbcFactory;
    @Autowired private org.springframework.beans.factory.ObjectFactory<org.akaza.openclinica.dao.managestudy.StudyEventDAO> studyEventDaoJdbcFactory;
    @Autowired private org.springframework.beans.factory.ObjectFactory<org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO> studyEventDefinitionDaoJdbcFactory;
    @Autowired private org.springframework.beans.factory.ObjectFactory<org.akaza.openclinica.dao.submit.ItemDAO> itemDaoJdbcFactory;
    @Autowired private org.springframework.beans.factory.ObjectFactory<org.akaza.openclinica.dao.submit.ItemDataDAO> itemDataDaoJdbcFactory;
    @Autowired private org.springframework.beans.factory.ObjectFactory<org.akaza.openclinica.dao.submit.ItemFormMetadataDAO> itemFormMetadataDaoJdbcFactory;
 


    @Autowired
    public UnifiedRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void setStudyDaoHibernate(StudyDao studyDaoHibernate) {
        this.studyDaoHibernate = studyDaoHibernate;
    }
    
    public void setStudySubjectDaoHibernate(StudySubjectDao studySubjectDaoHibernate) {
        this.studySubjectDaoHibernate = studySubjectDaoHibernate;
    }

    public void setSubjectDaoHibernate(SubjectDao subjectDaoHibernate) {
        this.subjectDaoHibernate = subjectDaoHibernate;
    }

    public void setCrfDaoHibernate(CrfDao crfDaoHibernate) {
        this.crfDaoHibernate = crfDaoHibernate;
    }

    public void setEventCrfDaoHibernate(EventCrfDao eventCrfDaoHibernate) {
        this.eventCrfDaoHibernate = eventCrfDaoHibernate;
    }

    public org.akaza.openclinica.bean.admin.CRFBean getCrfBeanByVersionId(Integer versionId) { return crfDaoJdbcFactory.getObject().findByVersionId(versionId); }
    public org.akaza.openclinica.bean.admin.CRFBean getCrfBean(Integer id) { return (org.akaza.openclinica.bean.admin.CRFBean) crfDaoJdbcFactory.getObject().findByPK(id); }
    public org.akaza.openclinica.bean.admin.CRFBean getCrfBeanByItemOid(String itemOid) { return crfDaoJdbcFactory.getObject().findByItemOid(itemOid); }
    public org.akaza.openclinica.bean.submit.CRFVersionBean getCrfVersionBean(Integer id) { return (org.akaza.openclinica.bean.submit.CRFVersionBean) crfVersionDaoJdbcFactory.getObject().findByPK(id); }
    public List<org.akaza.openclinica.bean.submit.CRFVersionBean> findAllCrfVersionsByCrf(Integer crfId) { return (List<org.akaza.openclinica.bean.submit.CRFVersionBean>) crfVersionDaoJdbcFactory.getObject().findAllByCRF(crfId); }
    public org.akaza.openclinica.bean.managestudy.StudyEventBean getStudyEventBean(Integer id) { return (org.akaza.openclinica.bean.managestudy.StudyEventBean) studyEventDaoJdbcFactory.getObject().findByPK(id); }
    public List<org.akaza.openclinica.bean.managestudy.StudyEventBean> findAllStudyEventsByDefinition(Integer defId) { return (List<org.akaza.openclinica.bean.managestudy.StudyEventBean>) studyEventDaoJdbcFactory.getObject().findAllByDefinition(defId); }
    public List<org.akaza.openclinica.bean.managestudy.StudyEventBean> findAllStudyEventsByStudyEventDefinitionAndCrfOids(String defOid, String crfOid) { return studyEventDaoJdbcFactory.getObject().findAllByStudyEventDefinitionAndCrfOids(defOid, crfOid); }
    public org.akaza.openclinica.bean.submit.ItemBean getItemBean(Integer id) { return (org.akaza.openclinica.bean.submit.ItemBean) itemDaoJdbcFactory.getObject().findByPK(id); }
    public org.akaza.openclinica.bean.submit.ItemFormMetadataBean getItemFormMetadataBeanByItemIdAndCRFVersionId(Integer itemId, Integer crfVersionId) { return itemFormMetadataDaoJdbcFactory.getObject().findByItemIdAndCRFVersionId(itemId, crfVersionId); }
    public org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean getStudyEventDefinitionBean(Integer id) { return (org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean) studyEventDefinitionDaoJdbcFactory.getObject().findByPK(id); }
    public org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean getStudyEventDefinitionBeanByOid(String oid) { return studyEventDefinitionDaoJdbcFactory.getObject().findByOid(oid); }

    public org.akaza.openclinica.bean.submit.ItemDataBean getItemDataBeanByItemIdAndEventCRFId(Integer itemId, Integer eventCrfId) { return itemDataDaoJdbcFactory.getObject().findByItemIdAndEventCRFId(itemId, eventCrfId); }
    public List<org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean> findAllStudyEventDefinitionsByCrf(org.akaza.openclinica.bean.admin.CRFBean crf) { return (List<org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean>) studyEventDefinitionDaoJdbcFactory.getObject().findAllByCrf(crf); }
    public java.util.HashMap findItemDataCountByStudyEventAndOIDs(Integer studyId, String itemOid, String itemGroupOid) { return itemDataDaoJdbcFactory.getObject().findCountByStudyEventAndOIDs(studyId, itemOid, itemGroupOid); }

    public StudyBean getStudyBean(Integer id) {
        return (StudyBean) studyDaoJdbcFactory.getObject().findByPK(id);
    }

    public Study getStudyEntity(Integer id) {
        return studyDaoHibernate.findById(id);
    }

    public StudySubjectBean getStudySubjectBean(Integer id) {
        return (StudySubjectBean) studySubjectDaoJdbcFactory.getObject().findByPK(id);
    }

    public StudySubject getStudySubjectEntity(Integer id) {
        return studySubjectDaoHibernate.findById(id);
    }
    
    public StudySubject getStudySubjectEntityByOid(String oid) {
        return studySubjectDaoHibernate.findByOcOID(oid);
    }
    
    public SubjectBean getSubjectBeanByUniqueIdentifier(String identifier) {
        return subjectDaoJdbcFactory.getObject().findByUniqueIdentifier(identifier);
    }
    
    // StudySubject methods
    public List<StudySubjectBean> findAllStudySubjectBeansByStudy(StudyBean study) {
        return studySubjectDaoJdbcFactory.getObject().findAllByStudy(study);
    }
    
    public int findTheGreatestStudySubjectLabel() {
        return studySubjectDaoJdbcFactory.getObject().findTheGreatestLabel();
    }
    
    public StudySubjectBean getStudySubjectBeanByLabelAndStudy(String label, StudyBean study) {
        return studySubjectDaoJdbcFactory.getObject().findByLabelAndStudy(label, study);
    }
    
    // Study methods
    public StudyBean getStudyBeanByUniqueIdentifier(String uniqueId) {
        return studyDaoJdbcFactory.getObject().findByUniqueIdentifier(uniqueId);
    }
    
    public StudyBean getSiteBeanByUniqueIdentifier(String studyUniqueId, String siteUniqueId) {
        return studyDaoJdbcFactory.getObject().findSiteByUniqueIdentifier(studyUniqueId, siteUniqueId);
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

    // ==========================================
    // Gatekeeper Write Methods for Core Entities
    // ==========================================

    @Transactional
    public SubjectBean createSubjectBean(SubjectBean subjectBean) {
        return save(subjectBean);
    }
    
    @Transactional
    public StudySubjectBean createStudySubjectBean(StudySubjectBean studySubject) {
        return save(studySubject);
    }

    @Transactional
    public StudyBean save(StudyBean bean) {
        Study entity = mapToEntity(bean);
        if (entity == null) entity = new Study();
        BeanUtils.copyProperties(bean, entity, "id");
        if (bean.getId() > 0) entity.setStudyId(bean.getId());
        entity = studyDaoHibernate.saveOrUpdate(entity);
        studyDaoHibernate.getEntityManager().flush();
        studyDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getStudyId());
        return bean;
    }

    @Transactional
    public SubjectBean save(SubjectBean bean) {
        Subject entity = null;
        if (bean.getId() > 0) {
            entity = subjectDaoHibernate.findById(bean.getId());
        }
        if (entity == null) entity = new Subject();
        BeanUtils.copyProperties(bean, entity, "id");
        if (bean.getId() > 0) entity.setSubjectId(bean.getId());
        entity = subjectDaoHibernate.saveOrUpdate(entity);
        subjectDaoHibernate.getEntityManager().flush();
        subjectDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getSubjectId());
        return bean;
    }

    @Transactional
    public StudySubjectBean save(StudySubjectBean bean) {
        StudySubject entity = mapToEntity(bean);
        if (entity == null) entity = new StudySubject();
        BeanUtils.copyProperties(bean, entity, "id");
        if (bean.getId() > 0) entity.setStudySubjectId(bean.getId());
        entity = studySubjectDaoHibernate.saveOrUpdate(entity);
        studySubjectDaoHibernate.getEntityManager().flush();
        studySubjectDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getStudySubjectId());
        return bean;
    }

    @Transactional
    public org.akaza.openclinica.bean.admin.CRFBean save(org.akaza.openclinica.bean.admin.CRFBean bean) {
        CrfBean entity = null;
        if (bean.getId() > 0) {
            entity = crfDaoHibernate.findById(bean.getId());
        }
        if (entity == null) entity = new CrfBean();
        BeanUtils.copyProperties(bean, entity, "id");
        if (bean.getId() > 0) entity.setCrfId(bean.getId());
        entity = crfDaoHibernate.saveOrUpdate(entity);
        crfDaoHibernate.getEntityManager().flush();
        crfDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getCrfId());
        return bean;
    }

    @Transactional
    public org.akaza.openclinica.bean.submit.EventCRFBean save(org.akaza.openclinica.bean.submit.EventCRFBean bean) {
        EventCrf entity = null;
        if (bean.getId() > 0) {
            entity = eventCrfDaoHibernate.findById(bean.getId());
        }
        if (entity == null) entity = new EventCrf();
        BeanUtils.copyProperties(bean, entity, "id");
        if (bean.getId() > 0) entity.setEventCrfId(bean.getId());
        entity = eventCrfDaoHibernate.saveOrUpdate(entity);
        eventCrfDaoHibernate.getEntityManager().flush();
        eventCrfDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getEventCrfId());
        return bean;
    }
}
