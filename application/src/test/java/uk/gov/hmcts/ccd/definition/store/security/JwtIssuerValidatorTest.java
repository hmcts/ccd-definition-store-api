package uk.gov.hmcts.ccd.definition.store.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtIssuerValidatorTest {

    private static final String VALID_ISSUER = "http://fr-am:8080/openam/oauth2/hmcts";
    private static final String ADDITIONAL_ALLOWED_ISSUER = "http://public-idam/o";
    private static final String INVALID_ISSUER = "http://unexpected-issuer";
    private static final Instant VALID_EXPIRES_AT = Instant.parse("2099-01-01T00:00:00Z");
    private static final Instant EXPIRED_AT = Instant.parse("2000-01-01T00:00:00Z");

    @Test
    void shouldAcceptJwtFromPrimaryIssuerWhenAllowedIssuersUnset() {
        assertFalse(validator(null).validate(buildJwt(VALID_ISSUER, VALID_EXPIRES_AT)).hasErrors());
    }

    @Test
    void shouldAcceptJwtFromAdditionalAllowedIssuer() {
        assertFalse(validator(ADDITIONAL_ALLOWED_ISSUER)
            .validate(buildJwt(ADDITIONAL_ALLOWED_ISSUER, VALID_EXPIRES_AT))
            .hasErrors());
    }

    @Test
    void shouldKeepPrimaryIssuerWhenAdditionalAllowedIssuersConfigured() {
        assertFalse(validator(ADDITIONAL_ALLOWED_ISSUER)
            .validate(buildJwt(VALID_ISSUER, VALID_EXPIRES_AT))
            .hasErrors());
    }

    @Test
    void shouldRejectJwtFromUnexpectedIssuer() {
        assertTrue(validator(ADDITIONAL_ALLOWED_ISSUER)
            .validate(buildJwt(INVALID_ISSUER, VALID_EXPIRES_AT))
            .hasErrors());
    }

    @Test
    void shouldRejectJwtWithoutIssuer() {
        assertTrue(validator(ADDITIONAL_ALLOWED_ISSUER)
            .validate(buildJwtWithoutIssuer(VALID_EXPIRES_AT))
            .hasErrors());
    }

    @Test
    void shouldRejectExpiredJwtEvenWhenIssuerMatches() {
        assertTrue(validator("").validate(buildJwt(VALID_ISSUER, EXPIRED_AT)).hasErrors());
    }

    private OAuth2TokenValidator<Jwt> validator(String allowedIssuers) {
        return JwtIssuerValidator.validator(VALID_ISSUER, allowedIssuers);
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
