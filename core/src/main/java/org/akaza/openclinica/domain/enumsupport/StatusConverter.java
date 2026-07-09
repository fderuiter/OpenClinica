package org.akaza.openclinica.domain.enumsupport;

import org.akaza.openclinica.bean.core.Status;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Status status) {
        return status != null ? status.getId() : null;
    }

    @Override
    public Status convertToEntityAttribute(Integer id) {
        return id != null ? Status.get(id) : null;
    }
}
