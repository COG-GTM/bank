package org.mounanga.disputesservice.exception;

public class AccountServiceUnavailableException extends RuntimeException {
    public AccountServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
