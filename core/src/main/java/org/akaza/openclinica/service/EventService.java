package org.akaza.openclinica.service;

import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudyParameterValueDao;

import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.repository.UnifiedRepository;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;


import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventService implements EventServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO userAccountDao;
    StudyEventDefinitionDAO studyEventDefinitionDao;
    StudyEventDAO studyEventDao;
    DataSource dataSource;

    @Autowired(required = false)
    private UnifiedRepository unifiedRepository;

    @Autowired(required = false)
    private EventCrfDao eventCrfDaoHibernate;
    @Autowired(required = false)
    private StudyEventDao studyEventDaoHibernate;
    @Autowired(required = false)
    private StudyEventDefinitionDao studyEventDefinitionDaoHibernate;
    @Autowired(required = false)
    private EventDefinitionCrfDao eventDefinitionCrfDaoHibernate;
    @Autowired(required = false)
    private StudyParameterValueDao studyParameterValueDaoHibernate;



    public EventService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EventService(SessionManager sessionManager) {
        this.dataSource = sessionManager.getDataSource();
    }

    public HashMap<String, String> scheduleEvent(UserAccountBean user, Date startDateTime, Date endDateTime, String location, String studyUniqueId,
            String siteUniqueId, String eventDefinitionOID, String studySubjectId) throws OpenClinicaSystemException {

        // Business Validation
        StudyBean study = getUnifiedRepository().getStudyBeanByUniqueIdentifier(studyUniqueId);
        int parentStudyId = study.getId();
        if (siteUniqueId != null) {
            study = getUnifiedRepository().getSiteBeanByUniqueIdentifier(studyUniqueId, siteUniqueId);
        }
        StudyEventDefinitionBean studyEventDefinition = getStudyEventDefinitionDao().findByOidAndStudy(eventDefinitionOID, study.getId(), parentStudyId);
        StudySubjectBean studySubject = getUnifiedRepository().getStudySubjectBeanByLabelAndStudy(studySubjectId, study);

        Integer studyEventOrdinal = null;
        if (canSubjectScheduleAnEvent(studyEventDefinition, studySubject)) {

            StudyEventBean studyEvent = new StudyEventBean();
            studyEvent.setStudyEventDefinitionId(studyEventDefinition.getId());
            studyEvent.setStudySubjectId(studySubject.getId());
            studyEvent.setLocation(location);
            studyEvent.setDateStarted(startDateTime);
            studyEvent.setDateEnded(endDateTime);
            studyEvent.setOwner(user);
            studyEvent.setStatus(Status.AVAILABLE);
            studyEvent.setSubjectEventStatus(SubjectEventStatus.SCHEDULED);
            studyEvent.setSampleOrdinal(calculateSampleOrdinal(studyEventDefinition, studySubject));
            studyEvent = (StudyEventBean) getStudyEventDao().create(studyEvent, true);
            studyEventOrdinal = studyEvent.getSampleOrdinal();

        } else {
            throw new OpenClinicaSystemException("Cannot schedule an event for this Subject");
        }

        HashMap<String, String> h = new HashMap<String, String>();
        h.put("eventDefinitionOID", eventDefinitionOID);
        h.put("studyEventOrdinal", studyEventOrdinal.toString());
        h.put("studySubjectOID", studySubject.getOid());
        return h;

    }



    
    public boolean completeParticipantEvent(String studySubjectOid, String studyEventDefOid, Integer ordinal) throws Exception {
        StudySubject subject = getUnifiedRepository().getStudySubjectEntityByOid(studySubjectOid);
        StudyEvent studyEvent = studyEventDaoHibernate.fetchByStudyEventDefOIDAndOrdinal(studyEventDefOid, ordinal, subject.getStudySubjectId());
        StudyEventDefinition studyEventDefinition = studyEventDefinitionDaoHibernate.findByStudyEventDefinitionId(studyEvent.getStudyEventDefinition().getStudyEventDefinitionId());
        Study study = studyEventDefinition.getStudy();
        
        if (!mayProceed(study)) {
            return false;
        }
        
        List<EventDefinitionCrf> eventDefCrfs = eventDefinitionCrfDaoHibernate.findByStudyEventDefinitionId(studyEventDefinition.getStudyEventDefinitionId());
        List<EventCrf> eventCrfs = eventCrfDaoHibernate.findByStudyEventIdStudySubjectId(studyEvent.getStudyEventId(), studySubjectOid);
        
        completeData(studyEvent, eventDefCrfs, eventCrfs);
        return true;
    }

    @Transactional
    public void completeData(StudyEvent studyEvent, List<EventDefinitionCrf> eventDefCrfs, List<EventCrf> eventCrfs) throws Exception {
        boolean completeStudyEvent = true;
        for (EventDefinitionCrf eventDefCrf:eventDefCrfs) {
            boolean foundEventCrfMatch = false;
            for (EventCrf eventCrf:eventCrfs) {
                if (eventDefCrf.getCrf().getCrfId() == eventCrf.getCrfVersion().getCrf().getCrfId()) {
                    foundEventCrfMatch = true;
                    if (eventDefCrf.getParicipantForm()) {
                        eventCrf.setStatusId(org.akaza.openclinica.domain.Status.UNAVAILABLE.getCode());
                        eventCrfDaoHibernate.saveOrUpdate(eventCrf);					
                    } else if (eventCrf.getStatusId() != org.akaza.openclinica.domain.Status.UNAVAILABLE.getCode()) completeStudyEvent = false;
                }
            }
            if (!foundEventCrfMatch && !eventDefCrf.getParicipantForm()) completeStudyEvent = false;
        }
        
        if (completeStudyEvent) {
            studyEvent.setSubjectEventStatusId(4);
            StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(true,false);
            StudyEventContainer container = new StudyEventContainer(studyEvent,changeDetails);
            studyEventDaoHibernate.saveOrUpdateTransactional(container);
        }
    }

    public boolean mayProceed(Study study) throws Exception {
        boolean accessPermission = false;
        StudyParameterValue pStatus = studyParameterValueDaoHibernate.findByStudyIdParameter(study.getStudyId(), "participantPortal");
        ParticipantPortalRegistrar participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOc_oid()).toString();
        String participateStatus = pStatus.getValue().toString();
        String studyStatus = study.getStatus().getName().toString();
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }
        return accessPermission;
    }

    public int getMaxSampleOrdinal(StudyEventDefinitionBean studyEventDefinition, StudySubjectBean studySubject) {
        return getStudyEventDao().getMaxSampleOrdinal(studyEventDefinition, studySubject);
    }

    public int calculateSampleOrdinal(StudyEventDefinitionBean studyEventDefinition, StudySubjectBean studySubject) {
        return getStudyEventDao().getMaxSampleOrdinal(studyEventDefinition, studySubject) + 1;
    }

    public StudyEventBean createStudyEvent(StudyEventBean studyEvent) {
        return (StudyEventBean) getStudyEventDao().create(studyEvent);
    }

    public boolean canSubjectScheduleAnEvent(StudyEventDefinitionBean studyEventDefinition, StudySubjectBean studySubject) {

        if (studyEventDefinition.isRepeating()) {
            return true;
        }
        if (getStudyEventDao().findAllByDefinitionAndSubject(studyEventDefinition, studySubject).size() > 0) {
            return false;
        }
        return true;
    }




    /**
     * @return the UserAccountDao
     */
    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }

    /**
     * @return the StudyEventDefinitionDao
     */
    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        studyEventDefinitionDao = studyEventDefinitionDao != null ? studyEventDefinitionDao : new StudyEventDefinitionDAO(dataSource);
        return studyEventDefinitionDao;
    }

    /**
     * @return the StudyEventDao
     */
    public StudyEventDAO getStudyEventDao() {
        studyEventDao = studyEventDao != null ? studyEventDao : new StudyEventDAO(dataSource);
        return studyEventDao;
    }

    /**
     * @return the datasource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param datasource
     *            the datasource to set
     */
    public void setDatasource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    private org.akaza.openclinica.repository.UnifiedRepository getUnifiedRepository() {
        if (this.unifiedRepository == null) {
            this.unifiedRepository = new org.akaza.openclinica.repository.UnifiedRepository(dataSource);
        }
        return this.unifiedRepository;
    }
}