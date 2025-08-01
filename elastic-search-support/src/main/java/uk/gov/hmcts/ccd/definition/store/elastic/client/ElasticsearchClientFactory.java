package uk.gov.hmcts.ccd.definition.store.elastic.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.elasticsearch.client.RestClient;

import java.util.function.Supplier;

public class ElasticsearchClientFactory {

    private final Supplier<RestClient> restClientSupplier;
    private final JacksonJsonpMapper mapper;

    public ElasticsearchClientFactory(Supplier<RestClient> restClientSupplier, JacksonJsonpMapper mapper) {
        this.restClientSupplier = restClientSupplier;
        this.mapper = mapper;
    }

    public ElasticsearchClient createClient() {
        RestClient restClient = restClientSupplier.get();
        RestClientTransport transport = new RestClientTransport(restClient, mapper);
        return new ElasticsearchClient(transport);
    }
}
