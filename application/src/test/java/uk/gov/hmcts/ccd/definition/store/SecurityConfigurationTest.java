package uk.gov.hmcts.ccd.definition.store;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import uk.gov.hmcts.ccd.definition.store.security.OidcIssuerConfiguration;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigurationTest {

    private static final String VALID_ISSUER = "http://fr-am:8080/openam/oauth2/hmcts";
    private static final String ADDITIONAL_ALLOWED_ISSUER = "http://public-idam/o";
    private static final String INVALID_ISSUER = "http://unexpected-issuer";

    @Test
    void shouldAcceptJwtFromPrimaryIssuerWhenAllowedIssuersUnset() {
        assertFalse(validator(null).validate(buildJwt(VALID_ISSUER, Instant.now().plusSeconds(300))).hasErrors());
    }

    @Test
    void shouldAcceptJwtFromAdditionalAllowedIssuer() {
        assertFalse(validator(ADDITIONAL_ALLOWED_ISSUER)
            .validate(buildJwt(ADDITIONAL_ALLOWED_ISSUER, Instant.now().plusSeconds(300)))
            .hasErrors());
    }

    @Test
    void shouldKeepPrimaryIssuerWhenAdditionalAllowedIssuersConfigured() {
        assertFalse(validator(ADDITIONAL_ALLOWED_ISSUER)
            .validate(buildJwt(VALID_ISSUER, Instant.now().plusSeconds(300)))
            .hasErrors());
    }

    @Test
    void shouldRejectJwtFromUnexpectedIssuer() {
        assertTrue(validator(ADDITIONAL_ALLOWED_ISSUER)
            .validate(buildJwt(INVALID_ISSUER, Instant.now().plusSeconds(300)))
            .hasErrors());
    }

    @Test
    void shouldRejectJwtWithoutIssuer() {
        assertTrue(validator(ADDITIONAL_ALLOWED_ISSUER)
            .validate(buildJwtWithoutIssuer(Instant.now().plusSeconds(300)))
            .hasErrors());
    }

    @Test
    void shouldRejectExpiredJwtEvenWhenIssuerMatches() {
        assertTrue(validator("").validate(buildJwt(VALID_ISSUER, Instant.now().minusSeconds(60))).hasErrors());
    }

    private OAuth2TokenValidator<Jwt> validator(String allowedIssuers) {
        return new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            new JwtClaimValidator<>(
                "iss",
                OidcIssuerConfiguration.allowedIssuers(VALID_ISSUER, allowedIssuers)::contains
            )
        );
    }

    private Jwt buildJwt(String issuer, Instant expiresAt) {
        return Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .issuer(issuer)
            .subject("user")
            .issuedAt(expiresAt.minusSeconds(60))
            .expiresAt(expiresAt)
            .build();
    }

    private Jwt buildJwtWithoutIssuer(Instant expiresAt) {
        return Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("user")
            .issuedAt(expiresAt.minusSeconds(60))
            .expiresAt(expiresAt)
            .build();
    }
}
