/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.Locale;

/**
 * Processes user request and generate subject list
 *
 * @author jxu
 */
public class ListSubjectServlet extends SecureController {
    Locale locale;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (ub.isSysAdmin()) {
            return;
        }

        // Let the DAO permissions handle what data the user can see.
        return;

    }

    @Override
    public void processRequest() throws Exception {
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());

        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        UserAccountDAO uadao = new UserAccountDAO(sm.getDataSource());

        ListSubjectTableFactory factory = new ListSubjectTableFactory();
        factory.setSubjectDao(sdao);
        factory.setStudySubjectDao(subdao);
        factory.setUserAccountDao(uadao);
        factory.setStudyDao(studyDao);
        factory.setCurrentStudy(currentStudy);


        String auditLogsHtml = factory.createTable(request, response).render();
        request.setAttribute("listSubjectsHtml", auditLogsHtml);

        forwardPage(Page.SUBJECT_LIST);
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

}
