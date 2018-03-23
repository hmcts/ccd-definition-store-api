package uk.gov.hmcts.ccd.definition.store;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.AuthCheckerConfiguration.ROLE_CCD_IMPORT;

@RunWith(MockitoJUnitRunner.class)
public class AuthCheckerConfigurationTest {

    @Mock
    private HttpServletRequest request;

    private AuthCheckerConfiguration configuration;

    @Before
    public void setup() {
        configuration = new AuthCheckerConfiguration();
    }

    @Test
    public void shouldReturnEmptyCollectionWhenURIIsNotImport() {
        when(request.getRequestURI()).thenReturn("/import/x");
        assertThat(configuration.authorizedRolesExtractor().apply(request), empty());

    }

    @Test
    public void shouldReturnCCDRoleWhenURIIsImport() {
        when(request.getRequestURI()).thenReturn("/import");
        final Collection<String> result = configuration.authorizedRolesExtractor().apply(request);
        assertThat(result, hasSize(1));
        assertThat(result, hasItem(ROLE_CCD_IMPORT));
    }

    @Test
    public void shouldReturnCCDRoleWhenURIIsImportEndingWithSlash() {
        when(request.getRequestURI()).thenReturn("/import/");
        final Collection<String> result = configuration.authorizedRolesExtractor().apply(request);
        assertThat(result, hasSize(1));
        assertThat(result, hasItem(ROLE_CCD_IMPORT));
    }

    @Test
    public void shouldReturnEmptyCollectionWhenUserIdExtractorIsInvoked() {
        assertThat(configuration.userIdExtractor().apply(request), is(Optional.empty()));
    }

    @Test
    public void shouldReturnServiceCollectionWhenAuthorizedServicesExtractorIsInvoked() {
        final List<String> authorisedServices = Arrays.asList("s1", "s2", "s3");
        ReflectionTestUtils.setField(configuration, "authorisedServices", authorisedServices);
        final Collection<String> result = configuration.authorizedServicesExtractor().apply(request);
        assertThat(result, hasSize(3));
        assertThat(result, hasItems("s1", "s2", "s3"));
    }
}
