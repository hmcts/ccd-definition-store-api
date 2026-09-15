package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * The testing support endpoints are excluded from authentication in SecurityConfiguration, so the only thing
 * standing between a caller and the deletion of case type definitions is the controller bean not being registered.
 * These tests pin that condition to the bean itself - a condition placed on the handler methods instead is silently
 * ignored by Spring MVC and leaves the endpoints exposed.
 */
class TestingSupportControllerConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(SessionFactoryConfiguration.class, TestingSupportController.class);

    @Test
    @DisplayName("Should not register the controller when the property is absent")
    void shouldNotRegisterControllerWhenPropertyAbsent() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(TestingSupportController.class));
    }

    @Test
    @DisplayName("Should not register the controller when testing support endpoints are disabled")
    void shouldNotRegisterControllerWhenDisabled() {
        contextRunner
            .withPropertyValues("testing-support-endpoints.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(TestingSupportController.class));
    }

    @Test
    @DisplayName("Should register the controller when testing support endpoints are enabled")
    void shouldRegisterControllerWhenEnabled() {
        contextRunner
            .withPropertyValues("testing-support-endpoints.enabled=true")
            .run(context -> assertThat(context).hasSingleBean(TestingSupportController.class));
    }

    @Configuration
    static class SessionFactoryConfiguration {

        @Bean
        SessionFactory sessionFactory() {
            return mock(SessionFactory.class);
        }
    }
}
