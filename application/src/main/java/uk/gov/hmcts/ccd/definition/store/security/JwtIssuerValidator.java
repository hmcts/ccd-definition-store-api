package uk.gov.hmcts.ccd.definition.store.security;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

public final class JwtIssuerValidator {

    private JwtIssuerValidator() {
    }

    public static OAuth2TokenValidator<Jwt> validator(String primaryIssuer, String additionalAllowedIssuers) {
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> withIssuer = new JwtClaimValidator<>(
            "iss",
            OidcIssuerConfiguration.allowedIssuers(primaryIssuer, additionalAllowedIssuers)::contains
        );
        return new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuer);
    }
}
