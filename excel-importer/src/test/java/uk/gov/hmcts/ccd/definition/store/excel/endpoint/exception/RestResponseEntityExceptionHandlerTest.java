package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.springframework.http.MediaType;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParsingException;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

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

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestResponseEntityExceptionHandlerTest {

    private RestResponseEntityExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new RestResponseEntityExceptionHandler(null);
    }

    @Test
    void approvedValidationRemainsActionableButMapperDiagnosticsStayPrivate() {
        for (ImportValidationException.Reason reason : ImportValidationException.Reason.values()) {
            ImportValidationException exception = new ImportValidationException(reason, "PRIVATE context");
            ResponseEntity<Object> response = exceptionHandler.handleBadRequest(exception, mock(WebRequest.class));
            assertEquals(400, response.getStatusCode().value());
            assertEquals(exception.getPublicMessage(), ((Map<?, ?>) response.getBody()).get("message"));
            assertFalse(response.getBody().toString().contains("PRIVATE"));
        }
        MapperException diagnostic = new MapperException("SECRET internal-host",
            new IllegalStateException("SECRET database cause"));
        ResponseEntity<Object> response = exceptionHandler.handleBadRequest(diagnostic, mock(WebRequest.class));
        assertEquals("Bad Request", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void duplicateDefaultFocusRetainsPlainTextValidationResponse() {
        ResponseEntity<Object> response = exceptionHandler.handleBadRequest(
            ImportValidationException.multipleDefaultFocus("CaseTypeTab", 2, "FT_MasterCaseType"),
            mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertEquals("Only one row per case type can have DefaultFocus set to true in worksheet CaseTypeTab; "
            + "found 2 for case type FT_MasterCaseType", response.getBody());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "NULLIFY_WITH_DEFAULT|NullifyByDefault cannot be set to Yes if DefaultValue has a value in CaseEventToFields",
        "JURISDICTION_COUNT|A definition must contain exactly one Jurisdiction",
        "MISSING_CASE_TYPE|A definition must contain at least one Case Type",
        "MISSING_CASE_FIELD_SHEET|A definition must contain a Case Field worksheet",
        "MISSING_COMPLEX_TYPES_SHEET|A definition must contain a Complex Types worksheet",
        "MISSING_FIXED_LIST_SHEET|A definition must contain a Fixed List worksheet",
        "MISSING_DISPLAY_GROUP_SHEET|A definition must contain a CaseTypeTab sheet with at least one entry",
        "MISSING_TAB_FIELDS|At least one CaseField must be defined in the CaseTypeTab for case type CaseTypeTab",
        "DUPLICATE_LAYOUT_ROW|Please make sure each row in worksheet CaseTypeTab is unique for case type TestCase"
    })
    void approvedSpreadsheetRulesRetainPlainText(ImportValidationException.Rule rule, String expectedMessage) {
        ResponseEntity<Object> response = exceptionHandler.handleBadRequest(
            ImportValidationException.rule(rule, "CaseTypeTab", "TestCase"), mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertEquals(expectedMessage, response.getBody());
    }

    @Test
    void approvedHiddenFieldErrorsRetainPlainTextWithoutPrivateDiagnostics() {
        ImportValidationException first = ImportValidationException.invalidHiddenField("CollectionField");
        ImportValidationException second = new ImportValidationException(
            ImportValidationException.Reason.MISSING_CASE_FIELDS, "SECRET diagnostic");
        ImportValidationException combined = ImportValidationException.combine(List.of(first, second));
        ResponseEntity<Object> response = exceptionHandler.handleBadRequest(combined, mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertEquals(first.getPublicMessage() + "\n" + second.getPublicMessage(), response.getBody());
        assertFalse(response.getBody().toString().contains("SECRET"));
        assertEquals("SECRET diagnostic", combined.getSuppressed()[1].getMessage());
    }

    @Test
    void publicFieldValidationDoesNotExposeItsTechnicalCause() {
        InvalidImportException exception = InvalidImportException.invalidReference(
            SheetName.SEARCH_PARTY,
            ColumnName.SEARCH_PARTY_NAME,
            "InvalidName", new IllegalStateException("SECRET jdbc internal-host"));
        ResponseEntity<Object> response = exceptionHandler.handleBadRequest(exception, mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertEquals(exception.getPublicMessage(), response.getBody());
        assertFalse(response.getBody().toString().contains("SECRET"));
        response = exceptionHandler.handleBadRequest(new InvalidImportException("SECRET diagnostic"),
            mock(WebRequest.class));
        assertEquals("Bad Request", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void publicPostStateValidationAndTechnicalParsingFailuresStaySeparate() {
        var exception = SpreadsheetParsingException
            .invalidCondition("CaseCreated(InvalidCondition)", new IllegalStateException("SECRET cause"));
        ResponseEntity<Object> response = exceptionHandler.handleSpreadsheetParsing(exception, mock(WebRequest.class));
        assertEquals("Invalid Post State Condition CaseCreated(InvalidCondition)",
            ((Map<?, ?>) response.getBody()).get("message"));
        assertEquals("/import", ((Map<?, ?>) response.getBody()).get("path"));
        assertFalse(response.getBody().toString().contains("SECRET"));
        response = exceptionHandler.handleSpreadsheetParsing(
            new SpreadsheetParsingException("SECRET diagnostic"),
            mock(WebRequest.class));
        assertEquals("Bad Request", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void handleBadRequest_shouldAggregateInnerMessages() throws Exception {
        final RuntimeException exception = new RuntimeException("Outer message", new Exception("Inner message"));

        final ResponseEntity<Object> response = exceptionHandler.handleBadRequest(exception, mock(WebRequest.class));

        assertThat(((Map<?, ?>) response.getBody()).get("message"), equalTo("Bad Request"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void handleMissingAccessProfilesException() {
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
    void handleBadRequest_validationException() throws Exception {
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
    void handleBadRequest_shouldStopMessageAggregationAtDepth5() throws Exception {
        final RuntimeException exception = new RuntimeException("Depth 1",
            new Exception("Depth 2",
                new Exception("Depth 3",
                    new Exception("Depth 4",
                        new Exception("Depth 5",
                            new Exception("Depth 6"))))));

        final ResponseEntity<Object> response = exceptionHandler.handleBadRequest(exception, mock(WebRequest.class));

        assertThat(((Map<?, ?>) response.getBody()).get("message"), equalTo("Bad Request"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void handleHttpServerErrorException_shouldSwitch500_502() throws IOException {
        HttpServerErrorException ex = new HttpServerErrorException(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

        MockHttpServletResponse response = new MockHttpServletResponse();
        exceptionHandler.handleHttpServerErrorException(ex, response);

        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getStatus());
    }

    @Test
    void handleHttpServerErrorException_shouldReturn5xx() throws IOException {
        HttpServerErrorException ex = new HttpServerErrorException(
            HttpStatus.GATEWAY_TIMEOUT, "Gateway Timeout");

        MockHttpServletResponse response = new MockHttpServletResponse();
        exceptionHandler.handleHttpServerErrorException(ex, response);

        assertEquals(HttpStatus.GATEWAY_TIMEOUT.value(), response.getStatus());
    }

    @Test
    void handleHttpClientErrorException_shouldReturn401() {
        HttpClientErrorException ex = new HttpClientErrorException(
            HttpStatus.UNAUTHORIZED, "Unauthorized");

        final ResponseEntity<Object> response = exceptionHandler
            .handleHttpClientErrorException(ex, mock(WebRequest.class));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleHttpClientErrorException_shouldReturn500IfReceived400() {
        HttpClientErrorException ex = new HttpClientErrorException(
            HttpStatus.BAD_REQUEST, "Bad Request");

        final ResponseEntity<Object> response = exceptionHandler
            .handleHttpClientErrorException(ex, mock(WebRequest.class));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleFeignServerException_shouldSwitch500_502() throws IOException {
        FeignException.FeignServerException ex = new FeignException.FeignServerException(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
            Request.create(Request.HttpMethod.GET, "Internal Server Error", Map.of(), new byte[0],
                Charset.defaultCharset(), null), new byte[0], Map.of());

        MockHttpServletResponse response = new MockHttpServletResponse();
        exceptionHandler.handleFeignServerException(ex, response);

        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getStatus());
    }

    @Test
    void handleFeignServerException_shouldReturn5xx() throws IOException {
        FeignException.FeignServerException ex = new FeignException.FeignServerException(
            HttpStatus.GATEWAY_TIMEOUT.value(), "Gateway Timeout", Request.create(Request.HttpMethod.GET,
            "Gateway Timeout", Map.of(), new byte[0], Charset.defaultCharset(), null), new byte[0],
            Map.of());

        MockHttpServletResponse response = new MockHttpServletResponse();
        exceptionHandler.handleFeignServerException(ex, response);

        assertEquals(HttpStatus.GATEWAY_TIMEOUT.value(), response.getStatus());
    }

    @Test
    void handleFeignClientException_shouldReturn401() {
        FeignException.FeignClientException ex = new FeignException.FeignClientException(
            HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED",
            Request.create(Request.HttpMethod.GET, "UNAUTHORIZED", Map.of(), new byte[0],
                Charset.defaultCharset(), null), new byte[0], Map.of());

        final ResponseEntity<Object> response = exceptionHandler
            .handleFeignClientException(ex, mock(WebRequest.class));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleFeignClientException_shouldReturn500IfReceived400() {
        FeignException.FeignClientException ex = new FeignException.FeignClientException(
            HttpStatus.BAD_REQUEST.value(), "Bad Request",
            Request.create(Request.HttpMethod.GET, "Bad Request", Map.of(), new byte[0],
                Charset.defaultCharset(), null), new byte[0], Map.of());

        final ResponseEntity<Object> response = exceptionHandler
            .handleFeignClientException(ex, mock(WebRequest.class));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void retainsOriginalExceptionAtInfoWithoutLeakingItInTheResponse() {
        Logger logger = (Logger) LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);
        Level previousLevel = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            logger.setLevel(Level.INFO);
            ImportValidationException original = new ImportValidationException(
                ImportValidationException.Reason.MISSING_LAYOUT_FIELDS, "PRIVATE support diagnostic");
            ResponseEntity<Object> result = exceptionHandler.handleBadRequest(original, mock(WebRequest.class));
            assertEquals(400, result.getStatusCode().value());
            assertEquals("PRIVATE support diagnostic", appender.list.get(0).getThrowableProxy().getMessage());
            assertEquals(1, appender.list.size());
            assertEquals(Level.WARN, appender.list.get(0).getLevel());
            assertEquals(original.getPublicMessage(), ((Map<?, ?>) result.getBody()).get("message"));
            assertEquals(false, result.getBody().toString().contains("PRIVATE"));
        } finally {
            logger.setLevel(previousLevel);
            logger.detachAppender(appender);
            appender.stop();
        }
    }

}
