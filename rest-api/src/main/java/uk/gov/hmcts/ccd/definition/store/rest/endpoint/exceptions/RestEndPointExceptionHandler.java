package uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.ccd.definition.store.domain.exception.BadRequestException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.Set;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class RestEndPointExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Integer MAX_DEPTH = 5;
    private static Logger log = LoggerFactory.getLogger(RestEndPointExceptionHandler.class);

    @ExceptionHandler(value = {IOException.class, PersistenceException.class})
    public ResponseEntity<Object> handleException(RuntimeException ex, WebRequest request) {
        log.error("Exception thrown '{}'", ex.getMessage(), ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {OptimisticLockException.class})
    public ResponseEntity<Object> handleConflict(PersistenceException ex, WebRequest request) {
        log.error("Exception thrown '{}'", ex.getMessage(), ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {ConcurrencyFailureException.class})
    public ResponseEntity<Object> handleConcurrencyFailure(ConcurrencyFailureException ex, WebRequest request) {
        log.error("Exception thrown '{}'", ex.getMessage(), ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<Object> handleNotFound(NotFoundException ex, WebRequest request) {
        log.error("Exception thrown '{}'", ex.getMessage(), ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintVioldation(ConstraintViolationException ex, WebRequest request) {
        log.error("Exception thrown '{}'", ex.getMessage(), ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {BadRequestException.class})
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex, WebRequest request) {
        log.error("Exception thrown '{}'", ex.getMessage(), ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.BAD_REQUEST, request);
    }

    private String flattenExceptionMessages(Throwable ex) {
        final StringBuilder sb = new StringBuilder(ex.getMessage());

        Integer remaining = MAX_DEPTH;
        Throwable inner = ex;
        while ((inner = inner.getCause()) != null && 0 < --remaining) {
            log.debug("Remaining '{}' out of '{}'", remaining, MAX_DEPTH);
            sb.append("\n").append(inner.getMessage());
        }

        return sb.toString();
    }

    private String getMessagesAsString(Set<String> messages) {
        StringBuilder result = new StringBuilder();
        messages.remove(null);
        for (String message : messages) {
            result.append(message + ". ");
        }
        return result.toString();
    }
}
