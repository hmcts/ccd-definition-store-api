package uk.gov.hmcts.ccd.definition.store.elastic;

import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.config.ElasticSearchConfiguration;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;

@Configuration
public class ElasticsearchConfigurationIT extends ElasticSearchConfiguration {

    public ElasticsearchConfigurationIT(CcdElasticSearchProperties config) {
        super(config);
    }

    @Bean
    public CaseTypeRepository caseTypeRepository() {
        return mock(CaseTypeRepository.class);
    }

    @Bean
    @Qualifier("reindexExecutor")
    public Executor reindexExecutor() {
        // Use synchronous executor for tests to avoid async timing issues
        return Runnable::run;
    }
}
