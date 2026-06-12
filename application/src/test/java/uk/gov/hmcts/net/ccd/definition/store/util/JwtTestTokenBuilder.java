package uk.gov.hmcts.net.ccd.definition.store.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Instant;
import java.util.Date;

public final class JwtTestTokenBuilder {

    private JwtTestTokenBuilder() {
    }

    public static String signedToken(String issuer, Instant expiresAt) throws JOSEException {
        return serialize(claims(expiresAt).issuer(issuer).build());
    }

    public static String signedTokenWithoutIssuer(Instant expiresAt) throws JOSEException {
        return serialize(claims(expiresAt).build());
    }

    private static JWTClaimsSet.Builder claims(Instant expiresAt) {
        return new JWTClaimsSet.Builder()
            .subject("user")
            .issueTime(Date.from(expiresAt.minusSeconds(60)))
            .expirationTime(Date.from(expiresAt));
    }

    private static String serialize(JWTClaimsSet claimsSet) throws JOSEException {
        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(KeyGenerator.getRsaJWK().getKeyID())
                .type(JOSEObjectType.JWT)
                .build(),
            claimsSet
        );
        signedJwt.sign(new RSASSASigner(KeyGenerator.getRsaJWK().toPrivateKey()));
        return signedJwt.serialize();
    }
}
