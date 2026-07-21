package org.akaza.openclinica.controller;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.ResponseTypeDao;
import org.akaza.openclinica.dao.hibernate.SectionDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.ResponseType;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.EnketoAPI;
import org.akaza.openclinica.web.pform.EnketoCredentials;
import org.akaza.openclinica.web.pform.PFormCache;
import javax.sql.DataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Controller
@RequestMapping(value = "/api/v1/editform")
public class EditFormController {
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext applicationContext;


    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    private org.akaza.openclinica.service.EditFormService editFormService;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    StudyDAO sdao;

    /**
     * @api {get} /pages/api/v1/editform/:studyOid/url Get Form Edit URL
     * @apiName getEditUrl
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} ecid Key that will be used by enketo to cache form information.
     * @apiGroup Form
     * @apiDescription This API is used to retrieve a URL for a form with data pre-loaded into it
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "ecid":"a9f8f3aadea4b67e1f214140ccfdf70bad0b9e9b622a9776a3c85bbf6bb532cd"
     *                  }
     * @apiSuccessExample Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    http://ocform.oc.com:8005/edit/::YYYM?instance_id=
     *                    d16bba9200177fad34594e75d8b9565ff92b0bce4297e3b6c27275e531044a59
     *                    &returnUrl=http%3A%2F%2Fstudy1.mystudy.me%3A8080%2F%23%2Fevent%2FSS_SUB001%2Fdashboard&ecid=
     *                    d16bba9200177fad34594e75d8b9565ff92b0bce4297e3b6c27275e531044a59
     *                    }
     */

    @RequestMapping(value = "/{studyOid}/url", method = RequestMethod.GET)
    public ResponseEntity<String> getEditUrl(@RequestParam(FORM_CONTEXT) String formContext, @PathVariable("studyOid") String studyOID) throws Exception {

        String editURL = null;
        if (!mayProceed(studyOID))
            return new ResponseEntity<String>(editURL, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        // Load context
        PFormCache cache = PFormCache.getInstance(context);
        HashMap<String, String> userContext = cache.getSubjectContext(formContext);

        // Fetch data via transactional service returning a DTO
        org.akaza.openclinica.bean.submit.EditFormDTO dto = editFormService.getEditFormDetails(userContext);

        // Call Enketo api to get edit url
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOID));

        // Build redirect url
        String redirectUrl = getRedirectUrl(dto.getStudySubjectOid(), studyOID);

        // Return Enketo URL
        editURL = enketo.getEditURL(dto.getCrfVersionOid(), dto.getPopulatedInstance(), formContext, redirectUrl).getEdit_url() + "&ecid=" + formContext;
        logger.debug("Generating Enketo edit url for form: " + editURL);

        return new ResponseEntity<String>(editURL, org.springframework.http.HttpStatus.ACCEPTED);

    }

    private String getRedirectUrl(String studySubjectOid, String studyOid) {
        String portalURL = CoreResources.getField("portalURL");
        String url = "";
        if (portalURL != null && !portalURL.equals("")) {
            ParticipantPortalRegistrar registrar = new ParticipantPortalRegistrar();
            Authorization pManageAuthorization = registrar.getAuthorization(studyOid);
            try {
                URL pManageUrl = new URL(portalURL);

                if (pManageAuthorization != null && pManageAuthorization.getStudy() != null && pManageAuthorization.getStudy().getHost() != null
                        && !pManageAuthorization.getStudy().getHost().equals("")) {
                    url = pManageUrl.getProtocol() + "://" + pManageAuthorization.getStudy().getHost() + "." + pManageUrl.getHost()
                            + ((pManageUrl.getPort() > 0) ? ":" + String.valueOf(pManageUrl.getPort()) : "");
                }
            } catch (MalformedURLException e) {
                logger.error("Error building redirect URL: " + e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
                return "";
            }
        }
        if (!url.equals(""))
            url = url + "/#/event/" + studySubjectOid + "/dashboard";
        return url;
    }



    private StudyBean getParentStudy(Integer studyId) {
        StudyBean study = getStudy(studyId);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

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

    private StudyBean getStudy(Integer id) {
        sdao = applicationContext.getBean(StudyDAO.class);
        StudyBean studyBean = (StudyBean) sdao.findByPK(id);
        return studyBean;
    }

    private StudyBean getStudy(String oid) {
        sdao = applicationContext.getBean(StudyDAO.class);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private String fetchEditUrl(String studyOID, CRFVersionBean crfVersion, int studyEventDefinitionId) throws Exception {
        StudyBean parentStudyBean = getParentStudy(studyOID);
        PFormCache cache = PFormCache.getInstance(context);
        String enketoURL = cache.getPFormURL(parentStudyBean.getOid(), crfVersion.getOid());
        String contextHash = cache.putAnonymousFormContext(studyOID, crfVersion.getOid(), studyEventDefinitionId);

        String url = enketoURL + "&" + FORM_CONTEXT + "=" + contextHash;
        logger.debug("Enketo URL for " + crfVersion.getName() + "= " + url);
        return url;

    }

    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean siteStudy = getStudy(studyOid);
        StudyBean study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = applicationContext.getBean(StudyParameterValueDAO.class);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOid()).toString(); // ACTIVE ,
                                                                                                            // PENDING ,
                                                                                                            // INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        String siteStatus = siteStudy.getStatus().getName().toString(); // available , pending , frozen , locked
        System.out.println("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus
                + "   siteStatus: " + siteStatus);
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "   siteStatus: "
                + siteStatus);
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available")
                && pManageStatus.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }

        return accessPermission;
    }

}