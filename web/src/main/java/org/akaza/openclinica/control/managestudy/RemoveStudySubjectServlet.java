/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 *
 * Removes a study subject and all the related data
 */
@Component
public class RemoveStudySubjectServlet extends SecureController {
    private EventCRFDAO _eventCRFDAO;
    private ItemDataDAO _itemDataDAO;
    private StudyDAO _studyDAO;
    private StudyEventDAO _studyEventDAO;
    private StudySubjectDAO _studySubjectDAO;
    private SubjectDAO _subjectDAO;

    @Autowired
    public RemoveStudySubjectServlet(EventCRFDAO _eventCRFDAO, ItemDataDAO _itemDataDAO, StudyDAO _studyDAO, StudyEventDAO _studyEventDAO, StudySubjectDAO _studySubjectDAO, SubjectDAO _subjectDAO) {
        this._eventCRFDAO = _eventCRFDAO;
        this._itemDataDAO = _itemDataDAO;
        this._studyDAO = _studyDAO;
        this._studyEventDAO = _studyEventDAO;
        this._studySubjectDAO = _studySubjectDAO;
        this._subjectDAO = _subjectDAO;
    }

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS_SERVLET, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS_SERVLET, respage.getString("current_study_frozen"));
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String studySubIdString = request.getParameter("id");// studySubjectId
        String subIdString = request.getParameter("subjectId");
        String studyIdString = request.getParameter("studyId");

        SubjectDAO sdao = this._subjectDAO;
        StudySubjectDAO subdao = this._studySubjectDAO;

        if (StringUtil.isBlank(studySubIdString) || StringUtil.isBlank(subIdString) || StringUtil.isBlank(studyIdString)) {
            addPageMessage(respage.getString("please_choose_a_study_subject_to_remove"));
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
        } else {
            int studyId = Integer.valueOf(studyIdString.trim()).intValue();
            int studySubId = Integer.valueOf(studySubIdString.trim()).intValue();
            int subjectId = Integer.valueOf(subIdString.trim()).intValue();

            SubjectBean subject = (SubjectBean) sdao.findByPK(subjectId);

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);

            StudyDAO studydao = this._studyDAO;
            StudyBean study = (StudyBean) studydao.findByPK(studyId);

            checkRoleByUserAndStudy(ub, study.getParentStudyId(), study.getId());

            // find study events
            StudyEventDAO sedao = this._studyEventDAO;
//            ArrayList events = sedao.findAllByStudyAndStudySubjectId(study, studySubId);
            ArrayList<DisplayStudyEventBean> displayEvents = ViewStudySubjectServlet.getDisplayStudyEventsForStudySubject(studySub, sm.getDataSource(), ub, currentRole);
            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {
                if (!studySub.getStatus().equals(Status.AVAILABLE)) {
                    addPageMessage(respage.getString("this_subject_is_not_available_for_this_study") + " "
                        + respage.getString("please_contact_sysadmin_for_more_information"));
                    forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
                    return;
                }

                request.setAttribute("subject", subject);
                request.setAttribute("study", study);
                request.setAttribute("studySub", studySub);
                request.setAttribute("events", displayEvents);

                forwardPage(Page.REMOVE_STUDY_SUBJECT);
            } else {
                logger.info("submit to remove the subject from study");
                // remove subject from study
                studySub.setStatus(Status.DELETED);
                studySub.setUpdater(ub);
                studySub.setUpdatedDate(new Date());
                subdao.update(studySub);

                // remove all study events
                // remove all event crfs
                EventCRFDAO ecdao = this._eventCRFDAO;

                for (int j = 0; j < displayEvents.size(); j++) {
                    DisplayStudyEventBean dispEvent = displayEvents.get(j);
                    StudyEventBean event = dispEvent.getStudyEvent();
                    if (!event.getStatus().equals(Status.DELETED)) {
                        event.setStatus(Status.AUTO_DELETED);
                        event.setUpdater(ub);
                        event.setUpdatedDate(new Date());
                        sedao.update(event);

                        ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

                        ItemDataDAO iddao = this._itemDataDAO;
                        for (int k = 0; k < eventCRFs.size(); k++) {
                            EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                            if (!eventCRF.getStatus().equals(Status.DELETED)) {
                                eventCRF.setStatus(Status.AUTO_DELETED);
                                eventCRF.setUpdater(ub);
                                eventCRF.setUpdatedDate(new Date());
                                ecdao.update(eventCRF);
                                // remove all the item data
                                ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                                for (int a = 0; a < itemDatas.size(); a++) {
                                    ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                                    if (!item.getStatus().equals(Status.DELETED)) {
                                        item.setStatus(Status.AUTO_DELETED);
                                        item.setUpdater(ub);
                                        item.setUpdatedDate(new Date());
                                        iddao.update(item);
                                    }
                                }
                            }
                        }
                    }
                }

                String emailBody =
                    respage.getString("the_subject") + " " + subject.getName() + " " + respage.getString("has_been_removed_from_the_study") + study.getName()
                        + ".";

                addPageMessage(emailBody);
//                try{
//                    sendEmail(emailBody);    
//                }catch(Exception ex){
//                    addPageMessage(respage.getString("mail_cannot_be_sent_to_admin"));
//                }
                forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
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
        // to study director
        boolean messageSent = sendEmail(ub.getEmail().trim(), respage.getString("remove_event_from_study"), emailBody, false);
        // to admin
        if(messageSent){
            sendEmail(EmailEngine.getAdminEmail(), respage.getString("remove_event_from_study"), emailBody, false);
        }

        logger.info("Sending email done..");
    }

}
