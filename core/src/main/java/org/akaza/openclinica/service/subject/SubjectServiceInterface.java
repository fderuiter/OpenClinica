package org.akaza.openclinica.service.subject;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;

import java.util.Date;
import java.util.List;

public interface SubjectServiceInterface {

    public String generateSubjectId(StudyBean studyBean);

    public abstract String createSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId);
    public SubjectBean updateSubject(SubjectBean subject, org.akaza.openclinica.bean.login.UserAccountBean updater, String reasonForChange, org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean note);

    public List<StudySubjectBean> getStudySubject(StudyBean study);

}