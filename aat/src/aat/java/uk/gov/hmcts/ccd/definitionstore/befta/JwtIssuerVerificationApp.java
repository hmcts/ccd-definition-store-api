package uk.gov.hmcts.ccd.definitionstore.befta;

import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.security.OidcIssuerConfiguration;
import uk.gov.hmcts.ccd.definitionstore.tests.AATHelper;
import uk.gov.hmcts.ccd.definitionstore.tests.Env;

import java.text.ParseException;
import java.util.Set;

@Slf4j
public final class JwtIssuerVerificationApp {

    private JwtIssuerVerificationApp() {
    }

    public static void main(String[] args) {
        Set<String> allowedIssuers = OidcIssuerConfiguration.allowedIssuers(
            Env.require("OIDC_ISSUER"),
            System.getenv("OIDC_ALLOWED_ISSUERS")
        );
        String actualIssuer = resolveIssuerFromRealToken(AATHelper.INSTANCE);

        if (!allowedIssuers.contains(actualIssuer)) {
            throw new IllegalStateException(
                "OIDC issuer mismatch: expected one of `" + String.join("`, `", allowedIssuers)
                    + "` but token iss was `" + actualIssuer + "`"
            );
        }

        log.info("Verified functional test token iss is allowed: {}", actualIssuer);
    }

    private static String resolveIssuerFromRealToken(AATHelper aat) {
        Credentials credentials = importerCredentials(aat);
        String accessToken = aat.getIdamHelper()
            .getIdamOauth2Token(credentials.username(), credentials.password());

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

    private static Credentials importerCredentials(AATHelper aat) {
        return new Credentials(aat.getImporterAutoTestEmail(), aat.getImporterAutoTestPassword());
    }

    private record Credentials(String username, String password) {
    }
}
