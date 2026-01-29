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

    private volatile RestClient restClient;
    private volatile RestClientTransport transport;
    private volatile ElasticsearchClient elasticsearchClient;
    private volatile ElasticsearchAsyncClient elasticsearchAsyncClient;

    public ElasticsearchClientFactory(Supplier<RestClient> restClientSupplier, JacksonJsonpMapper mapper) {
        this.restClientSupplier = restClientSupplier;
        this.mapper = mapper;
    }

    public RestClient createLowLevelClient() {
        if (restClient == null) {
            synchronized (this) {
                if (restClient == null) {
                    restClient = restClientSupplier.get();
                }
            }
        }
        return restClient;
    }

    private RestClientTransport createTransport() {
        if (transport == null) {
            synchronized (this) {
                if (transport == null) {
                    transport = new RestClientTransport(createLowLevelClient(), mapper);
                }
            }
        }
        return transport;
    }

    public ElasticsearchClient createClient() {
        if (elasticsearchClient == null) {
            synchronized (this) {
                if (elasticsearchClient == null) {
                    ElasticsearchTransport t = createTransport();
                    elasticsearchClient = new ElasticsearchClient(t);
                    try {
                        elasticsearchClient.cluster().putSettings(s ->
                            s.persistent(Map.of("action.destructive_requires_name", JsonData.of(false)))
                        );
                    } catch (IOException e) {
                        log.error("Failed to put cluster settings during client init", e);
                    }
                }
            }
        }
        return elasticsearchClient;
    }

    public ElasticsearchAsyncClient createAsyncClient() {
        if (elasticsearchAsyncClient == null) {
            synchronized (this) {
                if (elasticsearchAsyncClient == null) {
                    elasticsearchAsyncClient = new ElasticsearchAsyncClient(createTransport());
                }
            }
        }
        return elasticsearchAsyncClient;
    }
}
