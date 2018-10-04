package uk.gov.hmcts.ccd.definition.store.elastic.config;

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
public class ElasticSearchConfiguration {

    @Autowired
    private CcdElasticSearchProperties config;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(config.getHost(), config.getPort())).setMaxRetryTimeoutMillis(60000);
        RestClientBuilder.RequestConfigCallback requestConfigCallback = requestConfigBuilder ->
            requestConfigBuilder.setConnectTimeout(5000)
            .setSocketTimeout(60000);
        builder.setRequestConfigCallback(requestConfigCallback);
        return new RestHighLevelClient(builder);
    }
}
