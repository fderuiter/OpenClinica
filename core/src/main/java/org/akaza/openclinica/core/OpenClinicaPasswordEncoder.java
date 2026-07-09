package org.akaza.openclinica.core;

import org.springframework.security.crypto.password.PasswordEncoder;

public class OpenClinicaPasswordEncoder implements PasswordEncoder {

    PasswordEncoder currentPasswordEncoder;
    PasswordEncoder oldPasswordEncoder;

    public OpenClinicaPasswordEncoder() {
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return currentPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return currentPasswordEncoder.matches(rawPassword, encodedPassword) || oldPasswordEncoder.matches(rawPassword, encodedPassword);
    }

    public PasswordEncoder getCurrentPasswordEncoder() {
        return currentPasswordEncoder;
    }

    public void setCurrentPasswordEncoder(PasswordEncoder currentPasswordEncoder) {
        this.currentPasswordEncoder = currentPasswordEncoder;
    }

    public PasswordEncoder getOldPasswordEncoder() {
        return oldPasswordEncoder;
    }

    public void setOldPasswordEncoder(PasswordEncoder oldPasswordEncoder) {
        this.oldPasswordEncoder = oldPasswordEncoder;
    }

}
