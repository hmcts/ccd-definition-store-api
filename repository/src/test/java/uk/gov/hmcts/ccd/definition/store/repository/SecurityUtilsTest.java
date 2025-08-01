package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.ccd.definition.store.security.idam.IdamRepository;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@DisplayName("SecurityUtils")
class SecurityUtilsTest {

    private static final String SERVICE_JWT = "7gf364fg367f67";
    private static final String USER_ID = "123";
    private static final String USER_JWT = "Bearer 8gf364fg367f67";

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private IdamRepository idamRepository;

    @Mock
    private AuthTokenGenerator serviceTokenGenerator;

    @InjectMocks
    private SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        final GrantedAuthority[] authorities = new GrantedAuthority[]{newAuthority("role1"), newAuthority("role2")};

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_JWT);

        Jwt jwt = Jwt.withTokenValue(USER_JWT)
            .claim("aClaim", "aClaim")
            .header("aHeader", "aHeader")
            .build();
        Collection<? extends GrantedAuthority> authorityCollection = Stream.of("role1", "role2")
            .map(a -> new SimpleGrantedAuthority(a))
            .collect(Collectors.toCollection(ArrayList::new));

        doReturn(jwt).when(authentication).getPrincipal();
        doReturn(authentication).when(securityContext).getAuthentication();
        when(authentication.getAuthorities()).thenAnswer(invocationOnMock -> authorityCollection);
        SecurityContextHolder.setContext(securityContext);


        UserInfo userInfo = UserInfo.builder()
            .uid(USER_ID)
            .sub("emailId@a.com")
            .build();
        doReturn(userInfo).when(idamRepository).getUserInfo(USER_JWT);
    }

    @Test
    @DisplayName("authorizationHeaders")
    void authorizationHeaders() {
        final HttpHeaders headers = securityUtils.authorizationHeaders();

        assertAll(
            () -> assertHeader(headers, "ServiceAuthorization", SERVICE_JWT)
        );
    }

    @Test
    @DisplayName("authorizationHeaders")
    void getS2SToken() {
        final String headers = securityUtils.getS2SToken();

        assertAll(
            () -> assertEquals(SERVICE_JWT, headers)
        );
    }

    @Test
    @DisplayName("Get user token")
    void shouldReturnUserToken() {
        assertThat(securityUtils.getUserToken(), is(USER_JWT));
    }

    private void assertHeader(HttpHeaders headers, String name, String value) {
        assertThat(headers.get(name), hasSize(1));
        assertThat(headers.get(name).get(0), equalTo(value));
    }

    private GrantedAuthority newAuthority(String authority) {
        return (GrantedAuthority) () -> authority;
    }
}

