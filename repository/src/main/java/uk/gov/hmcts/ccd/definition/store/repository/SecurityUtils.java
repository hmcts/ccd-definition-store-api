package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.auth.provider.service.token.ServiceTokenGenerator;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.UserDetails;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class SecurityUtils {

    private final ServiceTokenGenerator serviceTokenGenerator;

    @Inject
    public SecurityUtils(@Qualifier("cachedServiceTokenGenerator") final ServiceTokenGenerator serviceTokenGenerator) {
        this.serviceTokenGenerator = serviceTokenGenerator;
    }

    public HttpHeaders authorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", serviceTokenGenerator.generate());
        return headers;
    }

    private UserDetails currentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        return null;
    }
}
