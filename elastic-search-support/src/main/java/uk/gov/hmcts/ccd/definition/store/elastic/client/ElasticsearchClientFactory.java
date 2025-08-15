package uk.gov.hmcts.ccd.definition.store.elastic.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class ElasticsearchClientFactory {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchClientFactory.class);
    private final Supplier<RestClient> restClientSupplier;
    private final JacksonJsonpMapper mapper;

    public ElasticsearchClientFactory(Supplier<RestClient> restClientSupplier, JacksonJsonpMapper mapper) {
        this.restClientSupplier = restClientSupplier;
        this.mapper = mapper;
    }

    public ElasticsearchClient createClient() {
        RestClient restClient = restClientSupplier.get();
        RestClientTransport transport = new RestClientTransport(restClient, mapper);
        ElasticsearchClient client = new ElasticsearchClient(transport);
        try {
            client.cluster().putSettings(s ->
                s.persistent(Map.of("action.destructive_requires_name", JsonData.of(false)))
            );
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return client;
    }
}
