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
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.exception.FileStorageException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.ccd.error.ErrorCode;
import java.util.stream.Collectors;

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

    private static Logger log = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    private SpreadsheetValidationErrorMessageCreator spreadsheetValidationErrorMessageCreator;

    public RestResponseEntityExceptionHandler(
        SpreadsheetValidationErrorMessageCreator spreadsheetValidationErrorMessageCreator) {
        this.spreadsheetValidationErrorMessageCreator = spreadsheetValidationErrorMessageCreator;
    }

    @ExceptionHandler(value = {InvalidImportException.class, MapperException.class})
    ResponseEntity<Object> handleBadRequest(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(
            ex, HttpStatus.BAD_REQUEST.getReasonPhrase(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = MissingAccessProfilesException.class)
    ResponseEntity<Object> handleAccessProfilesMissing(MissingAccessProfilesException ex, WebRequest request) {
        String missingAccessProfiles = new StringBuilder("Missing AccessProfiles.\n\n")
            .append(ex.getMissingAccessProfiles()
                .stream()
                .collect(Collectors.joining("\n"))).toString();

        String validationErrors = getValidationErrorMessage(
            "\n\nValidation errors occurred importing the spreadsheet.\n\n", ex.getValidationErrors());

        return handleExceptionInternal(
            ex, missingAccessProfiles + validationErrors, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    private HttpHeaders responseContentType() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        return headers;
    }

    @ExceptionHandler(value = {ValidationException.class})
    public ResponseEntity<Object> handleValidationException(ValidationException validationException,
                                                            WebRequest request) {

        String errorMessage = getValidationErrorMessage(
            "Validation errors occurred importing the spreadsheet.\n\n",
            validationException.getValidationResult().getValidationErrors());

        return handleExceptionInternal(
            validationException, errorMessage, responseContentType(), HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler(CaseTypeValidationException.class)
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    String caseTypeValidation(CaseTypeValidationException e) {
        log.warn("Case type validation failed", e);
        return getMessagesAsString(e.getErrors());
    }

    @ExceptionHandler(FileStorageException.class)
    public void handleFileStorageException(HttpServletResponse response,
                                           FileStorageException fileStorageException) throws IOException {
        log.error("File storage request failed", fileStorageException);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler(FeignException.FeignClientException.class)
    @ResponseBody
    public ResponseEntity<Object> handleFeignClientException(FeignException.FeignClientException exception,
                                                             WebRequest request) {

        int status = exception.status();
        if (status != HttpStatus.UNAUTHORIZED.value()) {
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        return handleExceptionInternal(exception, HttpStatus.valueOf(status).getReasonPhrase(), new HttpHeaders(),
            HttpStatus.valueOf(status), request);
    }

    @ExceptionHandler(FeignException.FeignServerException.class)
    @ResponseBody
    public void handleFeignServerException(FeignException.FeignServerException exception, HttpServletResponse response)
        throws IOException {

        log.error("Upstream request failed", exception);

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

        log.error("Upstream request failed", exception);

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

        int status = exception.getRawStatusCode();
        if (status != HttpStatus.UNAUTHORIZED.value()) {
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        return handleExceptionInternal(exception, HttpStatus.valueOf(status).getReasonPhrase(), new HttpHeaders(),
            HttpStatus.valueOf(status), request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
                                                              HttpHeaders headers, HttpStatusCode status,
                                                              WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("Request failed: code={} status={}",
                ErrorCode.fromStatus(status.value()), status.value(), exception);
        } else {
            log.warn("Request failed: code={} status={}",
                ErrorCode.fromStatus(status.value()), status.value(), exception);
        }
        Object safeBody = exception instanceof ValidationException
            || exception instanceof MissingAccessProfilesException
            ? body : Map.of("message", exception instanceof ImportValidationException validation
                ? validation.getPublicMessage() : HttpStatus.valueOf(status.value()).getReasonPhrase(),
                "code", ErrorCode.fromStatus(status.value()));
        return super.handleExceptionInternal(exception, safeBody, headers, status, request);
    }

    private String getMessagesAsString(Set<String> messages) {
        StringBuilder result = new StringBuilder();
        messages.remove(null);
        for (String message : messages) {
            result.append(message + ". ");
        }
        return result.toString();
    }

    private String getValidationErrorMessage(String message, List<ValidationError> validationErrors) {
        return new StringBuilder(message)
            .append(validationErrors
                .stream()
                .map(validationError -> String.format(
                    "- %s",
                    validationError.createMessage(this.spreadsheetValidationErrorMessageCreator)
                    )
                )
                .collect(Collectors.joining("\n"))).toString();
    }
}
