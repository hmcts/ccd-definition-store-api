package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import feign.FeignException;
import feign.Request;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import uk.gov.hmcts.ccd.definition.store.domain.validation.MissingAccessProfilesException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
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
    public void handleMissingAccessProfilesException() {
        Set<String> accessProfiles = new HashSet<>();
        accessProfiles.add("access_profile_1");
        List<ValidationError> validationErrors = new ArrayList<>();
        final MissingAccessProfilesException exception
            = new MissingAccessProfilesException(accessProfiles, validationErrors);

        final ResponseEntity<Object> response = exceptionHandler
            .handleAccessProfilesMissing(exception, mock(WebRequest.class));

        assertThat(response.getBody().toString(), containsString("Missing AccessProfiles."));
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

    @Test
    public void handleHttpServerErrorException_shouldSwitch500_502() throws IOException {
        HttpServerErrorException ex = new HttpServerErrorException(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

        MockHttpServletResponse response = new MockHttpServletResponse();
        exceptionHandler.handleHttpServerErrorException(ex, response);

        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getStatus());
    }

    @Test
    public void handleHttpServerErrorException_shouldReturn5xx() throws IOException {
        HttpServerErrorException ex = new HttpServerErrorException(
            HttpStatus.GATEWAY_TIMEOUT, "Gateway Timeout");

        MockHttpServletResponse response = new MockHttpServletResponse();
        exceptionHandler.handleHttpServerErrorException(ex, response);

        assertEquals(HttpStatus.GATEWAY_TIMEOUT.value(), response.getStatus());
    }

    @Test
    public void handleFeignServerException_shouldSwitch500_502() throws IOException {
        FeignException.FeignServerException ex = new FeignException.FeignServerException(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
            Request.create(Request.HttpMethod.GET, "Internal Server Error", Map.of(), new byte[0],
                Charset.defaultCharset(), null), new byte[0], Map.of());

        MockHttpServletResponse response = new MockHttpServletResponse();
        exceptionHandler.handleFeignServerException(ex, response);

        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getStatus());
    }

    @Test
    public void handleFeignServerException_shouldReturn5xx() throws IOException {
        FeignException.FeignServerException ex = new FeignException.FeignServerException(
            HttpStatus.GATEWAY_TIMEOUT.value(), "Gateway Timeout", Request.create(Request.HttpMethod.GET,
            "Gateway Timeout", Map.of(), new byte[0], Charset.defaultCharset(), null), new byte[0],
            Map.of());

        MockHttpServletResponse response = new MockHttpServletResponse();
        exceptionHandler.handleFeignServerException(ex, response);

        assertEquals(HttpStatus.GATEWAY_TIMEOUT.value(), response.getStatus());
    }

}
