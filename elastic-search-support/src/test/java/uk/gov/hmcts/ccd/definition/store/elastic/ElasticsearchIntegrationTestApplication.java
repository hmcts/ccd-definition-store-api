package uk.gov.hmcts.ccd.definition.store.elastic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test application used for Spring Boot context initialization
 * during integration tests involving Elasticsearch.
 */
@SpringBootApplication
public class ElasticsearchIntegrationTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchIntegrationTestApplication.class, args);
    }
}
