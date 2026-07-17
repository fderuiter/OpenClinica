package org.akaza.openclinica.control.managestudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 *
 */

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.ListDiscNotesForCRFTableFactory;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.admin.CRFDAO;
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
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class ListDiscNotesForCRFServlet extends SecureController {
    private CRFDAO _cRFDAO;
    private DiscrepancyNoteDAO _discrepancyNoteDAO;
    private EventCRFDAO _eventCRFDAO;
    private EventDefinitionCRFDAO _eventDefinitionCRFDAO;
    private StudyDAO _studyDAO;
    private StudyEventDAO _studyEventDAO;
    private StudyEventDefinitionDAO _studyEventDefinitionDAO;
    private StudyGroupClassDAO _studyGroupClassDAO;
    private StudyGroupDAO _studyGroupDAO;
    private StudySubjectDAO _studySubjectDAO;
    private SubjectDAO _subjectDAO;
    private SubjectGroupMapDAO _subjectGroupMapDAO;

    @Autowired
    public ListDiscNotesForCRFServlet(CRFDAO _cRFDAO, DiscrepancyNoteDAO _discrepancyNoteDAO, EventCRFDAO _eventCRFDAO, EventDefinitionCRFDAO _eventDefinitionCRFDAO, StudyDAO _studyDAO, StudyEventDAO _studyEventDAO, StudyEventDefinitionDAO _studyEventDefinitionDAO, StudyGroupClassDAO _studyGroupClassDAO, StudyGroupDAO _studyGroupDAO, StudySubjectDAO _studySubjectDAO, SubjectDAO _subjectDAO, SubjectGroupMapDAO _subjectGroupMapDAO) {
        this._cRFDAO = _cRFDAO;
        this._discrepancyNoteDAO = _discrepancyNoteDAO;
        this._eventCRFDAO = _eventCRFDAO;
        this._eventDefinitionCRFDAO = _eventDefinitionCRFDAO;
        this._studyDAO = _studyDAO;
        this._studyEventDAO = _studyEventDAO;
        this._studyEventDefinitionDAO = _studyEventDefinitionDAO;
        this._studyGroupClassDAO = _studyGroupClassDAO;
        this._studyGroupDAO = _studyGroupDAO;
        this._studySubjectDAO = _studySubjectDAO;
        this._subjectDAO = _subjectDAO;
        this._subjectGroupMapDAO = _subjectGroupMapDAO;
    }


    public static final String DISCREPANCY_NOTE_TYPE = "discrepancyNoteType";
    public static final String RESOLUTION_STATUS = "resolutionStatus";
    public static final String FILTER_SUMMARY = "filterSummary";
    Locale locale;
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    private SubjectDAO subjectDAO;
    private StudySubjectDAO studySubjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyDAO studyDAO;
    private StudyGroupDAO studyGroupDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private DiscrepancyNoteDAO discrepancyNoteDAO;
    private CRFDAO crfDAO;

    // < ResourceBundleresword;
    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    public static boolean mayViewDN(UserAccountBean ub, StudyUserRoleBean currentRole) {
    	if (currentRole != null) {
            Role r = currentRole.getRole();

            if (r != null && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) ||
                    r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r.equals(Role.RESEARCHASSISTANT2) ||r.equals(Role.MONITOR) )) {
                return true;
            }
        }

        return false;
    }

    
    
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < resword =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",locale);

        if (ub.isSysAdmin()) {
            return;
        }

        
        if (ListDiscNotesForCRFServlet.mayViewDN(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        // Determine whether to limit the displayed DN's to a certain DN type
        int resolutionStatus = 0;
        try {
            resolutionStatus = Integer.parseInt(request.getParameter("resolutionStatus"));
        } catch (NumberFormatException nfe) {
            // Show all DN's
            resolutionStatus = -1;
        }
        // request.setAttribute(RESOLUTION_STATUS,resolutionStatus);

        // Determine whether we already have a collection of resolutionStatus
        // Ids, and if not
        // create a new attribute. If there is no resolution status, then the
        // Set object should be cleared,
        // because we do not have to save a set of filter IDs.
        boolean hasAResolutionStatus = resolutionStatus >= 1 && resolutionStatus <= 5;
        Set<Integer> resolutionStatusIds = (HashSet) session.getAttribute(RESOLUTION_STATUS);
        // remove the session if there is no resolution status
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
            // Show all DN's
            discNoteType = -1;
        }
        request.setAttribute(DISCREPANCY_NOTE_TYPE, discNoteType);

        /*
         * DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil(); //
         * Generate a summary of how we are filtering; Map<String, List<String>>
         * filterSummary = discNoteUtil.generateFilterSummary(discNoteType,
         * resolutionStatusIds);
         *
         * if (!filterSummary.isEmpty()) { request.setAttribute(FILTER_SUMMARY,
         * filterSummary); }
         */

        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int definitionId = fp.getInt("defId");
        int tabId = fp.getInt("tab");
        if (definitionId <= 0) {
            addPageMessage(respage.getString("please_choose_an_ED_ta_to_vies_details"));
            forwardPage(Page.LIST_SUBJECT_DISC_NOTE_SERVLET);
            return;
        }

        request.setAttribute("eventDefinitionId", definitionId);

        ListDiscNotesForCRFTableFactory factory = new ListDiscNotesForCRFTableFactory();
        factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
        factory.setSubjectDAO(getSubjectDAO());
        factory.setStudySubjectDAO(getStudySubjectDAO());
        factory.setStudyEventDAO(getStudyEventDAO());
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
        factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
        factory.setStudyDAO(getStudyDAO());
        factory.setStudyGroupDAO(getStudyGroupDAO());
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(getEventCRFDAO());
        factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
        factory.setCrfDAO(getCrfDAO());
        factory.setDiscrepancyNoteDAO(getDiscrepancyNoteDAO());
        // factory.setStudyHasDiscNotes(allThreadedDiscNotes != null &&
        // !allThreadedDiscNotes.isEmpty());
        factory.setDiscNoteType(discNoteType);
        factory.setModule(module);
        factory.setResolutionStatus(resolutionStatus);
        factory.setResolutionStatusIds(resolutionStatusIds);
        factory.setSelectedStudyEventDefinition((StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(definitionId));
        String listDiscNotesForCRFHtml = factory.createTable(request, response).render();
        request.setAttribute("listDiscNotesForCRFHtml", listDiscNotesForCRFHtml);
        request.setAttribute("defId", definitionId);

        forwardPage(Page.LIST_DNOTES_FOR_CRF);
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        studyEventDefinitionDAO = studyEventDefinitionDAO == null ? this._studyEventDefinitionDAO : studyEventDefinitionDAO;
        return studyEventDefinitionDAO;
    }

    public SubjectDAO getSubjectDAO() {
        subjectDAO = this.subjectDAO == null ? this._subjectDAO : subjectDAO;
        return subjectDAO;
    }

    public StudySubjectDAO getStudySubjectDAO() {
        studySubjectDAO = this.studySubjectDAO == null ? this._studySubjectDAO : studySubjectDAO;
        return studySubjectDAO;
    }

    public StudyGroupClassDAO getStudyGroupClassDAO() {
        studyGroupClassDAO = this.studyGroupClassDAO == null ? this._studyGroupClassDAO : studyGroupClassDAO;
        return studyGroupClassDAO;
    }

    public SubjectGroupMapDAO getSubjectGroupMapDAO() {
        subjectGroupMapDAO = this.subjectGroupMapDAO == null ? this._subjectGroupMapDAO : subjectGroupMapDAO;
        return subjectGroupMapDAO;
    }

    public StudyEventDAO getStudyEventDAO() {
        studyEventDAO = this.studyEventDAO == null ? this._studyEventDAO : studyEventDAO;
        return studyEventDAO;
    }

    public StudyDAO getStudyDAO() {
        studyDAO = this.studyDAO == null ? this._studyDAO : studyDAO;
        return studyDAO;
    }

    public EventCRFDAO getEventCRFDAO() {
        eventCRFDAO = this.eventCRFDAO == null ? this._eventCRFDAO : eventCRFDAO;
        return eventCRFDAO;
    }

    public EventDefinitionCRFDAO getEventDefinitionCRFDAO() {
        eventDefintionCRFDAO = this.eventDefintionCRFDAO == null ? this._eventDefinitionCRFDAO : eventDefintionCRFDAO;
        return eventDefintionCRFDAO;
    }

    public CRFDAO getCrfDAO() {
        crfDAO = this.crfDAO == null ? this._cRFDAO : crfDAO;
        return crfDAO;
    }

    public StudyGroupDAO getStudyGroupDAO() {
        studyGroupDAO = this.studyGroupDAO == null ? this._studyGroupDAO : studyGroupDAO;
        return studyGroupDAO;
    }

    public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
        discrepancyNoteDAO = this.discrepancyNoteDAO == null ? this._discrepancyNoteDAO : discrepancyNoteDAO;
        return discrepancyNoteDAO;
    }

}
