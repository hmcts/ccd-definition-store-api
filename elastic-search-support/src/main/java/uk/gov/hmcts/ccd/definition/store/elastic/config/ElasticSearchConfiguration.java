package uk.gov.hmcts.ccd.definition.store.elastic.config;

import co.elastic.clients.transport.ElasticsearchTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
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
@SuppressWarnings("java:S1874")
public class ElasticSearchConfiguration {

    private final CcdElasticSearchProperties config;

    public ElasticSearchConfiguration(CcdElasticSearchProperties config) {
        this.config = config;
    }

    /**
     * NOTE: imports happens seldom. To prevent unused connections to the ES cluster hanging around, we create a new
     * HighLevelCCDElasticClient on each import and we close it once the import is completed.
     * The HighLevelCCDElasticClient is injected every time with a new restHighLevelClient which opens new connections
     */
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ElasticsearchClient elasticsearchClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(config.getHost(), config.getPort()))
            .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                .setConnectTimeout(5000).setSocketTimeout(60000))
            .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                .setDefaultIOReactorConfig(IOReactorConfig.custom().setSoKeepAlive(true).build())
            );
        RestClient restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(config.getHost(), config.getPort())).build();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public HighLevelCCDElasticClient ccdElasticClient() {
        return new HighLevelCCDElasticClient(config, elasticsearchClient()) {
        };
    }
}
