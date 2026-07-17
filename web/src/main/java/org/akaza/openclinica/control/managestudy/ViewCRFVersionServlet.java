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
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
@Component
public class ViewCRFVersionServlet extends SecureController {
    private CRFDAO _cRFDAO;
    private CRFVersionDAO _cRFVersionDAO;
    private ItemDAO _itemDAO;
    private ItemFormMetadataDAO _itemFormMetadataDAO;

    @Autowired
    public ViewCRFVersionServlet(CRFDAO _cRFDAO, CRFVersionDAO _cRFVersionDAO, ItemDAO _itemDAO, ItemFormMetadataDAO _itemFormMetadataDAO) {
        this._cRFDAO = _cRFDAO;
        this._cRFVersionDAO = _cRFVersionDAO;
        this._itemDAO = _itemDAO;
        this._itemFormMetadataDAO = _itemFormMetadataDAO;
    }

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        CRFVersionDAO cvdao = this._cRFVersionDAO;
        ItemDAO idao = this._itemDAO;
        ItemFormMetadataDAO ifmdao = this._itemFormMetadataDAO;
        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfVersionId = fp.getInt("id");

        if (crfVersionId == 0) {
            addPageMessage(respage.getString("please_choose_a_crf_to_view_details"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            // tbh
            CRFDAO crfdao = this._cRFDAO;
            CRFBean crf = (CRFBean) crfdao.findByPK(version.getCrfId());
            CRFVersionMetadataUtil metadataUtil = new CRFVersionMetadataUtil(sm.getDataSource());
            ArrayList<SectionBean> sections = metadataUtil.retrieveFormMetadata(version); 
            request.setAttribute("sections", sections);
            request.setAttribute("version", version);
            // tbh
            request.setAttribute("crfname", crf.getName());
            // tbh
            forwardPage(Page.VIEW_CRF_VERSION);

        }
    }

}
