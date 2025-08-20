package uk.gov.hmcts.ccd.definition.store.elastic.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.NodeSelector;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.ccd.definition.store.elastic.client.ElasticsearchClientFactory;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserInfoMixin;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

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
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ObjectMapper objectMapper() {
        return new Jackson2ObjectMapperBuilder()
            .featuresToEnable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .featuresToEnable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            .featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
            .mixIn(UserInfo.class, UserInfoMixin.class)
            .modulesToInstall(JavaTimeModule.class)
            .build();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected RestClientBuilder elasticsearchRestClientBuilder() {
        return RestClient.builder(new HttpHost(config.getHost(), config.getPort(), HttpHost.DEFAULT_SCHEME_NAME))
            .setFailureListener(new RestClient.FailureListener() {
                @Override
                public void onFailure(Node node) {
                    log.warn("Node marked as dead: {}", node);
                }
            })
            .setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS)
            .setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                    .setConnectTimeout(5000)
                    .setSocketTimeout(60000)
            )
            .setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultIOReactorConfig(
                    IOReactorConfig.custom()
                        .setSoKeepAlive(true)
                        .build()
                )
            );
    }

    @Bean(destroyMethod = "close")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RestClient restClient(RestClientBuilder builder) {
        return builder.build();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ElasticsearchClient elasticsearchClient(ElasticsearchClientFactory elasticsearchClientFactory) {
        return elasticsearchClientFactory.createClient();
    }

    @Bean(destroyMethod = "close")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RestClientTransport restClientTransport(RestClient restClient, JacksonJsonpMapper mapper) {
        return new RestClientTransport(restClient, mapper);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ElasticsearchClientFactory elasticsearchClientFactory(JacksonJsonpMapper mapper) {
        return new ElasticsearchClientFactory(() -> elasticsearchRestClientBuilder().build(), mapper);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public HighLevelCCDElasticClient ccdElasticClient(ElasticsearchClientFactory elasticsearchClientFactory) {
        return new HighLevelCCDElasticClient(config, elasticsearchClientFactory) {
        };
    }
}
