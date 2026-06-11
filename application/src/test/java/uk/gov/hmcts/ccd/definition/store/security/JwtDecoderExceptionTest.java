package uk.gov.hmcts.ccd.definition.store.security;

import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import uk.gov.hmcts.net.ccd.definition.store.util.KeyGenerator;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.net.ccd.definition.store.util.JwtTestTokenBuilder.signedToken;
import static uk.gov.hmcts.net.ccd.definition.store.util.JwtTestTokenBuilder.signedTokenWithoutIssuer;

class JwtDecoderExceptionTest {

    private static final String VALID_ISSUER = "http://localhost/o";
    private static final String ADDITIONAL_ALLOWED_ISSUER = "http://public-idam/o";
    private static final String INVALID_ISSUER = "http://unexpected-issuer/o";

    @Test
    void shouldDecodeJwtFromPrimaryIssuerWhenAllowedIssuersUnset() throws Exception {
        Jwt jwt = jwtDecoder().decode(signedToken(VALID_ISSUER, Instant.now().plusSeconds(300)));

        assertThat(jwt.getIssuer().toString()).isEqualTo(VALID_ISSUER);
    }

    @Test
    void shouldDecodeJwtFromAdditionalAllowedIssuer() throws Exception {
        Jwt jwt = jwtDecoder(ADDITIONAL_ALLOWED_ISSUER)
            .decode(signedToken(ADDITIONAL_ALLOWED_ISSUER, Instant.now().plusSeconds(300)));

        assertThat(jwt.getIssuer().toString()).isEqualTo(ADDITIONAL_ALLOWED_ISSUER);
    }

    @Test
    void shouldExposeIssInUnexpectedIssuerFailure() throws Exception {
        JwtValidationException exception = assertThrows(JwtValidationException.class,
            () -> jwtDecoder().decode(signedToken(INVALID_ISSUER, Instant.now().plusSeconds(300))));

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectJwtWithoutIssuerAtDecoderLevel() throws Exception {
        JwtValidationException exception = assertThrows(JwtValidationException.class,
            () -> jwtDecoder().decode(signedTokenWithoutIssuer(Instant.now().plusSeconds(300))));

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectExpiredJwtAtDecoderLevel() throws Exception {
        assertThrows(BadJwtException.class,
            () -> jwtDecoder().decode(signedToken(VALID_ISSUER, Instant.now().minusSeconds(60))));
    }

    private JwtDecoder jwtDecoder() throws JOSEException {
        return jwtDecoder("");
    }

    private JwtDecoder jwtDecoder(String allowedIssuers) throws JOSEException {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(KeyGenerator.getRsaJWK().toRSAPublicKey()).build();
        jwtDecoder.setJwtValidator(JwtIssuerValidator.validator(VALID_ISSUER, allowedIssuers));
        return jwtDecoder;
    }
}
