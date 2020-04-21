package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.DuplicateFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ControllerExceptionHandler {

    private static final String EXCEPTION_THROWN = "Exception thrown ";
    private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionHandler.class);

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
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
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
        return getMessage(e, "ElasticSearch initialisation exception:%s");
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
