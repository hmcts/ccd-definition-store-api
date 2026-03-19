package uk.gov.hmcts.ccd.definitionstore.befta;

import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.Env;

import java.text.ParseException;

public final class JwtIssuerVerificationApp {

    private JwtIssuerVerificationApp() {
    }

    public static void main(String[] args) {
        String expectedIssuer = Env.require("OIDC_ISSUER");
        String actualIssuer = resolveIssuerFromRealToken(AATHelper.INSTANCE);

        if (!expectedIssuer.equals(actualIssuer)) {
            throw new IllegalStateException(
                "OIDC_ISSUER mismatch: expected `" + expectedIssuer + "` but token iss was `" + actualIssuer + "`"
            );
        }

        System.out.println("Verified OIDC_ISSUER matches functional test token iss: " + actualIssuer);
    }

    private static String resolveIssuerFromRealToken(AATHelper aat) {
        String accessToken = aat.getIdamHelper()
            .getIdamOauth2Token(aat.getCaseworkerAutoTestEmail(), aat.getCaseworkerAutoTestPassword());

        try {
            String issuer = SignedJWT.parse(accessToken).getJWTClaimsSet().getIssuer();
            if (StringUtils.isBlank(issuer)) {
                throw new IllegalStateException("Decoded IDAM access token did not contain an iss claim");
            }
            return issuer;
        } catch (ParseException exception) {
            throw new IllegalStateException("Failed to parse IDAM access token as a JWT", exception);
        }
    }
}
