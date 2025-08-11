package uk.gov.hmcts.ccd.definition.store.elastic;

import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.config.ElasticSearchConfiguration;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
