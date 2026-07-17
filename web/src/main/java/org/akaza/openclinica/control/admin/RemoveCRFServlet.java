/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;

/**
 * Removes a crf
 *
 * @author jxu
 */
@Component
public class RemoveCRFServlet extends SecureController {
    private CRFDAO _cRFDAO;
    private CRFVersionDAO _cRFVersionDAO;
    private EventCRFDAO _eventCRFDAO;
    private EventDefinitionCRFDAO _eventDefinitionCRFDAO;
    private ItemDataDAO _itemDataDAO;
    private SectionDAO _sectionDAO;
    private StudyEventDAO _studyEventDAO;
    private StudyEventDefinitionDAO _studyEventDefinitionDAO;

    @Autowired
    public RemoveCRFServlet(CRFDAO _cRFDAO, CRFVersionDAO _cRFVersionDAO, EventCRFDAO _eventCRFDAO, EventDefinitionCRFDAO _eventDefinitionCRFDAO, ItemDataDAO _itemDataDAO, SectionDAO _sectionDAO, StudyEventDAO _studyEventDAO, StudyEventDefinitionDAO _studyEventDefinitionDAO) {
        this._cRFDAO = _cRFDAO;
        this._cRFVersionDAO = _cRFVersionDAO;
        this._eventCRFDAO = _eventCRFDAO;
        this._eventDefinitionCRFDAO = _eventDefinitionCRFDAO;
        this._itemDataDAO = _itemDataDAO;
        this._sectionDAO = _sectionDAO;
        this._studyEventDAO = _studyEventDAO;
        this._studyEventDefinitionDAO = _studyEventDefinitionDAO;
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
        throw new InsufficientPermissionException(Page.CRF_LIST_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        CRFDAO cdao = this._cRFDAO;
        CRFVersionDAO cvdao = this._cRFVersionDAO;
        FormProcessor fp = new FormProcessor(request);

        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfId = fp.getInt("id", true);

        String action = request.getParameter("action");
        if (crfId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_remove"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            CRFBean crf = (CRFBean) cdao.findByPK(crfId);
            ArrayList versions = cvdao.findAllByCRFId(crfId);
            crf.setVersions(versions);
            EventDefinitionCRFDAO edcdao = this._eventDefinitionCRFDAO;
            ArrayList edcs = (ArrayList) edcdao.findAllByCRF(crfId);

            SectionDAO secdao = this._sectionDAO;

            EventCRFDAO evdao = this._eventCRFDAO;
            ArrayList eventCRFs = evdao.findAllByCRF(crfId);
            StudyEventDAO seDao = this._studyEventDAO;
            StudyEventDefinitionDAO sedDao = this._studyEventDefinitionDAO;
            for (Object ecBean: eventCRFs) {
                StudyEventBean seBean = (StudyEventBean) seDao.findByPK(((EventCRFBean)ecBean).getStudyEventId());
                StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean)sedDao.findByPK(seBean.getStudyEventDefinitionId());
                ((EventCRFBean)ecBean).setEventName(sedBean.getName());
            }
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute("crfToRemove", crf);
                request.setAttribute("eventCRFs", eventCRFs);
                forwardPage(Page.REMOVE_CRF);
            } else {
                logger.info("submit to remove the crf");
                crf.setStatus(Status.DELETED);
                crf.setUpdater(ub);
                crf.setUpdatedDate(new Date());
                cdao.update(crf);

                for (int i = 0; i < versions.size(); i++) {
                    CRFVersionBean version = (CRFVersionBean) versions.get(i);
                    if (!version.getStatus().equals(Status.DELETED)) {
                        version.setStatus(Status.AUTO_DELETED);
                        version.setUpdater(ub);
                        version.setUpdatedDate(new Date());
                        cvdao.update(version);

                        ArrayList sections = secdao.findAllByCRFVersionId(version.getId());
                        for (int j = 0; j < sections.size(); j++) {
                            SectionBean section = (SectionBean) sections.get(j);
                            if (!section.getStatus().equals(Status.DELETED)) {
                                section.setStatus(Status.AUTO_DELETED);
                                section.setUpdater(ub);
                                section.setUpdatedDate(new Date());
                                secdao.update(section);
                            }
                        }
                    }
                }

                for (int i = 0; i < edcs.size(); i++) {
                    EventDefinitionCRFBean edc = (EventDefinitionCRFBean) edcs.get(i);
                    if (!edc.getStatus().equals(Status.DELETED)) {
                        edc.setStatus(Status.AUTO_DELETED);
                        edc.setUpdater(ub);
                        edc.setUpdatedDate(new Date());
                        edcdao.update(edc);
                    }
                }

                ItemDataDAO idao = this._itemDataDAO;
                for (int i = 0; i < eventCRFs.size(); i++) {
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(i);
                    if (!eventCRF.getStatus().equals(Status.DELETED)) {
                        eventCRF.setStatus(Status.AUTO_DELETED);
                        eventCRF.setUpdater(ub);
                        eventCRF.setUpdatedDate(new Date());
                        evdao.update(eventCRF);

                        ArrayList items = idao.findAllByEventCRFId(eventCRF.getId());
                        for (int j = 0; j < items.size(); j++) {
                            ItemDataBean item = (ItemDataBean) items.get(j);
                            if (!item.getStatus().equals(Status.DELETED)) {
                                item.setStatus(Status.AUTO_DELETED);
                                item.setUpdater(ub);
                                item.setUpdatedDate(new Date());
                                idao.update(item);
                            }
                        }
                    }
                }

                addPageMessage(respage.getString("the_CRF") + crf.getName() + " " + respage.getString("has_been_removed_succesfully"));
                forwardPage(Page.CRF_LIST_SERVLET);

            }
        }

    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

}
