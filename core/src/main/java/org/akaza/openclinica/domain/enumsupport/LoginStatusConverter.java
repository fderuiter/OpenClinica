package org.akaza.openclinica.domain.enumsupport;

import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LoginStatusConverter implements AttributeConverter<LoginStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(LoginStatus status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public LoginStatus convertToEntityAttribute(Integer id) {
        return id != null ? LoginStatus.getByCode(id) : null;
    }
}
