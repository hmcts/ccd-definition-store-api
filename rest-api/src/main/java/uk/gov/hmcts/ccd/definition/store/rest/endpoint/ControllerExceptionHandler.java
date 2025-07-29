package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.google.common.collect.ImmutableMap;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import uk.gov.hmcts.ccd.definition.store.domain.exception.BadRequestException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.DuplicateFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Integer MAX_DEPTH = 5;
    private static final String EXCEPTION_THROWN = "Exception thrown ";
    private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(value = {PersistenceException.class})
    public ResponseEntity<Object> handleException(RuntimeException ex, WebRequest request) {
        log(ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {OptimisticLockException.class})
    public ResponseEntity<Object> handleConflict(PersistenceException ex, WebRequest request) {
        log(ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {ConcurrencyFailureException.class})
    public ResponseEntity<Object> handleConcurrencyFailure(ConcurrencyFailureException ex, WebRequest request) {
        log(ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintVioldation(ConstraintViolationException ex, WebRequest request) {
        log(ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {BadRequestException.class})
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex, WebRequest request) {
        log(ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(),
            HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(code = NOT_FOUND)
    @ResponseBody
    Map<String, String> objectNotFound(NotFoundException e) {
        log(e);
        return getMessage(e, "Object Not Found for:%s");
    }

    @ExceptionHandler(DuplicateFoundException.class)
    @ResponseStatus(code = CONFLICT)
    @ResponseBody
    Map<String, String> objectFound(DuplicateFoundException e) {
        log(e);
        return getMessage(e, "Object already exists for:%s");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    Map<String, String> generalIllegal(IllegalArgumentException e) {
        log(e);
        return getMessage(e, "Illegal Input:%s");
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(code = INTERNAL_SERVER_ERROR)
    @ResponseBody
    Map<String, String> handleIOException(IOException e) {
        log(e);
        return getMessage(e, "Unexpected Error: " + e.getMessage());
    }

    @ExceptionHandler(CaseTypeValidationException.class)
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    String caseTypeValidation(CaseTypeValidationException e) {
        log(e);
        return getMessagesAstring(e.getErrors());
    }

    @ExceptionHandler(ElasticSearchInitialisationException.class)
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    Map<String, String> elasticSearchInitialisationException(ElasticSearchInitialisationException e) {
        LOG.error("ElasticSearch initialisation exception", e);
        return getMessage(e, "ElasticSearch initialisation exception: %s");
    }

    private String flattenExceptionMessages(Throwable ex) {
        final StringBuilder sb = new StringBuilder(ex.getMessage());

        Integer remaining = MAX_DEPTH;
        Throwable inner = ex;
        while ((inner = inner.getCause()) != null && 0 < --remaining) {
            LOG.debug("Remaining '{}' out of '{}'", remaining, MAX_DEPTH);
            sb.append("\n").append(inner.getMessage());
        }

        return sb.toString();
    }

    private Map<String, String> getMessage(Throwable e, String message) {
        return ImmutableMap.of("message", String.format(message, e.getMessage()));
    }

    private String getMessagesAstring(Set<String> messages) {
        StringBuilder result = new StringBuilder();
        for (String message : messages) {
            result.append(message).append(". ");
        }
        return result.toString();
    }

    private void log(final Exception e) {
        LOG.debug(EXCEPTION_THROWN + "{}", e.getMessage(), e);
    }

}
