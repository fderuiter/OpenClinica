/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 * 
 * Processes request of 'restore an event CRF from a event'
 */
@Component
public class RestoreEventCRFServlet extends SecureController {
    private CRFDAO _cRFDAO;
    private CRFVersionDAO _cRFVersionDAO;
    private EventCRFDAO _eventCRFDAO;
    private EventDefinitionCRFDAO _eventDefinitionCRFDAO;
    private ItemDataDAO _itemDataDAO;
    private StudyDAO _studyDAO;
    private StudyEventDAO _studyEventDAO;
    private StudyEventDefinitionDAO _studyEventDefinitionDAO;
    private StudySubjectDAO _studySubjectDAO;

    @Autowired
    public RestoreEventCRFServlet(CRFDAO _cRFDAO, CRFVersionDAO _cRFVersionDAO, EventCRFDAO _eventCRFDAO, EventDefinitionCRFDAO _eventDefinitionCRFDAO, ItemDataDAO _itemDataDAO, StudyDAO _studyDAO, StudyEventDAO _studyEventDAO, StudyEventDefinitionDAO _studyEventDefinitionDAO, StudySubjectDAO _studySubjectDAO) {
        this._cRFDAO = _cRFDAO;
        this._cRFVersionDAO = _cRFVersionDAO;
        this._eventCRFDAO = _eventCRFDAO;
        this._eventDefinitionCRFDAO = _eventDefinitionCRFDAO;
        this._itemDataDAO = _itemDataDAO;
        this._studyDAO = _studyDAO;
        this._studyEventDAO = _studyEventDAO;
        this._studyEventDefinitionDAO = _studyEventDefinitionDAO;
        this._studySubjectDAO = _studySubjectDAO;
    }

    /**
     * 
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int eventCRFId = fp.getInt("id");// eventCRFId
        int studySubId = fp.getInt("studySubId");// studySubjectId
        checkStudyLocked("ViewStudySubject?id" + studySubId, respage.getString("current_study_locked"));
        StudyEventDAO sedao = this._studyEventDAO;
        StudySubjectDAO subdao = this._studySubjectDAO;
        EventCRFDAO ecdao = this._eventCRFDAO;
        StudyDAO sdao = this._studyDAO;

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_restore"));
            request.setAttribute("id", new Integer(studySubId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {
            EventCRFBean eventCRF = (EventCRFBean) ecdao.findByPK(eventCRFId);

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            // YW 11-07-2007, an event CRF could not be restored if its study
            // subject has been removed
            Status s = studySub.getStatus();
            if ("removed".equalsIgnoreCase(s.getName()) || "auto-removed".equalsIgnoreCase(s.getName())) {
                addPageMessage(resword.getString("event_CRF") + resterm.getString("could_not_be") + resterm.getString("restored") + "."
                    + respage.getString("study_subject_has_been_deleted"));
                request.setAttribute("id", new Integer(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
            // YW
            request.setAttribute("studySub", studySub);

            // construct info needed on view event crf page
            CRFDAO cdao = this._cRFDAO;
            CRFVersionDAO cvdao = this._cRFVersionDAO;

            int crfVersionId = eventCRF.getCRFVersionId();
            CRFBean cb = cdao.findByVersionId(crfVersionId);
            eventCRF.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            eventCRF.setCrfVersion(cvb);

            // then get the definition so we can call
            // DisplayEventCRFBean.setFlags
            int studyEventId = eventCRF.getStudyEventId();

            StudyEventBean event = (StudyEventBean) sedao.findByPK(studyEventId);

            int studyEventDefinitionId = sedao.getDefinitionIdFromStudyEventId(studyEventId);
            StudyEventDefinitionDAO seddao = this._studyEventDefinitionDAO;
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(studyEventDefinitionId);
            event.setStudyEventDefinition(sed);
            request.setAttribute("event", event);

            EventDefinitionCRFDAO edcdao = this._eventDefinitionCRFDAO;

            StudyBean study = (StudyBean) sdao.findByPK(studySub.getStudyId());
            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());

            DisplayEventCRFBean dec = new DisplayEventCRFBean();
            dec.setEventCRF(eventCRF);
            dec.setFlags(eventCRF, ub, currentRole, edc.isDoubleEntry());

            // find all item data
            ItemDataDAO iddao = this._itemDataDAO;

            ArrayList itemData = iddao.findAllByEventCRFId(eventCRF.getId());

            request.setAttribute("items", itemData);

            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {
                if (!eventCRF.getStatus().equals(Status.DELETED) && !eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                    addPageMessage(respage.getString("this_event_CRF_avilable_for_study") + " " + " "
                        + respage.getString("please_contact_sysadmin_for_more_information"));
                    request.setAttribute("id", new Integer(studySubId).toString());
                    forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                    return;
                }

                request.setAttribute("displayEventCRF", dec);

                forwardPage(Page.RESTORE_EVENT_CRF);
            } else {
                logger.info("submit to restore the event CRF from study");

                //Set crf status to AVAILABLE to allow data
                //to be repopulated in the form properly
                eventCRF.setStatus(Status.AVAILABLE);
                ecdao.update(eventCRF);

                // restore all the item data to the appropriate status
                for (int a = 0; a < itemData.size(); a++) {
                    ItemDataBean item = (ItemDataBean) itemData.get(a);
                    if (item.getStatus().equals(Status.AUTO_DELETED)) {
                        item.setStatus(Status.AVAILABLE);
                        item.setUpdater(ub);
                        item.setUpdatedDate(new Date());
                        iddao.update(item);
                    }
                }

                Status restoredStatus = null;
                if(eventCRF.getDateCompleted() != null){
                    if(edc.isDoubleEntry() && eventCRF.getDateValidateCompleted() == null){
                        restoredStatus = Status.PENDING;
                        eventCRF.setStage(DataEntryStage.INITIAL_DATA_ENTRY);
                    }else{
                        restoredStatus = Status.UNAVAILABLE;
                        eventCRF.setStage(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE);
                    }

                    //Reset the DisplayEventCRFBean
                    dec.setEventCRF(eventCRF);
                    dec.setFlags(eventCRF, ub, currentRole, edc.isDoubleEntry());
                }else {
                    restoredStatus = Status.AVAILABLE;
                }
                eventCRF.setStatus(restoredStatus);
                eventCRF.setUpdater(ub);
                eventCRF.setUpdatedDate(new Date());

                ecdao.update(eventCRF);

                String emailBody =
                    respage.getString("the_event_CRF") + cb.getName() + " " + respage.getString("has_been_restored_to_the_event") + " "
                        + event.getStudyEventDefinition().getName() + ".";

                addPageMessage(emailBody);
                sendEmail(emailBody);
                request.setAttribute("id", new Integer(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }

    /**
     * Send email to director and administrator
     * 
     * @param request
     * @param response
     */
    private void sendEmail(String emailBody) throws Exception {

        logger.info("Sending email...");
        sendEmail(ub.getEmail().trim(), respage.getString("restore_event_CRF_to_event"), emailBody, false);
        // to admin
        sendEmail(EmailEngine.getAdminEmail(), respage.getString("restore_event_CRF_to_event"), emailBody, false);
        logger.info("Sending email done..");
    }

}
