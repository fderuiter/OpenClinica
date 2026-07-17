package org.akaza.openclinica.service;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import jakarta.servlet.ServletContext;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.service.ParticipantEventService;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.akaza.openclinica.service.audit.AuditService;
import org.akaza.openclinica.bean.admin.AuditEventBean;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cdisc.ns.odm.v130_api.ODM;
import org.cdisc.ns.odm.v130_api.ODMcomplexTypeDefinitionClinicalData;
import org.cdisc.ns.odm.v130_api.ODMcomplexTypeDefinitionFormData;
import org.cdisc.ns.odm.v130_api.ODMcomplexTypeDefinitionStudyEventData;
import org.cdisc.ns.odm.v130_api.ODMcomplexTypeDefinitionSubjectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OdmService {
    private CRFDAO _cRFDAO;
    private CRFVersionDAO _cRFVersionDAO;
    private EventCRFDAO _eventCRFDAO;
    private ItemDataDAO _itemDataDAO;
    private StudyDAO _studyDAO;
    private StudyEventDefinitionDAO _studyEventDefinitionDAO;
    private StudyParameterValueDAO _studyParameterValueDAO;
    private StudySubjectDAO _studySubjectDAO;

    @Autowired
    public OdmService(CRFDAO _cRFDAO, CRFVersionDAO _cRFVersionDAO, EventCRFDAO _eventCRFDAO, ItemDataDAO _itemDataDAO, StudyDAO _studyDAO, StudyEventDefinitionDAO _studyEventDefinitionDAO, StudyParameterValueDAO _studyParameterValueDAO, StudySubjectDAO _studySubjectDAO) {
        this._cRFDAO = _cRFDAO;
        this._cRFVersionDAO = _cRFVersionDAO;
        this._eventCRFDAO = _eventCRFDAO;
        this._itemDataDAO = _itemDataDAO;
        this._studyDAO = _studyDAO;
        this._studyEventDefinitionDAO = _studyEventDefinitionDAO;
        this._studyParameterValueDAO = _studyParameterValueDAO;
        this._studySubjectDAO = _studySubjectDAO;
    }


    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    private EventCrfDao eventCrfDao;

    public static final String FORM_CONTEXT = "ecid";
    private ParticipantPortalRegistrar participantPortalRegistrar;
    private StudyDAO sdao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Transactional
    public ODM getODM(String studyOID, String subjectKey, ServletContext context) throws Exception {
        ODM odm = new ODM();
        String ssoid = subjectKey;
        if (ssoid == null) {
            return null;
        }

        CRFVersionDAO versionDAO = this._cRFVersionDAO;
        StudyDAO studyDAO = this._studyDAO;
        StudySubjectDAO studySubjectDAO = this._studySubjectDAO;
        EventCRFDAO eventCRFDAO = this._eventCRFDAO;
        ItemDataDAO itemDataDAO = this._itemDataDAO;
        CRFDAO crfDAO = this._cRFDAO;
        List<ODMcomplexTypeDefinitionFormData> formDatas = new ArrayList<>();
        try {
            StudySubjectBean studySubjectBean = studySubjectDAO.findByOid(ssoid);
            ParticipantEventService participantEventService = new ParticipantEventService(dataSource);
            StudyEventBean nextEvent = participantEventService.getNextParticipantEvent(studySubjectBean);
            if (nextEvent != null) {
                logger.debug("Found event: " + nextEvent.getName() + " - ID: " + nextEvent.getId());

                List<EventCRFBean> eventCrfs = eventCRFDAO.findAllByStudyEvent(nextEvent);
                StudyBean study = studyDAO.findByOid(studyOID);
                if (!mayProceed(studyOID, studySubjectBean))
                    return odm;

                List<EventDefinitionCRFBean> eventDefCrfs = participantEventService.getEventDefCrfsForStudyEvent(studySubjectBean, nextEvent);
                for (EventDefinitionCRFBean eventDefCrf:eventDefCrfs) {
                    if (eventDefCrf.isParticipantForm()) {
                        EventCRFBean eventCRF = participantEventService.getExistingEventCRF(studySubjectBean, nextEvent, eventDefCrf);
                        boolean itemDataExists = false;
                        boolean validStatus = true;
                        CRFVersionBean crfVersion = null;
                        if (eventCRF!=null) {
                            if (eventCRF.getStatus().getId() != 1 && eventCRF.getStatus().getId() != 2)
                                validStatus = false;
                            if (itemDataDAO.findAllByEventCRFId(eventCRF.getId()).size() > 0)
                                itemDataExists = true;
                            crfVersion = (CRFVersionBean) versionDAO.findByPK(eventCRF.getCRFVersionId());
                        } else crfVersion = (CRFVersionBean) versionDAO.findByPK(eventDefCrf.getDefaultVersionId());

                        if (validStatus) {
                            String formUrl = null;
                            if (!itemDataExists)
                                formUrl = createEnketoUrl(studyOID, crfVersion, nextEvent, ssoid, context);
                            else
                                formUrl = createEditUrl(studyOID, crfVersion, nextEvent, ssoid, context);
                            formDatas.add(getFormDataPerCrf(crfVersion, nextEvent, eventCrfs, crfDAO, formUrl, itemDataExists));
                        }
                    }
                }
                return createOdm(study, studySubjectBean, nextEvent, formDatas);
            } else {
                logger.debug("Unable to find next event for subject.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            
            try {
                AuditService auditService = new AuditService(dataSource);
                AuditEventBean auditEvent = new AuditEventBean();
                auditEvent.setAuditTable("export");
                auditEvent.setEntityId(0);
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
                auditEvent.setReasonForChange("Export failed: " + msg);
                auditEvent.setActionMessage("Export failed");
                auditService.logEvent(auditEvent, null);
            } catch (Exception auditEx) {
                logger.error("Failed to log export error to audit trail", auditEx);
            }
            
            throw e;
        }

        return odm;

    }

    private StudyEventDefinitionBean getStudyEventDefinitionBean(int ID) {
        StudyEventDefinitionDAO seddao = this._studyEventDefinitionDAO;
        StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) seddao.findByPK(ID);
        return studyEventDefinitionBean;
    }

    private ODM createOdm(StudyBean study, StudySubjectBean studySubjectBean, StudyEventBean nextEvent, List<ODMcomplexTypeDefinitionFormData> formDatas) {
        ODM odm = new ODM();

        ODMcomplexTypeDefinitionClinicalData clinicalData = generateClinicalData(study);
        ODMcomplexTypeDefinitionSubjectData subjectData = generateSubjectData(studySubjectBean);
        ODMcomplexTypeDefinitionStudyEventData studyEventData = generateStudyEventData(nextEvent);
        studyEventData.getFormData().addAll(formDatas);
        subjectData.getStudyEventData().add(studyEventData);
        clinicalData.getSubjectData().add(subjectData);
        odm.getClinicalData().add(clinicalData);

        return odm;
    }

    private String createEnketoUrl(String studyOID, CRFVersionBean crfVersion, StudyEventBean nextEvent, String ssoid, ServletContext context) throws Exception {
        PFormCache cache = PFormCache.getInstance(context);
        String enketoURL = cache.getPFormURL(studyOID, crfVersion.getOid());
        String contextHash = cache.putSubjectContext(ssoid, String.valueOf(nextEvent.getStudyEventDefinitionId()),
                String.valueOf(nextEvent.getSampleOrdinal()), crfVersion.getOid());

        String url = enketoURL + "?" + FORM_CONTEXT + "=" + contextHash;
        logger.debug("Enketo URL for " + crfVersion.getName() + "= " + url);
        return url;

    }

    private String createEditUrl(String studyOID, CRFVersionBean crfVersion, StudyEventBean nextEvent, String ssoid, ServletContext context) throws Exception {
        PFormCache cache = PFormCache.getInstance(context);
        String contextHash = cache.putSubjectContext(ssoid, String.valueOf(nextEvent.getStudyEventDefinitionId()),
                String.valueOf(nextEvent.getSampleOrdinal()), crfVersion.getOid());
        String editURL = CoreResources.getField("sysURL.base") + "pages/api/v1/editform/" + studyOID + "/url";

        String url = editURL + "?" + FORM_CONTEXT + "=" + contextHash;
        logger.debug("Edit URL for " + crfVersion.getName() + "= " + url);
        return url;

    }

    private ODMcomplexTypeDefinitionFormData getFormDataPerCrf(CRFVersionBean crfVersion, StudyEventBean nextEvent, List<EventCRFBean> eventCrfs,
            CRFDAO crfDAO, String formUrl,boolean itemDataExists) {
        EventCRFBean selectedEventCRFBean = null;
        CRFBean crfBean = (CRFBean) crfDAO.findByVersionId(crfVersion.getId());
        for (EventCRFBean eventCRFBean : eventCrfs) {
            if (eventCRFBean.getCRFVersionId() == crfVersion.getId()) {
                selectedEventCRFBean = eventCRFBean;
                break;
            }
        }
        return generateFormData(crfVersion, nextEvent, selectedEventCRFBean, crfBean, formUrl, itemDataExists);

    }

    private ODMcomplexTypeDefinitionClinicalData generateClinicalData(StudyBean study) {
        ODMcomplexTypeDefinitionClinicalData clinicalData = new ODMcomplexTypeDefinitionClinicalData();
        clinicalData.setStudyName(study.getName());
        clinicalData.setStudyOID(study.getOid());
        return clinicalData;
    }

    private ODMcomplexTypeDefinitionSubjectData generateSubjectData(StudySubjectBean studySubject) {
        ODMcomplexTypeDefinitionSubjectData subjectData = new ODMcomplexTypeDefinitionSubjectData();
        subjectData.setSubjectKey(studySubject.getOid());
        subjectData.setStudySubjectID(studySubject.getLabel());
        subjectData.setStatus(studySubject.getStatus().getName());
        return subjectData;
    }

    private ODMcomplexTypeDefinitionStudyEventData generateStudyEventData(StudyEventBean studyEvent) {
        ODMcomplexTypeDefinitionStudyEventData studyEventData = new ODMcomplexTypeDefinitionStudyEventData();
        studyEventData.setStartDate(studyEvent.getDateStarted().toString());
        StudyEventDefinitionBean studyEventDefBean = getStudyEventDefinitionBean(studyEvent.getStudyEventDefinitionId());
        studyEventData.setEventName(studyEventDefBean.getName());
        studyEventData.setStudyEventOID(studyEventDefBean.getOid());
        studyEventData.setStudyEventRepeatKey(String.valueOf(studyEvent.getSampleOrdinal()));
        return studyEventData;
    }

    private ODMcomplexTypeDefinitionFormData generateFormData(CRFVersionBean crfVersionBean, StudyEventBean nextEvent, EventCRFBean eventCRFBean,
            CRFBean crfBean, String formUrl,boolean itemDataExists) {
        ODMcomplexTypeDefinitionFormData formData = new ODMcomplexTypeDefinitionFormData();
        formData.setFormOID(crfVersionBean.getOid());
        formData.setFormName(crfBean.getName());
        formData.setVersionDescription(crfVersionBean.getDescription());
        formData.setUrl(formUrl);
        if (eventCRFBean == null) {
            formData.setStatus("Not Started");
        } else {
            EventCrf eventCrf = eventCrfDao.findById(eventCRFBean.getId());
            if (!itemDataExists){
                formData.setStatus("Not Started");                
            }else{
                formData.setStatus(eventCRFBean.getStatus().getName());                
            } 
            
            if (eventCrf.getDateUpdated() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                formData.setStatusChangeTimeStamp(sdf.format(eventCrf.getDateUpdated()));
            }
        }
        return formData;
    }

    private StudyBean getStudy(String oid) {
        sdao = this._studyDAO;
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    private boolean mayProceed(String studyOid, StudySubjectBean ssBean) throws Exception {
        boolean accessPermission = false;
        logger.info("  studySubjectStatus: " + ssBean.getStatus().getName());
        if (mayProceed(studyOid) && ssBean.getStatus() == Status.AVAILABLE) {
            accessPermission = true;
        }
        return accessPermission;
    }

    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = this._studyParameterValueDAO;
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(studyOid).toString(); 
        String participateStatus = pStatus.getValue().toString(); 
        String studyStatus = study.getStatus().getName().toString(); 
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus);
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }
        return accessPermission;
    }

}
