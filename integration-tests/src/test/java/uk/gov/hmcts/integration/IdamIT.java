package uk.gov.hmcts.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpMethod.GET;
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.URI_IMPORT;

@Disabled
// FIXME : RDM-7631 - has to mock opendId jwks responses with proper Key set (RS256 public / private key).
class IdamIT extends IntegrationTest {

    private static final String URI_NON_IMPORT_ENDPOINT = "/api/user-role";
    private static final String IDAM_DETAILS = "/idam/details";

    /**
     * <pre>
     *     Given a user with ccd-import role.
     *     When accessing import end point
     *     Then user is on happy path
     * </pre>
     */
    @Test
    void shouldBeOnHappyPathGivenCCDImportUserWhenAccessingImportEndPoint() {
        final ResponseEntity<String>
            response =
            restTemplate.exchange(URI_IMPORT, GET, validRequestEntity(), String.class);

        assertHappyPath(response);
        verify(getRequestedFor(urlEqualTo(IDAM_DETAILS)).withHeader(AUTHORIZATION,
            equalTo(VALID_IDAM_TOKEN)));
    }

    /**
     * <pre>
     *     Given a user with ccd-import role.
     *     When accessing non import end point
     *     Then user is on happy path
     * </pre>
     */
    @Test
    void shouldBeOnHappyPathGivenCCDImportUserWhenAccessingNonImportEndPoint() {
        final ResponseEntity<String>
            response =
            restTemplate.exchange(URI_NON_IMPORT_ENDPOINT, GET, validRequestEntity(), String.class);

        assertHappyPath(response);
        verify(getRequestedFor(urlEqualTo(IDAM_DETAILS)).withHeader(AUTHORIZATION,
            equalTo(VALID_IDAM_TOKEN)));
    }

    /**
     * <pre>
     *     Given a user with no ccd-import role.
     *     When accessing non import end point
     *     Then user is on happy path
     * </pre>
     */
    @Test
    void shouldBeOnHappyPathGivenNoRolesUserWhenAccessingNonImportEndPoint() {
        final ResponseEntity<String>
            response =
            restTemplate.exchange(URI_NON_IMPORT_ENDPOINT, GET, validRequestEntity(), String.class);

        assertHappyPath(response);
        verify(getRequestedFor(urlEqualTo(IDAM_DETAILS)).withHeader(AUTHORIZATION, equalTo(VALID_IDAM_TOKEN)));
    }

    /**
     * <pre>
     *     Given a user with no ccd-import role.
     *     When accessing import end point
     *     Then 403 response
     * </pre>
     */
    @Test
    void should403GivenNoRolesUserWhenAccessingImportEndPoint() {
        stubFor(get(urlEqualTo(IDAM_DETAILS)).withHeader(AUTHORIZATION, equalTo(INVALID_IDAM_TOKEN))
            .willReturn(aResponse().withStatus(SC_FORBIDDEN)));

        final ResponseEntity<String>
            response =
            restTemplate.exchange(URI_IMPORT, GET, invalidRequestEntity(), String.class);

        assertThat(response.getStatusCodeValue(), is(SC_FORBIDDEN));
        verify(getRequestedFor(urlEqualTo(IDAM_DETAILS)).withHeader(AUTHORIZATION,
            equalTo(INVALID_IDAM_TOKEN)));
    }

    /**
     * <pre>
     *     Given a user with invalid token.
     *     When accessing non import end point
     *     Then 403 response
     * </pre>
     */
    @Test
    void should403GivenInvalidTokenWhenAccessingNonImportEndPoint() {
        stubFor(get(urlEqualTo(IDAM_DETAILS)).withHeader(AUTHORIZATION, equalTo(INVALID_IDAM_TOKEN))
            .willReturn(aResponse().withStatus(SC_FORBIDDEN)));

        final ResponseEntity<String>
            response =
            restTemplate.exchange(URI_NON_IMPORT_ENDPOINT, GET, invalidRequestEntity(), String.class);

        assertThat(response.getStatusCodeValue(), is(SC_FORBIDDEN));
        verify(getRequestedFor(urlEqualTo(IDAM_DETAILS)).withHeader(AUTHORIZATION, equalTo(INVALID_IDAM_TOKEN)));
    }
}
