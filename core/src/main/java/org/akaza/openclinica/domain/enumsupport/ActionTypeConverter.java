package org.akaza.openclinica.domain.enumsupport;

import org.akaza.openclinica.domain.rule.action.ActionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActionTypeConverter implements AttributeConverter<ActionType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ActionType status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public ActionType convertToEntityAttribute(Integer id) {
        return id != null ? (ActionType) ActionType.getByCode(id) : null;
    }
}
