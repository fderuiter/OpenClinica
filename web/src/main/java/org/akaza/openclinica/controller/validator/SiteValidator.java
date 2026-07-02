package org.akaza.openclinica.controller.validator;

import org.akaza.openclinica.bean.login.SiteDTO;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class SiteValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return SiteDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "uniqueSiteProtocolID", "NotBlank.site.uniqueSiteProtocolID", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotBlank.site.name", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "principalInvestigator", "NotBlank.site.principalInvestigator", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "expectedTotalEnrollment", "NotBlank.site.expectedTotalEnrollment", "This field cannot be blank.");
    }
}
