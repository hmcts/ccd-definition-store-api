package uk.gov.hmcts.integration;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.URI_IMPORT;

@Ignore
// FIXME : RDM-7635 - has to mock opendId jwks responses with proper Key set (RS256 public / private key).
public class ServiceToServiceIT extends IntegrationTest {

    private static final String SERVICE_TOKEN = "ServiceToken";
    private static final String INVALID_SERVICE_TOKEN = "InvalidServiceToken";
    private static final String URL_S2S_DETAILS = "/s2s/details";

    @Test
    public void shouldPassServiceAuthorizationWhenValidServiceToken() {

        final ResponseEntity<String>
            response =
            restTemplate.exchange(URI_IMPORT, GET, validRequestEntity(), String.class);
        assertHappyPath(response);
        verify(getRequestedFor(urlEqualTo(URL_S2S_DETAILS)).withHeader(AUTHORIZATION,
            equalTo(BEARER + SERVICE_TOKEN)));
    }

    @Test
    public void shouldFailServiceAuthorizationWhenInvalidServiceToken() {

        stubFor(get(urlEqualTo(URL_S2S_DETAILS)).withHeader(AUTHORIZATION, equalTo(BEARER + INVALID_SERVICE_TOKEN))
            .willReturn(aResponse().withStatus(SC_UNAUTHORIZED)));

        final HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_SERVICE_AUTHORIZATION, INVALID_SERVICE_TOKEN);
        headers.add(AUTHORIZATION, VALID_IDAM_TOKEN);
        headers.add(CONTENT_TYPE, APPLICATION_JSON);

        final ResponseEntity<String>
            response =
            restTemplate.exchange(URI_IMPORT, GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCodeValue(), is(SC_FORBIDDEN));
        verify(getRequestedFor(urlEqualTo(URL_S2S_DETAILS)).withHeader(AUTHORIZATION,
            equalTo(BEARER + INVALID_SERVICE_TOKEN)));
    }
}
