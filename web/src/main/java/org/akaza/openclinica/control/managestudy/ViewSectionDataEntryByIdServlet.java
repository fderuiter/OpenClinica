/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2008-2009 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Builds on top of ViewSectionDataEntryServlet, Doesn't add much other than using OIDs to get to the View Screen.
 * 
 * @author Krikor Krumlian
 */
@Component
public class ViewSectionDataEntryByIdServlet extends ViewSectionDataEntryServlet {
    private CRFVersionDAO _cRFVersionDAO;
    private StudyDAO _studyDAO;

    @Autowired
    public ViewSectionDataEntryByIdServlet(CRFVersionDAO _cRFVersionDAO, StudyDAO _studyDAO) {
        this._cRFVersionDAO = _cRFVersionDAO;
        this._studyDAO = _studyDAO;
    }


    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.managestudy.ViewSectionDataEntryServlet#mayProceed()
     */
    @Override
    public void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {
        return;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.managestudy.ViewSectionDataEntryServlet#processRequest()
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StudyDAO studyDao = this._studyDAO;
       
        StudyBean  currentStudy = (StudyBean) studyDao.findByPK(1);
        CRFVersionDAO crfVersionDao = this._cRFVersionDAO;
        if (request.getParameter("id") == null) {
            forwardPage(Page.LOGIN, request, response);
        }
        request.setAttribute("study", currentStudy);
        CRFVersionBean crfVersion = crfVersionDao.findByOid(request.getParameter("id"));
        if (crfVersion != null) {
            request.setAttribute("crfVersionId", String.valueOf(crfVersion.getId()));
            request.setAttribute("crfId", String.valueOf(crfVersion.getCrfId()));
            super.processRequest(request, response);
        } else {
            forwardPage(Page.LOGIN, request, response);
        }
    }
}
