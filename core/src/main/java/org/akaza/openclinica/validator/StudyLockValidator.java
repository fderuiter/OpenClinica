package org.akaza.openclinica.validator;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.springframework.validation.Errors;

public class StudyLockValidator {

    public static boolean isStudyLockedOrFrozen(StudyBean study, StudyDAO studyDAO) {
        if (study == null) {
            return false;
        }
        if (study.getStatus() == Status.LOCKED || study.getStatus() == Status.FROZEN) {
            return true;
        }
        if (study.getParentStudyId() > 0 && studyDAO != null) {
            StudyBean parentStudy = (StudyBean) studyDAO.findByPK(study.getParentStudyId());
            if (parentStudy != null && (parentStudy.getStatus() == Status.LOCKED || parentStudy.getStatus() == Status.FROZEN)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean validateStudyLockStatus(StudyBean study, StudyDAO studyDAO, String studyIdentifier, Errors errors) {
        if (isStudyLockedOrFrozen(study, studyDAO)) {
            errors.reject("subjectTransferValidator.study_status_wrong", new Object[] { studyIdentifier }, "Study or its parent study is locked or frozen. No clinical data imports are allowed.");
            return false;
        }
        return true;
    }
}
