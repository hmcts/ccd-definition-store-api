package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.exception.FileStorageException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Global exception handler for the Case Definition Importer {@link uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController
 * ImportController} class, providing appropriate HTTP responses based on the exceptions caught.
 *
 * @author Daniel Lam (A533913)
 */
@ControllerAdvice
class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Integer MAX_DEPTH = 5;
    private static Logger log = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    private SpreadsheetValidationErrorMessageCreator spreadsheetValidationErrorMessageCreator;

    public RestResponseEntityExceptionHandler(SpreadsheetValidationErrorMessageCreator spreadsheetValidationErrorMessageCreator) {
        this.spreadsheetValidationErrorMessageCreator = spreadsheetValidationErrorMessageCreator;
    }

    @ExceptionHandler(value = {InvalidImportException.class, MapperException.class})
    ResponseEntity<Object> handleBadRequest(RuntimeException ex, WebRequest request) {
        log.error("Exception thrown '{}'", ex.getMessage(), ex);
        return handleExceptionInternal(ex, flattenExceptionMessages(ex), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { ValidationException.class })
    public ResponseEntity<Object> handleValidationException(ValidationException validationException, WebRequest request) {

        String errorMessage = new StringBuilder("Validation errors occurred importing the spreadsheet.\n\n")
            .append(validationException.getValidationResult().getValidationErrors()
                .stream()
                .map(validationError -> String.format(
                    "- %s",
                    validationError.createMessage(this.spreadsheetValidationErrorMessageCreator)
                    )
                )
                .collect(Collectors.joining("\n"))
            ).toString();

        return handleExceptionInternal(validationException, errorMessage, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler(CaseTypeValidationException.class)
    @ResponseStatus(code = BAD_REQUEST)
    @ResponseBody
    String caseTypeValidation(CaseTypeValidationException e) {
        log.error("Exception thrown {}", e.getMessage(), e);
        return getMessagesAsString(e.getErrors());
    }

    @ExceptionHandler(FileStorageException.class)
    public void handleFileStorageException(HttpServletResponse response,
                                           FileStorageException fileStorageException) throws IOException {
        log.error("Exception thrown: {}", fileStorageException.getMessage(), fileStorageException);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private String flattenExceptionMessages(RuntimeException ex) {
        final StringBuilder sb = new StringBuilder(ex.getMessage());

        Integer remaining = MAX_DEPTH;
        Throwable inner = ex;
        while ((inner = inner.getCause()) != null && 0 < --remaining) {
            log.debug("Remaining '{}' out of '{}'", remaining, MAX_DEPTH);
            sb.append("\n")
              .append(inner.getMessage());
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
