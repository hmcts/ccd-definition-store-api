package uk.gov.hmcts.ccd.definition.store.elastic.integration;

import static com.google.common.collect.Lists.newArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.SynchronousElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.config.ElasticSearchConfiguration;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.SanityCheckApplication;
import uk.gov.hmcts.ccd.definition.store.repository.TestConfiguration;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        ElasticSearchConfiguration.class,
})
//@ContextConfiguration(initializers=ConfigFileApplicationContextInitializer.class)
@BootstrapWith(SpringBootTestContextBootstrapper.class)
//@ActiveProfiles("test")
@ContextConfiguration(classes = ElasticSearchConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
public class CaseMappingGenerationIT {

    @Autowired
    ElasticDefinitionImportListener listener;

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    CcdElasticSearchProperties config;

    @Test
    public void testMappingGeneration() {

        CaseTypeEntity caseType = new CaseTypeEntity();
        publisher.publishEvent(new DefinitionImportedEvent(newArrayList(caseType)));


    }
}
