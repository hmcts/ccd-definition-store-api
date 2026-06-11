package uk.gov.hmcts.ccd.definition.store.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OidcIssuerConfigurationTest {

    private static final String PRIMARY_ISSUER = "https://primary-idam.example.com/o";
    private static final String SECONDARY_ISSUER = "https://secondary-idam.example.com/o";
    private static final String TERTIARY_ISSUER = "https://tertiary-idam.example.com/o";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldOnlyHavePrimaryIssuerWhenAllowedIssuersMissing(String configuredAllowedIssuers) {
        assertThat(OidcIssuerConfiguration.allowedIssuers(PRIMARY_ISSUER, configuredAllowedIssuers))
            .containsExactly(PRIMARY_ISSUER);
    }

    @Test
    void shouldIncludePrimaryAndConfiguredAllowedIssuers() {
        assertThat(OidcIssuerConfiguration.allowedIssuers(
            PRIMARY_ISSUER,
            " " + SECONDARY_ISSUER + ", " + TERTIARY_ISSUER + " , " + SECONDARY_ISSUER + " "
        ))
            .containsExactly(PRIMARY_ISSUER, SECONDARY_ISSUER, TERTIARY_ISSUER);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allowedIssuerListsWithEmptyEntries")
    void shouldIgnoreEmptyConfiguredAllowedIssuerEntries(String name,
                                                         String configuredAllowedIssuers,
                                                         String[] expectedIssuers) {
        assertThat(OidcIssuerConfiguration.allowedIssuers(PRIMARY_ISSUER, configuredAllowedIssuers))
            .containsExactly(expectedIssuers);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectMissingPrimaryIssuer(String primaryIssuer) {
        assertThatThrownBy(() -> OidcIssuerConfiguration.allowedIssuers(primaryIssuer, SECONDARY_ISSUER))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("oidc.issuer must not be blank");
    }

    private static Stream<Arguments> allowedIssuerListsWithEmptyEntries() {
        return Stream.of(
            Arguments.of(
                "nothing before comma",
                ", " + SECONDARY_ISSUER,
                new String[]{PRIMARY_ISSUER, SECONDARY_ISSUER}
            ),
            Arguments.of(
                "nothing after comma",
                SECONDARY_ISSUER + ",",
                new String[]{PRIMARY_ISSUER, SECONDARY_ISSUER}
            ),
            Arguments.of(
                "nothing between commas",
                SECONDARY_ISSUER + ",," + TERTIARY_ISSUER,
                new String[]{PRIMARY_ISSUER, SECONDARY_ISSUER, TERTIARY_ISSUER}
            )
        );
    }
}
