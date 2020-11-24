package uk.gov.hmcts.integration;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.definition.store.CaseDataAPIApplication;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CaseDataAPIApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(locations = "classpath:integration_tests.properties")
abstract class IntegrationTest {

    protected static final String VALID_IDAM_TOKEN = "Bearer UserAuthToken";
    protected static final String INVALID_IDAM_TOKEN = "Bearer irJ7eg6TsoOPW2uoWCXmUwHeMy5Nz9si2fRNiZbL";
    protected static final String BEARER = "Bearer ";
    protected static final String HEADER_SERVICE_AUTHORIZATION = "ServiceAuthorization";
    protected static final String APPLICATION_JSON = "application/json";

    @Autowired
    protected TestRestTemplate restTemplate;

    protected HttpEntity<Object> invalidRequestEntity() {
        return new HttpEntity<>(buildHeaders(INVALID_IDAM_TOKEN));
    }

    protected HttpEntity<?> validRequestEntity() {
        return new HttpEntity<>(validHeaders());
    }

    protected void assertHappyPath(final ResponseEntity<String> response) {
        assertThat(response.getStatusCodeValue(), not(401));
        assertThat(response.getStatusCodeValue(), not(403));
    }

    private HttpHeaders validHeaders() {
        return buildHeaders(VALID_IDAM_TOKEN);
    }

    private HttpHeaders buildHeaders(final String idamToken) {
        HttpHeaders headers = buildHeaders();
        headers.add(AUTHORIZATION, idamToken);
        return headers;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_SERVICE_AUTHORIZATION, "ServiceToken");
        headers.add(CONTENT_TYPE, APPLICATION_JSON);
        return headers;
    }
}
