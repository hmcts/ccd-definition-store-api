package uk.gov.hmcts.net.ccd.definition.store.security;

import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.ccd.definition.store.CaseDataAPIApplication;
import uk.gov.hmcts.net.ccd.definition.store.TestConfiguration;
import uk.gov.hmcts.net.ccd.definition.store.TestIdamConfiguration;
import uk.gov.hmcts.net.ccd.definition.store.wiremock.config.WireMockTestConfiguration;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.net.ccd.definition.store.util.JwtTestTokenBuilder.signedToken;
import static uk.gov.hmcts.net.ccd.definition.store.util.JwtTestTokenBuilder.signedTokenWithoutIssuer;

@SpringBootTest(classes = {
    CaseDataAPIApplication.class,
    TestConfiguration.class,
    TestIdamConfiguration.class,
    WireMockTestConfiguration.class
})
@TestPropertySource(
    locations = "classpath:test.properties",
    properties = {
        "oidc.issuer=http://localhost:${wiremock.server.port}/o",
        "oidc.allowed-issuers=http://public-idam/o"
    }
)
@AutoConfigureWireMock(port = 0)
class JwtDecoderIssuerValidationIT {

    private static final String ADDITIONAL_ALLOWED_ISSUER = "http://public-idam/o";
    private static final String INVALID_ISSUER = "http://unexpected-issuer/o";
    private static final Instant VALID_EXPIRES_AT = Instant.parse("2099-01-01T00:00:00Z");
    private static final Instant EXPIRED_AT = Instant.parse("2000-01-01T00:00:00Z");

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Value("${oidc.issuer}")
    private String validIssuer;

    @Test
    void shouldUseJwtDecoderBeanFromSecurityConfiguration() {
        assertThat(applicationContext.getBeanNamesForType(JwtDecoder.class)).containsOnly("jwtDecoder");

        BeanDefinition jwtDecoderBeanDefinition = applicationContext.getBeanFactory().getBeanDefinition("jwtDecoder");

        assertThat(jwtDecoderBeanDefinition.getFactoryBeanName()).isEqualTo("securityConfiguration");
        assertThat(jwtDecoderBeanDefinition.getFactoryMethodName()).isEqualTo("jwtDecoder");
    }

    @Test
    void shouldDecodeJwtFromConfiguredIssuer() {
        String token = signedJwt(validIssuer, VALID_EXPIRES_AT);

        Jwt jwt = assertDoesNotThrow(() -> jwtDecoder.decode(token));

        assertEquals(validIssuer, jwt.getIssuer().toString());
    }

    @Test
    void shouldDecodeJwtFromAdditionalAllowedIssuer() {
        String token = signedJwt(ADDITIONAL_ALLOWED_ISSUER, VALID_EXPIRES_AT);

        Jwt jwt = assertDoesNotThrow(() -> jwtDecoder.decode(token));

        assertEquals(ADDITIONAL_ALLOWED_ISSUER, jwt.getIssuer().toString());
    }

    @Test
    void shouldRejectJwtFromUnexpectedIssuer() {
        String token = signedJwt(INVALID_ISSUER, VALID_EXPIRES_AT);

        JwtValidationException exception = assertThrows(JwtValidationException.class,
            () -> jwtDecoder.decode(token));

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectJwtWithoutIssuer() {
        String token = signedJwtWithoutIssuer(VALID_EXPIRES_AT);

        JwtValidationException exception = assertThrows(JwtValidationException.class,
            () -> jwtDecoder.decode(token));

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectExpiredJwtEvenWhenIssuerMatches() {
        String token = signedJwt(validIssuer, EXPIRED_AT);

        assertThrows(BadJwtException.class,
            () -> jwtDecoder.decode(token));
    }

    private static String signedJwt(String issuer, Instant expiresAt) {
        try {
            return signedToken(issuer, expiresAt);
        } catch (JOSEException exception) {
            throw new AssertionError("Failed to create signed JWT", exception);
        }
    }

    private static String signedJwtWithoutIssuer(Instant expiresAt) {
        try {
            return signedTokenWithoutIssuer(expiresAt);
        } catch (JOSEException exception) {
            throw new AssertionError("Failed to create signed JWT", exception);
        }
    }
}
