package org.akaza.openclinica.controller;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

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
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.ParticipantEventService;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.akaza.openclinica.service.audit.AuditService;
import org.akaza.openclinica.bean.admin.AuditEventBean;
import javax.sql.DataSource;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/odmk")
public class OdmController {

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    RuleController ruleController;

    @Autowired
    EventCrfDao eventCrfDao;

    @Autowired
    private org.akaza.openclinica.service.OdmService odmService;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;
    StudyDAO sdao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * @api {get} /pages/odmk/studies/:studyOid/metadata Retrieve metadata
     * @apiName getStudyMetadata
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiGroup Study
     * @apiDescription Retrieve the metadata of the specified study
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    The whole Study Metadata
     *                    }
     */

    @RequestMapping(value = "/studies/{study}/metadata", method = RequestMethod.GET)
    public ModelAndView getStudyMetadata(Model model, HttpSession session, @PathVariable("study") String studyOid, HttpServletResponse response)
            throws Exception {
        if (!mayProceed(studyOid))
            return null;
        return ruleController.studyMetadata(model, session, studyOid, response);
    }

    /**
     * This URL needs to change ... Right now security disabled on this ... You can call this with
     * http://localhost:8080/OpenClinica-web-MAINLINE-SNAPSHOT /pages/odmk/studies/S_DEFAULTS1/events
     *
     * @param studyOid
     * @return
     * @throws Exception
     */
    /**
     * @api {get} /pages/odmk/study/:studyOid/studysubject/:studySubjectOid/events Retrieve an event - participant
     * @apiName getEvent
     * @apiPermission Module participate - enabled
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} studySubjectOid Study Subject Oid
     * @apiGroup Study Event
     * @apiDescription Retrieve an event with earliest start date and ordinal.
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "studySubjectOid": "SS_DYN101"
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "id": null,
     *                    "signature": [],
     *                    "clinicalData": [{
     *                    "annotations": [],
     *                    "subjectData": [{
     *                    "annotation": [],
     *                    "signature": null,
     *                    "status": "available",
     *                    "dateOfBirth": null,
     *                    "uniqueIdentifier": null,
     *                    "studyEventData": [{
     *                    "annotation": [],
     *                    "signature": null,
     *                    "status": null,
     *                    "eventName": "Scoring Visit",
     *                    "studyEventRepeatKey": null,
     *                    "endDate": null,
     *                    "formData": [{
     *                    "annotation": [],
     *                    "signature": null,
     *                    "status": "Not Started",
     *                    "interviewerName": null,
     *                    "formOID": "F_SCORING2_CRF_V10",
     *                    "itemGroupData": [],
     *                    "url":
     *                    "http://localhost:8006/::YYYF?iframe=true&ecid=a480dc4479409f6fe99a03d472f5cf77f4f12fb2b5ac471b9d35d737d934b042"
     *                    ,
     *                    "version": null,
     *                    "transactionType": null,
     *                    "auditRecord": null,
     *                    "archiveLayoutRef": null,
     *                    "formDataElementExtension": [],
     *                    "formRepeatKey": null,
     *                    "interviewDate": null,
     *                    "formName": "Scoring2_CRF",
     *                    "versionDescription": "Scoring2",
     *                    "statusChangeTimeStamp": null
     *                    }],
     *                    "studyEventOID": null,
     *                    "transactionType": null,
     *                    "auditRecord": null,
     *                    "studyEventDataElementExtension": [],
     *                    "studyEventLocation": null,
     *                    "startDate": "2015-08-27 12:00:00.0",
     *                    "subjectAgeAtEvent": null
     *                    }],
     *                    "studySubjectID": "DYN101",
     *                    "transactionType": null,
     *                    "yearOfBirth": null,
     *                    "auditRecord": null,
     *                    "investigatorRef": null,
     *                    "siteRef": null,
     *                    "subjectDataElementExtension": [],
     *                    "subjectKey": "SS_DYN101",
     *                    "secondaryID": null,
     *                    "sex": null
     *                    }],
     *                    "studyName": "Baseline Study 101",
     *                    "studyOID": "S_BL101",
     *                    "metaDataVersionOID": null,
     *                    "auditRecords": [],
     *                    "signatures": []
     *                    }],
     *                    "fileType": null,
     *                    "fileOID": null,
     *                    "description": null,
     *                    "study": [],
     *                    "association": [],
     *                    "odmversion": null,
     *                    "creationDateTime": null,
     *                    "adminData": [],
     *                    "referenceData": [],
     *                    "granularity": null,
     *                    "archival": null,
     *                    "priorFileOID": null,
     *                    "asOfDateTime": null,
     *                    "originator": null,
     *                    "sourceSystem": null,
     *                    "sourceSystemVersion": null
     *                    }
     */

    @RequestMapping(value = "/study/{studyOid}/studysubject/{studySubjectOid}/events", method = RequestMethod.GET)
    public @ResponseBody
    ODM getEvent(@PathVariable("studyOid") String studyOid, @PathVariable("studySubjectOid") String studySubjectOid) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        return odmService.getODM(studyOid, studySubjectOid, context);
    }



    private StudyBean getStudy(String oid) {
        sdao = new org.akaza.openclinica.dao.managestudy.StudyDAO(dataSource);
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

    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean study = getParentStudy(studyOid);
        org.akaza.openclinica.dao.service.StudyParameterValueDAO spvdao = new org.akaza.openclinica.dao.service.StudyParameterValueDAO(dataSource);
        org.akaza.openclinica.bean.service.StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
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
