package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.MissingAccessProfilesException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.exception.FileStorageException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Global exception handler for the Case Definition Importer
 * {@link uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController
 * ImportController} class, providing appropriate HTTP responses based on the exceptions caught.
 *
 * @author Daniel Lam (A533913)
 */
@ControllerAdvice
class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String BAD_REQUEST_ERROR_MSG = "Bad request";
    private static final String VALIDATION_ERROR_MSG = "Validation failed";
    private static final String IMPORT_VALIDATION_ERROR_MSG = "Validation errors occurred importing the spreadsheet.";
    private static final String INTERNAL_SERVER_ERROR_MSG = "An internal server error occurred";
    private static Logger log = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    public RestResponseEntityExceptionHandler(
        SpreadsheetValidationErrorMessageCreator spreadsheetValidationErrorMessageCreator) {
    }

    @ExceptionHandler(value = {InvalidImportException.class, MapperException.class})
    ResponseEntity<Object> handleBadRequest(RuntimeException ex, WebRequest request) {
        log.error("Exception thrown '{}'", ex.getMessage(), ex);
        return handleExceptionInternal(
            ex, BAD_REQUEST_ERROR_MSG, responseContentType(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = MissingAccessProfilesException.class)
    ResponseEntity<Object> handleAccessProfilesMissing(MissingAccessProfilesException ex, WebRequest request) {
        log.warn("Missing access profiles while importing spreadsheet", ex);
        return handleExceptionInternal(
            ex, VALIDATION_ERROR_MSG, responseContentType(), HttpStatus.BAD_REQUEST, request);
    }

    private HttpHeaders responseContentType() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        return headers;
    }

    @ExceptionHandler(value = {ValidationException.class})
    public ResponseEntity<Object> handleValidationException(ValidationException validationException,
                                                            WebRequest request) {

        log.warn("Validation failed while importing spreadsheet", validationException);
        return handleExceptionInternal(
            validationException, IMPORT_VALIDATION_ERROR_MSG, responseContentType(),
            HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler(CaseTypeValidationException.class)
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    String caseTypeValidation(CaseTypeValidationException e) {
        log.error("Exception thrown {}", e.getMessage(), e);
        return VALIDATION_ERROR_MSG;
    }

    @ExceptionHandler(FileStorageException.class)
    public void handleFileStorageException(HttpServletResponse response,
                                           FileStorageException fileStorageException) throws IOException {
        log.error("Exception thrown: {}", fileStorageException.getMessage(), fileStorageException);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler(FeignException.FeignClientException.class)
    @ResponseBody
    public ResponseEntity<Object> handleFeignClientException(FeignException.FeignClientException exception,
                                                             WebRequest request) {
        log.error(exception.getMessage(), exception);

        int status = exception.status();
        if (status != HttpStatus.UNAUTHORIZED.value()) {
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        return handleExceptionInternal(exception, INTERNAL_SERVER_ERROR_MSG, responseContentType(),
            HttpStatus.valueOf(status), request);
    }

    @ExceptionHandler(FeignException.FeignServerException.class)
    @ResponseBody
    public void handleFeignServerException(FeignException.FeignServerException exception, HttpServletResponse response)
        throws IOException {
        log.error(exception.getMessage(), exception);

        int status = exception.status();
        if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            status = HttpStatus.BAD_GATEWAY.value();
        }

        response.sendError(status);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    @ResponseBody
    public void handleHttpServerErrorException(HttpServerErrorException exception, HttpServletResponse response)
        throws IOException {
        log.error(exception.getMessage(), exception);

        int status = exception.getRawStatusCode();
        if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            status = HttpStatus.BAD_GATEWAY.value();
        }

        response.sendError(status);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseBody
    public ResponseEntity<Object> handleHttpClientErrorException(HttpClientErrorException exception,
                                                                 WebRequest request) {
        log.error(exception.getMessage(), exception);

        int status = exception.getRawStatusCode();
        if (status != HttpStatus.UNAUTHORIZED.value()) {
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        return handleExceptionInternal(exception, INTERNAL_SERVER_ERROR_MSG, responseContentType(),
            HttpStatus.valueOf(status), request);
    }

}
