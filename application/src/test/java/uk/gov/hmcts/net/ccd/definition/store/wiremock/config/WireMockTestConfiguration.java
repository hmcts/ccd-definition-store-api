package uk.gov.hmcts.net.ccd.definition.store.wiremock.config;

import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ContextCleanupListener;
import uk.gov.hmcts.net.ccd.definition.store.wiremock.extensions.DynamicOAuthJwkSetResponseTransformer;
import uk.gov.hmcts.net.ccd.definition.store.wiremock.extensions.CustomisedResponseTransformer;

@Configuration
public class WireMockTestConfiguration extends ContextCleanupListener {

    @Bean
    public WireMockConfigurationCustomizer wireMockConfigurationCustomizer() {
        return config -> config.extensions(new CustomisedResponseTransformer(),
            new DynamicOAuthJwkSetResponseTransformer());
    }
}

