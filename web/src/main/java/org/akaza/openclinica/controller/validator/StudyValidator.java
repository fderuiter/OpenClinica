package org.akaza.openclinica.controller.validator;

import org.akaza.openclinica.bean.login.StudyDTO;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class StudyValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return StudyDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyDTO dto = (StudyDTO) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "briefTitle", "field.required", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "uniqueProtocolID", "field.required", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "briefSummary", "field.required", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "principalInvestigator", "field.required", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sponsor", "field.required", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDate", "field.required", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "expectedTotalEnrollment", "field.required", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "protocolType", "field.required", "This field cannot be blank.");
        if (dto.getAssignUserRoles() == null || dto.getAssignUserRoles().isEmpty()) {
            errors.rejectValue("assignUserRoles", "field.required", "Missing Field");
        }
    }
}
