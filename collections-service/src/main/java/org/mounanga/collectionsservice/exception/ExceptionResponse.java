package org.mounanga.collectionsservice.exception;

import java.util.Collection;

public record ExceptionResponse(int status, String message, String error, Collection<String> validationErrors) {
}
