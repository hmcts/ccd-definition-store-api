package uk.gov.hmcts.ccd.definition.store;

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

    @Value("${spring.data.elasticsearch.cluster-nodes}")
    private String clusterNodes;
    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;
//    private RestHighLevelClient restHighLevelClient;


//    @Override
//    public void destroy() {
//        try {
//            if (restHighLevelClient != null) {
//                log.info("stopping ElasticSearch client");
//                restHighLevelClient.close();
//            }
//        } catch (final Exception e) {
//            log.error("Error closing ElasticSearch client: ", e);
//        }
//    }
//
//    @Override
//    public Class<RestHighLevelClient> getObjectType() {
//        return RestHighLevelClient.class;
//    }
//
//    @Override
//    public boolean isSingleton() {
//        return false;
//    }
//
//    @Override
//    public RestHighLevelClient createInstance() {
//        return buildClient();
//    }
//
//    private RestHighLevelClient buildClient() {
//        try {
//
//            log.info("nodes: "+clusterNodes);
//            log.info("name: "+clusterName);
//            restHighLevelClient = new RestHighLevelClient(
//                    RestClient.builder(
//                            new HttpHost("localhost", 9200, "http"),
//                            new HttpHost("localhost", 9201, "http")));
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return restHighLevelClient;
//    }

    @Bean
    public RestHighLevelClient getRestHighLevelClient() {
        return new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost("localhost", 9200, "http")));
    }
}
