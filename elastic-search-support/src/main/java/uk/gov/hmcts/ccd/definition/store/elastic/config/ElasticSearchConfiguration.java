package uk.gov.hmcts.ccd.definition.store.elastic.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfiguration {

    @Autowired
    private CcdElasticSearchProperties config;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(config.getHost(), config.getPort(), config.getScheme())));
    }
}
