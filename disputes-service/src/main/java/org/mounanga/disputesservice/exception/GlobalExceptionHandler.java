package org.mounanga.disputesservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exception) {
        Set<String> validationErrors = new HashSet<>();
        exception.getBindingResult().getAllErrors().forEach(error -> validationErrors.add(error.getDefaultMessage()));

        return ResponseEntity.status(BAD_REQUEST).body(new ExceptionResponse(
                BAD_REQUEST.value(),
                exception.getMessage(),
                exception.getLocalizedMessage(),
                validationErrors
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleException(ConstraintViolationException exception) {
        Set<String> validationErrors = new HashSet<>();
        exception.getConstraintViolations().forEach(violation -> validationErrors.add(violation.getMessage()));

        return ResponseEntity.status(BAD_REQUEST).body(new ExceptionResponse(
                BAD_REQUEST.value(),
                exception.getMessage(),
                exception.getLocalizedMessage(),
                validationErrors
        ));
    }

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ExceptionResponse> handleException(FieldValidationException exception) {
        return ResponseEntity.status(BAD_REQUEST).body(new ExceptionResponse(
                BAD_REQUEST.value(),
                exception.getMessage(),
                exception.getLocalizedMessage(),
                new HashSet<>(exception.getErrors())
        ));
    }

    @ExceptionHandler({DisputeNotFoundException.class, AccountNotFoundException.class})
    public ResponseEntity<ExceptionResponse> handleNotFoundException(RuntimeException exception) {
        return ResponseEntity.status(NOT_FOUND).body(new ExceptionResponse(
                NOT_FOUND.value(),
                exception.getMessage(),
                exception.getLocalizedMessage(),
                new HashSet<>()
        ));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ExceptionResponse> handleException(OptimisticLockingFailureException exception) {
        return ResponseEntity.status(CONFLICT).body(new ExceptionResponse(
                CONFLICT.value(),
                "Dispute was modified concurrently, please retry",
                exception.getLocalizedMessage(),
                new HashSet<>()
        ));
    }

    @ExceptionHandler(AccountServiceUnavailableException.class)
    public ResponseEntity<ExceptionResponse> handleException(AccountServiceUnavailableException exception) {
        return ResponseEntity.status(BAD_GATEWAY).body(new ExceptionResponse(
                BAD_GATEWAY.value(),
                exception.getMessage(),
                exception.getLocalizedMessage(),
                new HashSet<>()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ExceptionResponse(
                INTERNAL_SERVER_ERROR.value(),
                exception.getMessage(),
                exception.getLocalizedMessage(),
                new HashSet<>()
        ));
    }
}
