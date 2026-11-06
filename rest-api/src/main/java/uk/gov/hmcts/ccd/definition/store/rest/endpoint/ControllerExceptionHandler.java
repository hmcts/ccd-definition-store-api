package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.google.common.collect.ImmutableMap;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.ccd.error.SafeErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.ccd.definition.store.domain.exception.BadRequestException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.DuplicateFoundException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(value = {PersistenceException.class})
    public ResponseEntity<Object> handleException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(),
            INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {OptimisticLockException.class})
    public ResponseEntity<Object> handleConflict(PersistenceException ex, WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(),
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {ConcurrencyFailureException.class})
    public ResponseEntity<Object> handleConcurrencyFailure(ConcurrencyFailureException ex, WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(),
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(),
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {jakarta.validation.ConstraintViolationException.class})
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleMethodConstraintViolation(jakarta.validation.ConstraintViolationException ex) {
        SafeErrorResponse.log(LOG, BAD_REQUEST, ex);
        String message = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        return ImmutableMap.of("message", "Validation failed: " + message);
    }

    @ExceptionHandler(value = {BadRequestException.class})
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex, WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(),
            HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(code = NOT_FOUND)
    @ResponseBody
    Map<String, String> objectNotFound(NotFoundException e) {
        SafeErrorResponse.log(LOG, NOT_FOUND, e);
        return SafeErrorResponse.body(NOT_FOUND);
    }

    @ExceptionHandler(DuplicateFoundException.class)
    @ResponseStatus(code = CONFLICT)
    @ResponseBody
    Map<String, String> objectFound(DuplicateFoundException e) {
        SafeErrorResponse.log(LOG, CONFLICT, e);
        return SafeErrorResponse.body(CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    Map<String, String> generalIllegal(IllegalArgumentException e) {
        SafeErrorResponse.log(LOG, BAD_REQUEST, e);
        return SafeErrorResponse.body(BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(code = INTERNAL_SERVER_ERROR)
    @ResponseBody
    Map<String, String> handleIOException(IOException e) {
        SafeErrorResponse.log(LOG, INTERNAL_SERVER_ERROR, e);
        return SafeErrorResponse.body(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CaseTypeValidationException.class)
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    String caseTypeValidation(CaseTypeValidationException e) {
        SafeErrorResponse.log(LOG, BAD_REQUEST, e);
        return getMessagesAstring(e.getErrors());
    }

    @ExceptionHandler(ElasticSearchInitialisationException.class)
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    Map<String, String> elasticSearchInitialisationException(ElasticSearchInitialisationException e) {
        SafeErrorResponse.log(LOG, BAD_REQUEST, e);
        return SafeErrorResponse.body(BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
                                                              HttpHeaders headers, HttpStatusCode status,
                                                              WebRequest request) {
        SafeErrorResponse.log(LOG, status, exception);
        return super.handleExceptionInternal(exception, SafeErrorResponse.body(status),
            headers, status, request);
    }

    private String getMessagesAstring(Set<String> messages) {
        StringBuilder result = new StringBuilder();
        for (String message : messages) {
            result.append(message).append(". ");
        }
        return result.toString();
    }

}
