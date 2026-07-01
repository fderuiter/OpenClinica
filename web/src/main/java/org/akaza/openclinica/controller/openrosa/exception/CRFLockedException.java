package org.akaza.openclinica.controller.openrosa.exception;

public class CRFLockedException extends RuntimeException {
    public CRFLockedException(String message) {
        super(message);
    }
}
