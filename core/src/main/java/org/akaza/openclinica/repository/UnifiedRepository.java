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


    private org.akaza.openclinica.dao.login.UserAccountDAO userAccountDaoJdbc;

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
        this.userAccountDaoJdbc = new org.akaza.openclinica.dao.login.UserAccountDAO(dataSource);
    }
    
    public org.akaza.openclinica.bean.login.UserAccountBean getUserAccountBeanByUserName(String userName) {
        return (org.akaza.openclinica.bean.login.UserAccountBean) userAccountDaoJdbc.findByUserName(userName);
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
    
    public SubjectBean getSubjectBeanByUniqueIdentifier(String identifier) {
        return subjectDaoJdbc.findByUniqueIdentifier(identifier);
    }
    
    // StudySubject methods
    public List<StudySubjectBean> findAllStudySubjectBeansByStudy(StudyBean study) {
        return studySubjectDaoJdbc.findAllByStudy(study);
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
    
    public Study mapToEntity(StudyBean bean) {
        if (bean == null) return null;
        Study entity = new Study();
        entity.setStudyId(bean.getId());
        entity.setName(bean.getName());
        entity.setUniqueIdentifier(bean.getIdentifier());
        entity.setSecondaryIdentifier(bean.getSecondaryIdentifier());
        entity.setSummary(bean.getSummary());
        entity.setDatePlannedStart(bean.getDatePlannedStart());
        entity.setDatePlannedEnd(bean.getDatePlannedEnd());
        entity.setPrincipalInvestigator(bean.getPrincipalInvestigator());
        entity.setFacilityName(bean.getFacilityName());
        entity.setFacilityCity(bean.getFacilityCity());
        entity.setFacilityState(bean.getFacilityState());
        entity.setFacilityZip(bean.getFacilityZip());
        entity.setFacilityCountry(bean.getFacilityCountry());
        entity.setFacilityRecruitmentStatus(bean.getFacilityRecruitmentStatus());
        entity.setFacilityContactName(bean.getFacilityContactName());
        entity.setFacilityContactDegree(bean.getFacilityContactDegree());
        entity.setFacilityContactPhone(bean.getFacilityContactPhone());
        entity.setFacilityContactEmail(bean.getFacilityContactEmail());
        entity.setProtocolType(bean.getProtocolType());
        entity.setProtocolDescription(bean.getProtocolDescription());
        entity.setProtocolDateVerification(bean.getProtocolDateVerification());
        entity.setPhase(bean.getPhase());
        entity.setExpectedTotalEnrollment(bean.getExpectedTotalEnrollment());
        entity.setSponsor(bean.getSponsor());
        entity.setCollaborators(bean.getCollaborators());
        entity.setMedlineIdentifier(bean.getMedlineIdentifier());
        entity.setUrl(bean.getUrl());
        entity.setUrlDescription(bean.getUrlDescription());
        entity.setConditions(bean.getConditions());
        entity.setKeywords(bean.getKeywords());
        entity.setEligibility(bean.getEligibility());
        entity.setGender(bean.getGender());
        entity.setAgeMax(bean.getAgeMax());
        entity.setAgeMin(bean.getAgeMin());
        entity.setHealthyVolunteerAccepted(bean.getHealthyVolunteerAccepted());
        entity.setPurpose(bean.getPurpose());
        entity.setAllocation(bean.getAllocation());
        entity.setMasking(bean.getMasking());
        entity.setControl(bean.getControl());
        entity.setAssignment(bean.getAssignment());
        entity.setEndpoint(bean.getEndpoint());
        entity.setInterventions(bean.getInterventions());
        entity.setDuration(bean.getDuration());
        entity.setSelection(bean.getSelection());
        entity.setTiming(bean.getTiming());
        entity.setOfficialTitle(bean.getOfficialTitle());
        entity.setResultsReference(bean.isResultsReference());
        entity.setOc_oid(bean.getOid());
        entity.setDateCreated(bean.getCreatedDate());
        entity.setDateUpdated(bean.getUpdatedDate());
        if (bean.getUpdater() != null) entity.setUpdateId(bean.getUpdater().getId());
        return entity;
    }
    
    public StudySubjectBean mapToBean(StudySubject studySubject) {
        if (studySubject == null) return null;
        return getStudySubjectBean(studySubject.getStudySubjectId());
    }
    
    public StudySubject mapToEntity(StudySubjectBean bean) {
        if (bean == null) return null;
        StudySubject entity = new StudySubject();
        entity.setStudySubjectId(bean.getId());
        entity.setLabel(bean.getLabel());
        entity.setSecondaryLabel(bean.getSecondaryLabel());
        entity.setEnrollmentDate(bean.getEnrollmentDate());
        entity.setDateCreated(bean.getCreatedDate());
        entity.setDateUpdated(bean.getUpdatedDate());
        entity.setOcOid(bean.getOid());
        if (bean.getUpdater() != null) entity.setUpdateId(bean.getUpdater().getId());
        return entity;
    }

    public Subject mapToEntity(SubjectBean bean) {
        if (bean == null) return null;
        Subject entity = new Subject();
        entity.setSubjectId(bean.getId());
        entity.setDateOfBirth(bean.getDateOfBirth());
        entity.setGender(bean.getGender());
        entity.setUniqueIdentifier(bean.getUniqueIdentifier());
        entity.setDobCollected(bean.isDobCollected());
        entity.setDateCreated(bean.getCreatedDate());
        entity.setDateUpdated(bean.getUpdatedDate());
        if (bean.getUpdater() != null) entity.setUpdateId(bean.getUpdater().getId());
        return entity;
    }

    public CrfBean mapToEntity(org.akaza.openclinica.bean.admin.CRFBean bean) {
        if (bean == null) return null;
        CrfBean entity = new CrfBean();
        entity.setCrfId(bean.getId());
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setOcOid(bean.getOid());
        entity.setDateCreated(bean.getCreatedDate());
        entity.setDateUpdated(bean.getUpdatedDate());
        if (bean.getUpdater() != null) entity.setUpdateId(bean.getUpdater().getId());
        return entity;
    }

    public EventCrf mapToEntity(org.akaza.openclinica.bean.submit.EventCRFBean bean) {
        if (bean == null) return null;
        EventCrf entity = new EventCrf();
        entity.setEventCrfId(bean.getId());
        entity.setDateInterviewed(bean.getDateInterviewed());
        entity.setInterviewerName(bean.getInterviewerName());
        entity.setAnnotations(bean.getAnnotations());
        entity.setDateCompleted(bean.getDateCompleted());
        entity.setValidatorId(bean.getValidatorId());
        entity.setDateValidate(bean.getDateValidate());
        entity.setDateValidateCompleted(bean.getDateValidateCompleted());
        entity.setValidatorAnnotations(bean.getValidatorAnnotations());
        entity.setValidateString(bean.getValidateString());
        entity.setElectronicSignatureStatus(bean.isElectronicSignatureStatus());
        entity.setSdvStatus(bean.isSdvStatus());
        entity.setSdvUpdateId(bean.getSdvUpdateId());
        entity.setDateCreated(bean.getCreatedDate());
        entity.setDateUpdated(bean.getUpdatedDate());
        if (bean.getUpdater() != null) entity.setUpdateId(bean.getUpdater().getId());
        return entity;
    }

    private void checkInitialization() {
        if (studyDaoHibernate == null || subjectDaoHibernate == null || studySubjectDaoHibernate == null || crfDaoHibernate == null || eventCrfDaoHibernate == null) {
            throw new IllegalStateException("Persistence modules not fully initialized");
        }
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
        checkInitialization();
        Study entity = mapToEntity(bean);
        entity = studyDaoHibernate.saveOrUpdate(entity);
        studyDaoHibernate.getEntityManager().flush();
        studyDaoHibernate.getEntityManager().detach(entity);
        bean.setId(entity.getStudyId());
        return bean;
    }

    @Transactional
    public SubjectBean save(SubjectBean bean) {
        checkInitialization();
        Subject entity = mapToEntity(bean);
        entity = subjectDaoHibernate.saveOrUpdate(entity);
        subjectDaoHibernate.getEntityManager().flush();
        subjectDaoHibernate.getEntityManager().detach(entity);
        bean.setId(entity.getSubjectId());
        return bean;
    }

    @Transactional
    public StudySubjectBean save(StudySubjectBean bean) {
        checkInitialization();
        StudySubject entity = mapToEntity(bean);
        entity = studySubjectDaoHibernate.saveOrUpdate(entity);
        studySubjectDaoHibernate.getEntityManager().flush();
        studySubjectDaoHibernate.getEntityManager().detach(entity);
        bean.setId(entity.getStudySubjectId());
        return bean;
    }

    @Transactional
    public org.akaza.openclinica.bean.admin.CRFBean save(org.akaza.openclinica.bean.admin.CRFBean bean) {
        checkInitialization();
        CrfBean entity = mapToEntity(bean);
        entity = crfDaoHibernate.saveOrUpdate(entity);
        crfDaoHibernate.getEntityManager().flush();
        crfDaoHibernate.getEntityManager().detach(entity);
        bean.setId(entity.getCrfId());
        return bean;
    }

    @Transactional
    public org.akaza.openclinica.bean.submit.EventCRFBean save(org.akaza.openclinica.bean.submit.EventCRFBean bean) {
        checkInitialization();
        EventCrf entity = mapToEntity(bean);
        entity = eventCrfDaoHibernate.saveOrUpdate(entity);
        eventCrfDaoHibernate.getEntityManager().flush();
        eventCrfDaoHibernate.getEntityManager().detach(entity);
        bean.setId(entity.getEventCrfId());
        return bean;
    }
}
