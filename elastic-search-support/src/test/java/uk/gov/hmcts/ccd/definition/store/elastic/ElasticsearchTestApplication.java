package uk.gov.hmcts.ccd.definition.store.elastic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "uk.gov.hmcts.ccd.definition.store.elastic")
@EnableConfigurationProperties
public class ElasticsearchTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchTestApplication.class, args);
    }
}
