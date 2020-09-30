package uk.gov.hmcts.ccd.definition.store.elastic.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;

@Configuration
@ComponentScan("uk.gov.hmcts.ccd.definition.store.elastic")
@EnableConfigurationProperties(value = CcdElasticSearchProperties.class)
@Slf4j
public class ElasticSearchConfiguration {

    @Autowired
    private CcdElasticSearchProperties config;

    private RestHighLevelClient restHighLevelClient;

    /**
     * NOTE: imports happens seldom. To prevent unused connections to the ES cluster hanging around, we create a new
     * HighLevelCCDElasticClient on each import and we close it once the import is completed.
     * The HighLevelCCDElasticClient is injected every time with a new restHighLevelClient which opens new connections
     */
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(config.getHost(), config.getPort()))
            .setMaxRetryTimeoutMillis(60000);
        RestClientBuilder.RequestConfigCallback requestConfigCallback = requestConfigBuilder ->
            requestConfigBuilder.setConnectTimeout(5000)
                .setSocketTimeout(60000);
        builder.setRequestConfigCallback(requestConfigCallback);
        return new RestHighLevelClient(builder);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public HighLevelCCDElasticClient ccdElasticClient() {
        return new HighLevelCCDElasticClient(config, restHighLevelClient()) {
        };
    }
}
