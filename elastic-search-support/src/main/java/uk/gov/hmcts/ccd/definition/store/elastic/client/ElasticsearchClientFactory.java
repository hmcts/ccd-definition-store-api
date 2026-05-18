package uk.gov.hmcts.ccd.definition.store.elastic.client;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
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

    private RestClient restClient;
    private RestClientTransport transport;
    private ElasticsearchClient elasticsearchClient;
    private ElasticsearchAsyncClient elasticsearchAsyncClient;

    public ElasticsearchClientFactory(Supplier<RestClient> restClientSupplier, JacksonJsonpMapper mapper) {
        this.restClientSupplier = restClientSupplier;
        this.mapper = mapper;
    }

    public synchronized RestClient createLowLevelClient() {
        if (restClient == null) {
            restClient = restClientSupplier.get();
        }
        return restClient;
    }


    private synchronized RestClientTransport createTransport() {
        if (transport == null) {
            transport = new RestClientTransport(createLowLevelClient(), mapper);
        }
        return transport;
    }

    public synchronized ElasticsearchClient createClient() {
        if (elasticsearchClient != null) {
            return elasticsearchClient;
        }

        try {
            ElasticsearchClient client = new ElasticsearchClient(createTransport());
            applyClusterSettings(client);
            elasticsearchClient = client;
            return client;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to create Elasticsearch client", e);
        }
    }

    public synchronized ElasticsearchAsyncClient createAsyncClient() {
        if (elasticsearchAsyncClient != null) {
            return elasticsearchAsyncClient;
        }

        try {
            ElasticsearchAsyncClient client = new ElasticsearchAsyncClient(createTransport());
            elasticsearchAsyncClient = client;
            return client;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to create Elasticsearch async client", e);
        }
    }

    private void applyClusterSettings(ElasticsearchClient client) {
        try {
            client.cluster().putSettings(s ->
                s.persistent(Map.of("action.destructive_requires_name", JsonData.of(false)))
            );
        } catch (IOException e) {
            log.error("Failed to put cluster settings during client init", e);
        }
    }
}
