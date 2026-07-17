package org.akaza.openclinica.controller.migration;

import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.submit.ListDiscNotesSubjectTableFactory;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Controller
public class ListDiscNotesSubjectController {

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    public static final String RESOLUTION_STATUS = "resolutionStatus";
    public static final String DISCREPANCY_NOTE_TYPE = "discrepancyNoteType";
    public static final String FILTER_SUMMARY = "filterSummary";

    @RequestMapping(value = "/ListDiscNotesSubjectServlet", method = {RequestMethod.GET, RequestMethod.POST})
    public String listDiscNotesSubject(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
        StudyUserRoleBean currentRole = (StudyUserRoleBean) session.getAttribute("userRole");
        StudyBean currentStudy = (StudyBean) session.getAttribute("study");

        // Authorization check
        boolean authorized = false;
        if (ub != null && ub.isSysAdmin()) {
            authorized = true;
        } else if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            authorized = true;
        }

        if (!authorized) {
            // In SecureController this throws InsufficientPermissionException and redirects
            // We can just redirect to the main page or error page
            return "redirect:/Menu";
        }

        String module = request.getParameter("module");
        String moduleStr = "manage";
        if (module != null && module.trim().length() > 0) {
            if ("submit".equals(module)) {
                request.setAttribute("module", "submit");
                moduleStr = "submit";
            } else if ("admin".equals(module)) {
                request.setAttribute("module", "admin");
                moduleStr = "admin";
            } else {
                request.setAttribute("module", "manage");
            }
        }

        request.setAttribute("closeInfoShowIcons", true);

        int resolutionStatus = 0;
        try {
            resolutionStatus = Integer.parseInt(request.getParameter("resolutionStatus"));
        } catch (NumberFormatException nfe) {
            resolutionStatus = -1;
        }

        boolean hasAResolutionStatus = resolutionStatus >= 1 && resolutionStatus <= 5;
        Set<Integer> resolutionStatusIds = (HashSet) session.getAttribute(RESOLUTION_STATUS);
        
        if (!hasAResolutionStatus && resolutionStatusIds != null) {
            request.removeAttribute(RESOLUTION_STATUS);
            resolutionStatusIds = null;
        }
        if (hasAResolutionStatus) {
            if (resolutionStatusIds == null) {
                resolutionStatusIds = new HashSet<Integer>();
            }
            resolutionStatusIds.add(resolutionStatus);
            request.setAttribute(RESOLUTION_STATUS, resolutionStatusIds);
        }

        int discNoteType = 0;
        try {
            discNoteType = Integer.parseInt(request.getParameter("type"));
        } catch (NumberFormatException nfe) {
            discNoteType = -1;
        }
        request.setAttribute(DISCREPANCY_NOTE_TYPE, discNoteType);

        DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();
        Map<String, List<String>> filterSummary = discNoteUtil.generateFilterSummary(discNoteType, resolutionStatusIds);

        if (!filterSummary.isEmpty()) {
            request.setAttribute(FILTER_SUMMARY, filterSummary);
        }
        Locale locale = LocaleResolver.getLocale(request);

        Map stats = discNoteUtil.generateDiscNoteSummaryRefactored(dataSource, currentStudy, resolutionStatusIds, discNoteType);
        request.setAttribute("summaryMap", stats);
        Set mapKeys = stats.keySet();
        request.setAttribute("mapKeys", mapKeys);

        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudySubjectDAO sdao = new StudySubjectDAO(dataSource);
        StudyEventDAO sedao = new StudyEventDAO(dataSource);
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(dataSource);
        SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(dataSource);
        StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(dataSource);
        StudyGroupDAO sgdao = new StudyGroupDAO(dataSource);
        StudySubjectDAO ssdao = new StudySubjectDAO(dataSource);
        EventCRFDAO edao = new EventCRFDAO(dataSource);
        EventDefinitionCRFDAO eddao = new EventDefinitionCRFDAO(dataSource);
        SubjectDAO subdao = new SubjectDAO(dataSource);
        DiscrepancyNoteDAO dnDAO = new DiscrepancyNoteDAO(dataSource);

        ListDiscNotesSubjectTableFactory factory = new ListDiscNotesSubjectTableFactory(ResourceBundleProvider.getTermsBundle(locale));
        factory.setStudyEventDefinitionDao(seddao);
        factory.setSubjectDAO(subdao);
        factory.setStudySubjectDAO(sdao);
        factory.setStudyEventDAO(sedao);
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(sgcdao);
        factory.setSubjectGroupMapDAO(sgmdao);
        factory.setStudyDAO(studyDAO);
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(edao);
        factory.setEventDefintionCRFDAO(eddao);
        factory.setStudyGroupDAO(sgdao);
        factory.setDiscrepancyNoteDAO(dnDAO);

        factory.setModule(moduleStr);
        factory.setDiscNoteType(discNoteType);
        factory.setResolutionStatus(resolutionStatus);
        factory.setResolutionStatusIds(resolutionStatusIds);
        factory.setResword(ResourceBundleProvider.getWordsBundle(locale));
        String listDiscNotesHtml = factory.createTable(request, response).render();
        request.setAttribute("listDiscNotesHtml", listDiscNotesHtml);

        return "managestudy/listSubjectDiscNote";
    }
}
