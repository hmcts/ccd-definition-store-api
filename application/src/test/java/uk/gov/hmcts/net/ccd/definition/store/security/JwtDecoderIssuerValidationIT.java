package uk.gov.hmcts.net.ccd.definition.store.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.ccd.definition.store.CaseDataAPIApplication;
import uk.gov.hmcts.net.ccd.definition.store.TestConfiguration;
import uk.gov.hmcts.net.ccd.definition.store.TestIdamConfiguration;
import uk.gov.hmcts.net.ccd.definition.store.util.KeyGenerator;
import uk.gov.hmcts.net.ccd.definition.store.wiremock.config.WireMockTestConfiguration;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {
    CaseDataAPIApplication.class,
    TestConfiguration.class,
    TestIdamConfiguration.class,
    WireMockTestConfiguration.class
})
@TestPropertySource(
    locations = "classpath:test.properties",
    properties = "oidc.issuer=http://localhost:${wiremock.server.port}/o"
)
@AutoConfigureWireMock(port = 0)
class JwtDecoderIssuerValidationIT {

    private static final String INVALID_ISSUER = "http://unexpected-issuer/o";

    @Autowired
    private JwtDecoder jwtDecoder;

    @Value("${oidc.issuer}")
    private String validIssuer;

    @Test
    void shouldDecodeJwtFromConfiguredIssuer() throws Exception {
        Jwt jwt = assertDoesNotThrow(() -> jwtDecoder.decode(buildToken(validIssuer, Instant.now().plusSeconds(300))));

        assertEquals(validIssuer, jwt.getIssuer().toString());
    }

    @Test
    void shouldRejectJwtFromUnexpectedIssuer() throws Exception {
        JwtValidationException exception = assertThrows(JwtValidationException.class,
            () -> jwtDecoder.decode(buildToken(INVALID_ISSUER, Instant.now().plusSeconds(300))));

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectExpiredJwtEvenWhenIssuerMatches() throws Exception {
        assertThrows(BadJwtException.class,
            () -> jwtDecoder.decode(buildToken(validIssuer, Instant.now().minusSeconds(60))));
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
                .issueTime(Date.from(Instant.now().minusSeconds(60)))
                .expirationTime(Date.from(expiresAt))
                .build()
        );
        signedJwt.sign(new RSASSASigner(KeyGenerator.getRsaJWK().toPrivateKey()));
        return signedJwt.serialize();
    }
}
