package uk.gov.hmcts.ccd.definition.store.rest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IdamProfileServiceTest {

    private IdamProfileClient client;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApplicationParams applicationParams;

    @Captor
    private ArgumentCaptor<String> idamUserProfileURLCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> httpMethodCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity> requestEntityCaptor;

    @Captor
    private ArgumentCaptor<Class<IdamProperties>> idamPropertiesClassCaptor;

    @BeforeEach
    void setUp() {
        client = new IdamProfileClient(securityUtils, restTemplate, applicationParams);
    }

    @DisplayName("Should get logged in user details")
    @Test
    public void shouldGetLoggedInUserDetails() {
        final HttpEntity requestEntity = setupMocksForIdam();
        final IdamProperties expectedIdamProperties = client.getLoggedInUserDetails();
        assertEquals("445", expectedIdamProperties.getId());
        assertEquals("user@hmcts.net", expectedIdamProperties.getEmail());

        verify(restTemplate).exchange(idamUserProfileURLCaptor.capture(),
                                      httpMethodCaptor.capture(),
                                      requestEntityCaptor.capture(),
                                      idamPropertiesClassCaptor.capture());
        assertEquals("http://idam.local/details", idamUserProfileURLCaptor.getValue());
        assertEquals(HttpMethod.GET, httpMethodCaptor.getValue());
        assertEquals(requestEntity, requestEntityCaptor.getValue());
        assertEquals(IdamProperties.class, idamPropertiesClassCaptor.getValue());
    }

    private HttpEntity setupMocksForIdam() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "ey123.ey456");
        given(securityUtils.userAuthorizationHeaders()).willReturn(httpHeaders);
        final HttpEntity requestEntity = new HttpEntity(securityUtils.userAuthorizationHeaders());
        given(applicationParams.idamUserProfileURL()).willReturn("http://idam.local/details");
        final IdamProperties idamProperties = new IdamProperties();
        idamProperties.setId("445");
        idamProperties.setEmail("user@hmcts.net");
        final ResponseEntity<IdamProperties> responseEntity = new ResponseEntity<>(idamProperties, HttpStatus.OK);
        given(restTemplate.exchange(applicationParams.idamUserProfileURL(), HttpMethod.GET, requestEntity,
                                    IdamProperties.class)).willReturn(responseEntity);
        return requestEntity;
    }
}
