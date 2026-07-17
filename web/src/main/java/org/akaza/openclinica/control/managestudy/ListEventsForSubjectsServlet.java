/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.Locale;

/**
 * @author Krikor Krumlian
 */
@Component
public class ListEventsForSubjectsServlet extends SecureController {
    private CRFDAO _cRFDAO;
    private CRFVersionDAO _cRFVersionDAO;
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
    public ListEventsForSubjectsServlet(CRFDAO _cRFDAO, CRFVersionDAO _cRFVersionDAO, EventCRFDAO _eventCRFDAO, EventDefinitionCRFDAO _eventDefinitionCRFDAO, StudyDAO _studyDAO, StudyEventDAO _studyEventDAO, StudyEventDefinitionDAO _studyEventDefinitionDAO, StudyGroupClassDAO _studyGroupClassDAO, StudyGroupDAO _studyGroupDAO, StudySubjectDAO _studySubjectDAO, SubjectDAO _subjectDAO, SubjectGroupMapDAO _subjectGroupMapDAO) {
        this._cRFDAO = _cRFDAO;
        this._cRFVersionDAO = _cRFVersionDAO;
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


    // Shaoyu Su
    private static final long serialVersionUID = 1L;
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
    private CRFDAO crfDAO;
    Locale locale;
    private boolean showMoreLink;
	private Object crfVersionDAO;
    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        if(fp.getString("showMoreLink").equals("")){
            showMoreLink = true;
        }else {
            showMoreLink = Boolean.parseBoolean(fp.getString("showMoreLink"));
        }
        String idSetting = currentStudy.getStudyParameterConfig().getSubjectIdGeneration();
        // set up auto study subject id
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            //Shaoyu Su
            //int nextLabel = getStudySubjectDAO().findTheGreatestLabel() + 1;
            //request.setAttribute("label", new Integer(nextLabel).toString());
            request.setAttribute("label", resword.getString("id_generated_Save_Add"));
        }

        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int definitionId = fp.getInt("defId");
        if (definitionId <= 0) {
            addPageMessage(respage.getString("please_choose_an_ED_ta_to_vies_details"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
            return;
        }

        ListEventsForSubjectTableFactory factory = new ListEventsForSubjectTableFactory(showMoreLink);
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
        factory.setCrfVersionDAO(getCRFVersionDAO());
        factory.setSelectedStudyEventDefinition((StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(definitionId));
        String listEventsForSubjectsHtml = factory.createTable(request, response).render();
        request.setAttribute("listEventsForSubjectsHtml", listEventsForSubjectsHtml);
        request.setAttribute("defId", definitionId);
        // A. Hamid.
        // For event definitions and group class list in the add subject popup
        request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
        request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
        request.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
        //
        forwardPage(Page.LIST_EVENTS_FOR_SUBJECTS);

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

    public CRFVersionDAO getCRFVersionDAO(){
    	CRFVersionDAO	crfVersionDAO =this._cRFVersionDAO;
    	return crfVersionDAO;
    	}
    public StudyGroupDAO getStudyGroupDAO() {
        studyGroupDAO = this.studyGroupDAO == null ? this._studyGroupDAO : studyGroupDAO;
        return studyGroupDAO;
    }

}
