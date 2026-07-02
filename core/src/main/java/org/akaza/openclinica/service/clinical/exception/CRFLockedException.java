package org.akaza.openclinica.service.clinical.exception;

public class CRFLockedException extends RuntimeException {
    public CRFLockedException(String message) {
        super(message);
    }
}
