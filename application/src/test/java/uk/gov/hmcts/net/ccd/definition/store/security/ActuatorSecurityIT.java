package uk.gov.hmcts.net.ccd.definition.store.security;

import uk.gov.hmcts.ccd.definition.store.SecurityConfiguration;
import uk.gov.hmcts.ccd.definition.store.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActuatorSecurityIT {

    @Mock
    private ServiceAuthFilter serviceAuthFilter;

    @Mock
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Mock
    private WebSecurity webSecurity;

    @Mock
    private WebSecurity.IgnoredRequestConfigurer ignoredRequestConfigurer;

    private SecurityConfiguration securityConfiguration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityConfiguration = new SecurityConfiguration(serviceAuthFilter, jwtGrantedAuthoritiesConverter);
        when(webSecurity.ignoring()).thenReturn(ignoredRequestConfigurer);
        when(ignoredRequestConfigurer.requestMatchers(any(String[].class))).thenReturn(ignoredRequestConfigurer);
    }

    @Test
    void shouldKeepHealthEndpointsAnonymous() {
        securityConfiguration.webSecurityCustomizer().customize(webSecurity);

        List<String> ignoredPaths = ignoredPaths();

        assertTrue(ignoredPaths.contains("/health"));
        assertTrue(ignoredPaths.contains("/health/liveness"));
        assertTrue(ignoredPaths.contains("/health/readiness"));
    }

    @Test
    void shouldNotExposeLoggersEndpointAnonymously() {
        securityConfiguration.webSecurityCustomizer().customize(webSecurity);

        List<String> ignoredPaths = ignoredPaths();

        assertFalse(ignoredPaths.contains("/loggers/**"));
    }

    private List<String> ignoredPaths() {
        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        verify(ignoredRequestConfigurer).requestMatchers(argumentCaptor.capture());
        return Arrays.asList(argumentCaptor.getValue());
    }
}
