package uk.gov.hmcts.ccd.definition.store.elastic.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.definition.store.elastic.CcdElasticSearchProperties;

@Slf4j
@Configuration
public class ElasticSearchConfiguration {

    @Autowired
    private CcdElasticSearchProperties properties;

    @Bean
    public RestHighLevelClient getRestHighLevelClient() {
        return new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(properties.getHost(), properties.getPort(), "http")));
    }
}
