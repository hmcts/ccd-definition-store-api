package uk.gov.hmcts.ccd.definition.store.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfiguration {

    @Bean
    public FlywayConfigurationCustomizer flywayCustomizer() {
        return configuration -> configuration.configuration(
            Map.of("flyway.postgresql.transactional.lock", "false")
        );
    }

}
