package org.mounanga.collectionsservice.exception;

public class DelinquentAccountNotFoundException extends RuntimeException {

    public DelinquentAccountNotFoundException(String message) {
        super(message);
    }
}
