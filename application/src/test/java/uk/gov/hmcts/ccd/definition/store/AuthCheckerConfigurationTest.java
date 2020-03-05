package uk.gov.hmcts.ccd.definition.store;

import java.util.Collection;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.rest.configuration.AdminWebAuthorizationProperties;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.AuthCheckerConfiguration.ROLE_CCD_IMPORT;

@RunWith(MockitoJUnitRunner.class)
public class AuthCheckerConfigurationTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private AdminWebAuthorizationProperties adminWebAuthorizationProperties;

    @InjectMocks
    private AuthCheckerConfiguration configuration;

    @Test
    public void shouldReturnEmptyCollectionWhenURIIsNotImport() {
        when(request.getRequestURI()).thenReturn("/import/x");
        when(adminWebAuthorizationProperties.isEnabled()).thenReturn(false);
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
    public void shouldReturnEmptyCollectionWhenAdminWebIsNotEnabled() {
        when(request.getRequestURI()).thenReturn("/api/user-role");
        when(adminWebAuthorizationProperties.isEnabled()).thenReturn(false);
        assertThat(configuration.authorizedRolesExtractor().apply(request), empty());
    }

    @Test
    public void shouldReturnManageDraftDefinitionConfigurationPropertiesWhenAdminWebIsEnabled() {
        when(request.getRequestURI()).thenReturn("/api/draft");
        when(adminWebAuthorizationProperties.isEnabled()).thenReturn(true);
        when(adminWebAuthorizationProperties.getManageDefinition()).thenReturn(asList("cat"));
        final Collection<String> result = configuration.authorizedRolesExtractor().apply(request);
        assertThat(result, hasSize(1));
        assertThat(result, hasItem("cat"));
    }

    @Test
    public void shouldReturnUpdateDraftDefinitionConfigurationPropertiesWhenAdminWebIsEnabled() {
        when(request.getRequestURI()).thenReturn("/api/draft/save");
        when(adminWebAuthorizationProperties.isEnabled()).thenReturn(true);
        when(adminWebAuthorizationProperties.getManageDefinition()).thenReturn(asList("cat"));
        final Collection<String> result = configuration.authorizedRolesExtractor().apply(request);
        assertThat(result, hasSize(1));
        assertThat(result, hasItem("cat"));
    }

    @Test
    public void shouldReturnEmptyCollectionWhenAdminWebIsEnabledAndRequestUriIsNotDraftApi() {
        when(request.getRequestURI()).thenReturn("/api/user-role");
        when(adminWebAuthorizationProperties.isEnabled()).thenReturn(true);
        assertThat(configuration.authorizedRolesExtractor().apply(request), empty());
    }
}
