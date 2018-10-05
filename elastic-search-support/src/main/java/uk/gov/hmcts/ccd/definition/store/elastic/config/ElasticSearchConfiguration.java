package uk.gov.hmcts.ccd.definition.store.elastic.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("uk.gov.hmcts.ccd.definition.store.elastic")
@EnableConfigurationProperties(value = CcdElasticSearchProperties.class)
@Slf4j
public class ElasticSearchConfiguration {

    @Autowired
    private CcdElasticSearchProperties config;

    private RestHighLevelClient restHighLevelClient;

    @PostConstruct
    public void init() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(config.getHost(), config.getPort())).setMaxRetryTimeoutMillis(60000);
        RestClientBuilder.RequestConfigCallback requestConfigCallback = requestConfigBuilder ->
            requestConfigBuilder.setConnectTimeout(5000)
                .setSocketTimeout(60000);
        builder.setRequestConfigCallback(requestConfigCallback);
        restHighLevelClient = new RestHighLevelClient(builder);
    }


    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return restHighLevelClient;
    }

    @PreDestroy
    public void cleanup() {
        try {
            log.info("Closing the ES REST client");
            this.restHighLevelClient.close();
        } catch (IOException ioe) {
            log.error("Problem occurred when closing the ES REST client", ioe);
        }
    }
}
