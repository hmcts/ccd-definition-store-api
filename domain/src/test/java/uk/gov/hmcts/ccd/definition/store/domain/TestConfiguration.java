package uk.gov.hmcts.ccd.definition.store.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class TestConfiguration {
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
