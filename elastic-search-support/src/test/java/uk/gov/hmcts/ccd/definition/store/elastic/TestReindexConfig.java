package uk.gov.hmcts.ccd.definition.store.elastic;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.mockito.Mockito.mock;

/**
 * Test configuration for reindex integration tests.
 * Registers minimal JPA and mocks external dependencies (IDAM).
 */
@TestConfiguration
@EnableJpaRepositories(basePackageClasses = ReindexRepository.class)
@EntityScan(basePackageClasses = ReindexEntity.class)
public class TestReindexConfig {

    @Bean
    @Primary
    public IdamClient mockIdamClient() {
        return mock(IdamClient.class);
    }

    @Bean
    @Primary
    public IdamApi mockIdamApi() {
        return mock(IdamApi.class);
    }
}
