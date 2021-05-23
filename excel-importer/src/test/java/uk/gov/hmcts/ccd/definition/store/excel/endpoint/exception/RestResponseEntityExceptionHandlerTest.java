package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import uk.gov.hmcts.ccd.definition.store.domain.validation.MissingUserRolesException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestResponseEntityExceptionHandlerTest {

    private RestResponseEntityExceptionHandler exceptionHandler;

    @Before
    public void setUp() {
        exceptionHandler = new RestResponseEntityExceptionHandler(null);
    }

    @Test
    public void handleBadRequest_shouldAggregateInnerMessages() throws Exception {
        final RuntimeException exception = new RuntimeException("Outer message", new Exception("Inner message"));

        final ResponseEntity<Object> response = exceptionHandler.handleBadRequest(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), equalTo("Outer message\nInner message"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void handleMissingUserRolesException() throws Exception {
        Set<String> userRoles = new HashSet<>();
        userRoles.add("user_role_1");
        List<ValidationError> validationErrors = new ArrayList<>();
        final MissingUserRolesException exception = new MissingUserRolesException(userRoles, validationErrors);

        final ResponseEntity<Object> response = exceptionHandler
            .handleUserRolesMissing(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), containsString("Missing UserRoles."));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void handleBadRequest_validationException() throws Exception {
        ValidationException validationException = mock(ValidationException.class);
        ValidationResult validationResult = mock(ValidationResult.class);
        List<ValidationError> validationErrors = new ArrayList<>();
        when(validationException.getValidationResult()).thenReturn(validationResult);
        when(validationResult.getValidationErrors()).thenReturn(validationErrors);
        final ResponseEntity<Object> response = exceptionHandler
            .handleValidationException(validationException, mock(WebRequest.class));

        assertThat(response.getBody().toString(),
            containsString("Validation errors occurred importing the spreadsheet."));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    public void handleBadRequest_shouldStopMessageAggregationAtDepth5() throws Exception {
        final RuntimeException exception = new RuntimeException("Depth 1",
            new Exception("Depth 2",
                new Exception("Depth 3",
                    new Exception("Depth 4",
                        new Exception("Depth 5",
                            new Exception("Depth 6"))))));

        final ResponseEntity<Object> response = exceptionHandler.handleBadRequest(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), equalTo("Depth 1\nDepth 2\nDepth 3\nDepth 4\nDepth 5"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

}
