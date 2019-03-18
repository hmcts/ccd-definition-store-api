package uk.gov.hmcts.ccd.definition.store;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController;
import uk.gov.hmcts.ccd.definition.store.rest.configuration.AdminWebAuthorizationProperties;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.UserRoleController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Configuration
public class AuthCheckerConfiguration {

    @VisibleForTesting
    protected static final String ROLE_CCD_IMPORT = "ccd-import";
    private static final String REGEX_URI_IMPORT = "^" + ImportController.URI_IMPORT  + "/?$";
    private static final String REGEX_URI_USER_ROLE = "^/api" + UserRoleController.URI_USER_ROLE + "/?$";

    @Value("#{'${casedefinitionstore.authorised.services}'.split(',')}")
    private List<String> authorisedServices;

    @Autowired
    private AdminWebAuthorizationProperties adminWebAuthorizationProperties;

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return request -> authorisedServices;
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        return request -> extractRoleFromRequest(request);
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        return request -> Optional.empty();
    }

    private Collection<String> extractRoleFromRequest(final HttpServletRequest request) {
        if (request.getRequestURI().matches(REGEX_URI_IMPORT)) {
            return Collections.singletonList(ROLE_CCD_IMPORT);
        }
        if (adminWebAuthorizationProperties.isEnabled()) {
            if (request.getRequestURI().matches(REGEX_URI_USER_ROLE)) {
                return adminWebAuthorizationProperties.getManageUserRole();
            } else if ("/api/draft".equals(request.getRequestURI()) || "/api/draft/save".equals(request.getRequestURI())) {
                // temporarily here for now as we are about to use definition-designer-api
                return adminWebAuthorizationProperties.getManageDefinition();
            }
        }
        return Collections.emptyList();
    }
}
