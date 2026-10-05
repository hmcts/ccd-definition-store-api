package uk.gov.hmcts.net.ccd.definition.store;

import io.jsonwebtoken.Jwts;
import uk.gov.hmcts.net.ccd.definition.store.util.KeyGenerator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestPropertySource(properties = {
    "idam.security.allowed-issuers=http://localhost:${wiremock.server.port}/o,http://alternate.issuer/o"
})
class SecurityConfigurationIT extends BaseTest {

    private static final String ISSUER = "http://localhost:%s/o";

    @Autowired
    private JwtDecoder jwtDecoder;

    @Value("${wiremock.server.port}")
    private Integer wiremockPort;

    @Test
    void shouldDecodeJwtWhenIssuerAndTimestampAreValid() throws Exception {
        String issuer = issuer();
        String token = createToken(issuer, Instant.now().plusSeconds(300));

        Jwt jwt = jwtDecoder.decode(token);

        assertThat(jwt.getIssuer().toString()).isEqualTo(issuer);
    }

    @Test
    void shouldDecodeJwtWhenAlternateIssuerIsUsed() throws Exception {
        String issuer = "http://alternate.issuer/o";
        String token = createToken(issuer, Instant.now().plusSeconds(300));

        Jwt jwt = jwtDecoder.decode(token);

        assertThat(jwt.getIssuer().toString()).isEqualTo(issuer);
    }

    @Test
    void shouldRejectJwtWhenIssuerIsNotAllowed() throws Exception {
        String token = createToken("https://untrusted-issuer.example", Instant.now().plusSeconds(300));

        assertThatThrownBy(() -> jwtDecoder.decode(token))
            .isInstanceOf(JwtValidationException.class);
    }

    @Test
    void shouldRejectJwtWhenTimestampHasExpired() throws Exception {
        String token = createToken(issuer(), Instant.now().minusSeconds(120));

        assertThatThrownBy(() -> jwtDecoder.decode(token))
            .isInstanceOf(JwtValidationException.class);
    }

    private String issuer() {
        return ISSUER.formatted(wiremockPort);
    }

    private String createToken(String issuer, Instant expiration) throws Exception {
        Instant issuedAt = expiration.minusSeconds(2);
        return Jwts.builder()
            .header().keyId(KeyGenerator.getRsaJWK().getKeyID()).and()
            .issuer(issuer)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiration))
            .signWith(KeyGenerator.getRsaJWK().toRSAPrivateKey(), Jwts.SIG.RS256)
            .compact();
    }

}