package uk.gov.hmcts.ccd.definition.store.elastic.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.HttpHost;
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

    private CcdElasticSearchProperties config;

    public ElasticSearchConfiguration(CcdElasticSearchProperties config) {
        this.config = config;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public JacksonJsonpMapper jsonpMapper(ObjectMapper objectMapper) {
        return new JacksonJsonpMapper(objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper(); // customize if needed
    }

    @Bean
    protected RestClientBuilder elasticsearchRestClientBuilder() {
        return RestClient.builder(new HttpHost(config.getHost(), config.getPort()))
            .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                .setConnectTimeout(5000)
                .setSocketTimeout(60000))
            .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                .setDefaultIOReactorConfig(IOReactorConfig.custom()
                    .setSoKeepAlive(true)
                    .build()));
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RestClient restClient(RestClientBuilder builder) {
        return builder.build();
    }

    /**
     * NOTE: imports seldom happen. To prevent unused connections to the ES cluster hanging around, we create a new
     * ElasticsearchClient on each import and we close it once the import is completed.
     * The ElasticsearchClient is injected every time with a new restClientTransport which opens new connections
     */
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ElasticsearchClient elasticsearchClient(RestClientTransport transport) {
        return new ElasticsearchClient(transport);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RestClientTransport restClientTransport(RestClient restClient, JacksonJsonpMapper mapper) {
        return new RestClientTransport(restClient, mapper);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public HighLevelCCDElasticClient ccdElasticClient(ElasticsearchClient elasticsearchClient) {
        return new HighLevelCCDElasticClient(config, elasticsearchClient) {
        };
    }
}
