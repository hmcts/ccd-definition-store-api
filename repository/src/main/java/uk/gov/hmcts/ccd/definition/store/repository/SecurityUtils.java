package uk.gov.hmcts.ccd.definition.store.repository;


import java.util.Objects;
import javax.inject.Named;
import javax.inject.Singleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.ccd.definition.store.security.idam.IdamRepository;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Named
@Singleton
public class SecurityUtils {

    private final IdamRepository idamRepository;

    @Autowired
    public SecurityUtils(final IdamRepository idamRepository) {
        this.idamRepository = idamRepository;
    }

    public HttpHeaders authorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, getUserBearerToken());
        return headers;
    }

    public UserInfo getUserInfo() {
        return idamRepository.getUserInfo(getUserToken());
    }

    public HttpHeaders userAuthorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, getUserBearerToken());
        return headers;
    }

    public String getUserToken() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getTokenValue();
    }


    public boolean isAuthenticated() {
        return Objects.nonNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private String getUserBearerToken() {
        return "Bearer " + getUserToken();
    }
}
