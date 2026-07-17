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
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * Views the detail of an event CRF
 */
@Component
public class ViewEventCRFServlet extends SecureController {
    private CRFDAO _cRFDAO;
    private EventCRFDAO _eventCRFDAO;
    private ItemDAO _itemDAO;
    private ItemDataDAO _itemDataDAO;
    private ItemFormMetadataDAO _itemFormMetadataDAO;
    private SectionDAO _sectionDAO;
    private StudySubjectDAO _studySubjectDAO;

    @Autowired
    public ViewEventCRFServlet(CRFDAO _cRFDAO, EventCRFDAO _eventCRFDAO, ItemDAO _itemDAO, ItemDataDAO _itemDataDAO, ItemFormMetadataDAO _itemFormMetadataDAO, SectionDAO _sectionDAO, StudySubjectDAO _studySubjectDAO) {
        this._cRFDAO = _cRFDAO;
        this._eventCRFDAO = _eventCRFDAO;
        this._itemDAO = _itemDAO;
        this._itemDataDAO = _itemDataDAO;
        this._itemFormMetadataDAO = _itemFormMetadataDAO;
        this._sectionDAO = _sectionDAO;
        this._studySubjectDAO = _studySubjectDAO;
    }

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int eventCRFId = fp.getInt("id", true);
        int studySubId = fp.getInt("studySubId", true);

        StudySubjectDAO subdao = this._studySubjectDAO;
        EventCRFDAO ecdao = this._eventCRFDAO;
        ItemDataDAO iddao = this._itemDataDAO;
        ItemDAO idao = this._itemDAO;
        ItemFormMetadataDAO ifmdao = this._itemFormMetadataDAO;
        CRFDAO cdao = this._cRFDAO;
        SectionDAO secdao = this._sectionDAO;

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_view"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
        } else {
            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            EventCRFBean eventCRF = (EventCRFBean) ecdao.findByPK(eventCRFId);
            CRFBean crf = cdao.findByVersionId(eventCRF.getCRFVersionId());
            request.setAttribute("crf", crf);

            ArrayList sections = secdao.findAllByCRFVersionId(eventCRF.getCRFVersionId());
            for (int j = 0; j < sections.size(); j++) {
                SectionBean section = (SectionBean) sections.get(j);
                ArrayList itemData = iddao.findAllByEventCRFId(eventCRFId);

                ArrayList displayItemData = new ArrayList();
                for (int i = 0; i < itemData.size(); i++) {
                    ItemDataBean id = (ItemDataBean) itemData.get(i);
                    DisplayItemBean dib = new DisplayItemBean();
                    ItemBean item = (ItemBean) idao.findByPK(id.getItemId());
                    ItemFormMetadataBean ifm = ifmdao.findByItemIdAndCRFVersionId(item.getId(), eventCRF.getCRFVersionId());

                    item.setItemMeta(ifm);
                    dib.setItem(item);
                    dib.setData(id);
                    dib.setMetadata(ifm);
                    displayItemData.add(dib);
                }
                section.setItems(displayItemData);
            }

            request.setAttribute("sections", sections);
            request.setAttribute("studySubId", new Integer(studySubId).toString());
            forwardPage(Page.VIEW_EVENT_CRF);
        }
    }

}
