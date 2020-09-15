package uk.gov.hmcts.ccd.definition.store.elastic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.definition.store.elastic.config.ElasticSearchConfiguration;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;

import static org.mockito.Mockito.mock;

@Configuration
public class ElasticsearchConfigurationIT extends ElasticSearchConfiguration {

    @Bean
    public CaseTypeRepository caseTypeRepository() {
        return mock(CaseTypeRepository.class);
    }
}
