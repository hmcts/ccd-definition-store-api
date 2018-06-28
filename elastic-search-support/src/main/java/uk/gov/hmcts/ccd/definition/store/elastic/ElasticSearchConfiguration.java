package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Configuration
@PropertySource(value = "classpath:elastic-search-support.properties")
public class ElasticSearchConfiguration {

    @Value("${ccd.elasticsearch.host}")
    private String hostname;
    @Value("${ccd.elasticsearch.port}")
    private int port;

    @Bean
    public RestHighLevelClient getRestHighLevelClient() {
        return new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(hostname, port, "http")));
    }
}
