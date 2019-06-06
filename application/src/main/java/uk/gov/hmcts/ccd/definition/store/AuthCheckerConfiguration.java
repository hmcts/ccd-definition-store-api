package uk.gov.hmcts.ccd.definition.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.definition.store.rest.configuration.AdminWebAuthorizationProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Configuration
public class AuthCheckerConfiguration {

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
        return this::extractRoleFromRequest;
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        return request -> Optional.empty();
    }

    private Collection<String> extractRoleFromRequest(final HttpServletRequest request) {
        if (adminWebAuthorizationProperties.isEnabled()
            && ("/api/draft".equals(request.getRequestURI()) || "/api/draft/save".equals(request.getRequestURI()))) {
            // temporarily here for now as we are about to use definition-designer-api
            return adminWebAuthorizationProperties.getManageDefinition();
        }
        return Collections.emptyList();
    }
}
