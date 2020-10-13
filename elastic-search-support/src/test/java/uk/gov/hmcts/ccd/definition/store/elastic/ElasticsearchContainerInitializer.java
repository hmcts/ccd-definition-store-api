package uk.gov.hmcts.ccd.definition.store.elastic;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ElasticsearchContainerInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Container
    private GenericContainer<ElasticsearchContainer> elasticsearchContainer;

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        elasticsearchContainer = CcdElasticsearchContainer.getInstance();
        TestPropertyValues
            .of("elasticsearch.port=" + elasticsearchContainer.getMappedPort(9200),
                "elasticsearch.host=" + elasticsearchContainer.getHost())
            .applyTo(configurableApplicationContext.getEnvironment());
    }
}
