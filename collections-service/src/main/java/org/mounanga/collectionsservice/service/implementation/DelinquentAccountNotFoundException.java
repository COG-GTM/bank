package org.mounanga.collectionsservice.service.implementation;

public class DelinquentAccountNotFoundException extends RuntimeException {

    public DelinquentAccountNotFoundException(String message) {
        super(message);
    }
}
