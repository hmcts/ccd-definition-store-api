package uk.gov.hmcts.ccd.definition.store.elastic.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchClientFactory {

    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public ElasticsearchClientFactory(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public ElasticsearchClient createClient() {
        return elasticsearchClient;
    }
}
