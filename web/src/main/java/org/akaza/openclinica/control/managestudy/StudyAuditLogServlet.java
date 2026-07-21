/*
 * Created on Sep 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.Locale;

/**
 * @author thickerson
 * 
 * 
 */
public class StudyAuditLogServlet extends SecureController {

    Locale locale;

    // <ResourceBundle resword,resexception,respage;

    public static String getLink(int userId) {
        return "AuditLogStudy";
    }

    /*
     * (non-Javadoc) Assume that we get the user id automatically. We will jump
     * from the edit user page if the user is an admin, they can get to see the
     * users' log
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */

    /*
     * (non-Javadoc) redo this servlet to run the audits per study subject for
     * the study; need to add a studyId param and then use the
     * StudySubjectDAO.findAllByStudyOrderByLabel() method to grab a lot of
     * study subject beans and then return them much like in
     * ViewStudySubjectAuditLogServet.process() currentStudy instead of studyId?
     */
    @Override
    protected void processRequest() throws Exception {
        int studyId = currentStudy.getId();

        StudySubjectDAO subdao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), StudySubjectDAO.class);
        SubjectDAO sdao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), SubjectDAO.class);
        AuditDAO adao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), AuditDAO.class);
        UserAccountDAO uadao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), UserAccountDAO.class);

        FormProcessor fp = new FormProcessor(request);

        StudyEventDAO sedao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), StudyEventDAO.class);
        StudyEventDefinitionDAO seddao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), StudyEventDefinitionDAO.class);
        EventDefinitionCRFDAO edcdao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), EventDefinitionCRFDAO.class);
        EventCRFDAO ecdao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), EventCRFDAO.class);
        StudyDAO studydao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), StudyDAO.class);
        CRFDAO cdao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), CRFDAO.class);
        CRFVersionDAO cvdao = org.akaza.openclinica.control.SpringServletAccess.getDao(getServletContext(), CRFVersionDAO.class);

        StudyAuditLogTableFactory factory = new StudyAuditLogTableFactory();
        factory.setSubjectDao(sdao);
        factory.setStudySubjectDao(subdao);
        factory.setUserAccountDao(uadao);
        factory.setCurrentStudy(currentStudy);

        String auditLogsHtml = factory.createTable(request, response).render();
        request.setAttribute("auditLogsHtml", auditLogsHtml);

        forwardPage(Page.AUDIT_LOGS_STUDY);

    }

    /*
     * (non-Javadoc) Since access to this servlet is admin-only, restricts user
     * to see logs of specific users only @author thickerson
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        if (ub.isSysAdmin()) {
            return;
        }

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");
    }

}
