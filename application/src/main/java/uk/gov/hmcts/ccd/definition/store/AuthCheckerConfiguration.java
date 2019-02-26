package uk.gov.hmcts.ccd.definition.store;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController;
import uk.gov.hmcts.ccd.definition.store.rest.configuration.AdminWebAuthorizationProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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
        if (request.getRequestURI().matches("^/api/user-role/?$")) {
            return new ArrayList<>(adminWebAuthorizationProperties.getManageUserProfile());
        }
        return Collections.emptyList();
    }
}
