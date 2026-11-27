package uk.gov.hmcts.ccd.definition.store.domain;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecentralisedEventsConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfiguration.class);

    @Test
    void shouldBindWhitelistsForMultipleCaseTypes() {
        contextRunner.withPropertyValues(
            "decentralised-events.case-types.pcs=ext:create,ext:update",
            "decentralised-events.case-types.civil=ext:submit",
            "decentralised-events.case-types.empty="
        ).run(context -> {
            DecentralisedEventsConfiguration configuration = context.getBean(DecentralisedEventsConfiguration.class);
            assertEquals(List.of("ext:create", "ext:update"), configuration.getCaseTypes().get("pcs"));
            assertEquals(List.of("ext:submit"), configuration.getCaseTypes().get("civil"));
            assertTrue(configuration.getCaseTypes().get("empty").isEmpty());
        });
    }

    @Test
    void shouldDefaultToEmptyMapWhenNoWhitelistsConfigured() {
        contextRunner.run(context -> assertTrue(context.getBean(DecentralisedEventsConfiguration.class)
            .getCaseTypes().isEmpty()));
    }

    @Configuration
    @EnableConfigurationProperties(DecentralisedEventsConfiguration.class)
    static class TestConfiguration {
    }
}
