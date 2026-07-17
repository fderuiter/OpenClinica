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
import org.akaza.openclinica.bean.admin.NewCRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.datamap.VersioningMap;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
@Component
public class DeleteCRFVersionServlet extends SecureController {
    private CRFDAO _cRFDAO;
    private CRFVersionDAO _cRFVersionDAO;
    private EventCRFDAO _eventCRFDAO;
    private EventDefinitionCRFDAO _eventDefinitionCRFDAO;
    private ItemDataDAO _itemDataDAO;
    private StudyEventDAO _studyEventDAO;
    private StudyEventDefinitionDAO _studyEventDefinitionDAO;
    private StudySubjectDAO _studySubjectDAO;

    @Autowired
    public DeleteCRFVersionServlet(CRFDAO _cRFDAO, CRFVersionDAO _cRFVersionDAO, EventCRFDAO _eventCRFDAO, EventDefinitionCRFDAO _eventDefinitionCRFDAO, ItemDataDAO _itemDataDAO, StudyEventDAO _studyEventDAO, StudyEventDefinitionDAO _studyEventDefinitionDAO, StudySubjectDAO _studySubjectDAO) {
        this._cRFDAO = _cRFDAO;
        this._cRFVersionDAO = _cRFVersionDAO;
        this._eventCRFDAO = _eventCRFDAO;
        this._eventDefinitionCRFDAO = _eventDefinitionCRFDAO;
        this._itemDataDAO = _itemDataDAO;
        this._studyEventDAO = _studyEventDAO;
        this._studyEventDefinitionDAO = _studyEventDefinitionDAO;
        this._studySubjectDAO = _studySubjectDAO;
    }

    public static final String VERSION_ID = "verId";

    public static final String VERSION_TO_DELETE = "version";

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.CRF_LIST_SERVLET, "not admin", "1");
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int versionId = fp.getInt(VERSION_ID, true);
        String action = request.getParameter("action");
        if (versionId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_version_to_delete"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            CRFVersionDAO cvdao = this._cRFVersionDAO;
            CRFDAO cdao = this._cRFDAO;
            EventDefinitionCRFDAO edcdao = this._eventDefinitionCRFDAO;
            StudyEventDefinitionDAO sedDao = this._studyEventDefinitionDAO;
            StudyEventDAO seDao = this._studyEventDAO;
            ItemDataDAO iddao = this._itemDataDAO;
            EventCRFDAO ecdao = this._eventCRFDAO;
            StudySubjectDAO ssdao = this._studySubjectDAO;
            CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(versionId);

            // find definitions using this version
            ArrayList definitions = edcdao.findByDefaultVersion(version.getId());
            for (Object edcBean: definitions) {
                StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean)sedDao.findByPK(((EventDefinitionCRFBean)edcBean).getStudyEventDefinitionId());
                ((EventDefinitionCRFBean)edcBean).setEventName(sedBean.getName());
            }

            // find event crfs using this version
            		
            ArrayList<ItemDataBean> idBeans = iddao.findByCRFVersion(version);
            ArrayList <EventCRFBean> eCRFs = ecdao.findAllByCRF(version.getCrfId());
               for(EventCRFBean eCRF : eCRFs){
            	   
            	   StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(eCRF.getStudySubjectId());
            	   eCRF.setStudySubject(ssBean);
                   StudyEventBean seBean = (StudyEventBean) seDao.findByPK(eCRF.getStudyEventId());
                   StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) sedDao.findByPK(seBean.getStudyEventDefinitionId());
                   seBean.setStudyEventDefinition(sedBean);
                   eCRF.setStudyEvent(seBean);
               }
            
            ArrayList eventCRFs = ecdao.findAllByCRFVersion(versionId);
            boolean canDelete = true;
            if (!definitions.isEmpty()) {// used in definition
                canDelete = false;
                request.setAttribute("definitions", definitions);
                addPageMessage(respage.getString("this_CRF_version") + " "+ version.getName()
                    + respage.getString("has_associated_study_events_definitions_cannot_delete"));

            } else if (!idBeans.isEmpty()) {
                canDelete = false;
                request.setAttribute("eventCRFs", eCRFs);
                request.setAttribute("itemDataForVersion", idBeans);
                addPageMessage(respage.getString("this_CRF_version") +" "+ version.getName() + respage.getString("has_associated_item_data_cannot_delete"));
            
            } else if (!eventCRFs.isEmpty()) {
                canDelete = false;
                request.setAttribute("eventsForVersion", eventCRFs);
                addPageMessage(respage.getString("this_CRF_version") + " "+ version.getName() + respage.getString("has_associated_study_events_cannot_delete"));
            }
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute(VERSION_TO_DELETE, version);
                forwardPage(Page.DELETE_CRF_VERSION);
            } else {
                // submit
                if (canDelete) {
                    ArrayList items = cvdao.findNotSharedItemsByVersion(versionId);
                    NewCRFBean nib = new NewCRFBean(sm.getDataSource(), version.getCrfId());
                    nib.setDeleteQueries(cvdao.generateDeleteQueries(versionId, items));
                    nib.deleteFromDB();
                    addPageMessage(respage.getString("the_CRF_version_has_been_deleted_succesfully"));
                } else {
                    addPageMessage(respage.getString("the_CRF_version_cannot_be_deleted"));
                }
                forwardPage(Page.CRF_LIST_SERVLET);
            }

        }

    }

}
