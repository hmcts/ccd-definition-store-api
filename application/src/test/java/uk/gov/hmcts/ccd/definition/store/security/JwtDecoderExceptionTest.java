package uk.gov.hmcts.ccd.definition.store.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import uk.gov.hmcts.net.ccd.definition.store.util.KeyGenerator;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtDecoderExceptionTest {

    private static final String VALID_ISSUER = "http://localhost/o";
    private static final String ADDITIONAL_ALLOWED_ISSUER = "http://public-idam/o";
    private static final String INVALID_ISSUER = "http://unexpected-issuer/o";

    @Test
    void shouldDecodeJwtFromPrimaryIssuerWhenAllowedIssuersUnset() throws Exception {
        Jwt jwt = jwtDecoder().decode(buildToken(VALID_ISSUER, Instant.now().plusSeconds(300)));

        assertThat(jwt.getIssuer().toString()).isEqualTo(VALID_ISSUER);
    }

    @Test
    void shouldDecodeJwtFromAdditionalAllowedIssuer() throws Exception {
        Jwt jwt = jwtDecoder(ADDITIONAL_ALLOWED_ISSUER)
            .decode(buildToken(ADDITIONAL_ALLOWED_ISSUER, Instant.now().plusSeconds(300)));

        assertThat(jwt.getIssuer().toString()).isEqualTo(ADDITIONAL_ALLOWED_ISSUER);
    }

    @Test
    void shouldExposeIssInUnexpectedIssuerFailure() throws Exception {
        JwtValidationException exception = assertThrows(JwtValidationException.class,
            () -> jwtDecoder().decode(buildToken(INVALID_ISSUER, Instant.now().plusSeconds(300))));

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectJwtWithoutIssuerAtDecoderLevel() throws Exception {
        JwtValidationException exception = assertThrows(JwtValidationException.class,
            () -> jwtDecoder().decode(buildTokenWithoutIssuer(Instant.now().plusSeconds(300))));

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectExpiredJwtAtDecoderLevel() throws Exception {
        assertThrows(BadJwtException.class,
            () -> jwtDecoder().decode(buildToken(VALID_ISSUER, Instant.now().minusSeconds(60))));
    }

    private JwtDecoder jwtDecoder() throws JOSEException {
        return jwtDecoder("");
    }

    private JwtDecoder jwtDecoder(String allowedIssuers) throws JOSEException {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(KeyGenerator.getRsaJWK().toRSAPublicKey()).build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            new JwtClaimValidator<>(
                "iss",
                OidcIssuerConfiguration.allowedIssuers(VALID_ISSUER, allowedIssuers)::contains
            )
        );
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }

    private String buildToken(String issuer, Instant expiresAt) throws JOSEException, ParseException {
        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(KeyGenerator.getRsaJWK().getKeyID())
                .type(JOSEObjectType.JWT)
                .build(),
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject("user")
                .issueTime(Date.from(expiresAt.minusSeconds(60)))
                .expirationTime(Date.from(expiresAt))
                .build()
        );
        signedJwt.sign(new RSASSASigner(KeyGenerator.getRsaJWK().toPrivateKey()));
        return signedJwt.serialize();
    }

    private String buildTokenWithoutIssuer(Instant expiresAt) throws JOSEException, ParseException {
        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(KeyGenerator.getRsaJWK().getKeyID())
                .type(JOSEObjectType.JWT)
                .build(),
            new JWTClaimsSet.Builder()
                .subject("user")
                .issueTime(Date.from(expiresAt.minusSeconds(60)))
                .expirationTime(Date.from(expiresAt))
                .build()
        );
        signedJwt.sign(new RSASSASigner(KeyGenerator.getRsaJWK().toPrivateKey()));
        return signedJwt.serialize();
    }
}
