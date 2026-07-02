package org.akaza.openclinica.controller.validator;

import org.akaza.openclinica.bean.login.EventDefinitionDTO;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class EventDefinitionValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return EventDefinitionDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotBlank.event.name", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "repeating", "NotBlank.event.repeating", "This field cannot be blank.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "NotBlank.event.type", "This field cannot be blank.");

        EventDefinitionDTO dto = (EventDefinitionDTO) target;
        
        if (dto.getName() != null && dto.getName().length() > 2000) {
            errors.rejectValue("name", "Length.event.name", "The Length Should not exceed 2000.");
        }
        if (dto.getDescription() != null && dto.getDescription().length() > 2000) {
            errors.rejectValue("description", "Length.event.description", "The Length Should not exceed 2000.");
        }
        if (dto.getCategory() != null && dto.getCategory().length() > 2000) {
            errors.rejectValue("category", "Length.event.category", "The Length Should not exceed 2000.");
        }
    }
}
