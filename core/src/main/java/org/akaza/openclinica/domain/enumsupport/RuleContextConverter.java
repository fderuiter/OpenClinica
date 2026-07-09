package org.akaza.openclinica.domain.enumsupport;

import org.akaza.openclinica.domain.rule.expression.Context;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RuleContextConverter implements AttributeConverter<Context, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Context status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public Context convertToEntityAttribute(Integer id) {
        return id != null ? (Context) Context.getByCode(id) : null;
    }
}
