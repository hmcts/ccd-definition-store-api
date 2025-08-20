package uk.gov.hmcts.ccd.definition.store.elastic.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.Alias;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import co.elastic.clients.elasticsearch.indices.update_aliases.Action;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.elasticsearch.action.ActionListener;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static uk.gov.hmcts.ccd.definition.store.elastic.ElasticGlobalSearchListener.GLOBAL_SEARCH;

@Slf4j
@SuppressWarnings("java:S1874")
public class HighLevelCCDElasticClient implements CCDElasticClient, AutoCloseable {

    private static final String CASES_INDEX_SETTINGS_JSON = "/casesIndexSettings.json";
    private static final String GLOBAL_SEARCH_CASES_INDEX_SETTINGS_JSON = "/globalSearchCasesIndexSettings.json";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final Object LOCK = new Object();

    private final CcdElasticSearchProperties config;
    private ElasticsearchClient elasticClient;

    @Autowired
    public HighLevelCCDElasticClient(CcdElasticSearchProperties config, ElasticsearchClientFactory clientFactory) {
        this.config = config;
        elasticClient = clientFactory.createClient();
    }

    private synchronized ElasticsearchClient getElasticClient() {
        return elasticClient;
    }

    private <T> T executeWithRetry(ElasticOperation<T> operation, String operationName) throws IOException {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                synchronized (LOCK) {
                    return operation.execute(getElasticClient());
                }
            } catch (Exception e) {
                lastException = e;
                attempts++;

                if (isDeadHostException(e)) {
                    log.warn("ElasticsearchClient encountered dead node: {} — resetting...", e.getMessage());
                } else if (isConnectionError(e)) {
                    log.warn("Connection error during {}, attempt {}/{}",
                        operationName, attempts, MAX_RETRIES);
                }

                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Operation interrupted", ie);
                    }
                }
            }
        }

        throw new IOException("Failed to execute " + operationName
            + " after " + MAX_RETRIES + " attempts", lastException);
    }

    private boolean isDeadHostException(Exception e) {
        return e.getMessage() != null && e.getMessage().contains("DeadHostState");
    }

    private boolean isConnectionError(Exception e) {
        return e instanceof ConnectionClosedException
            || e instanceof ElasticsearchException
            && e.getCause() instanceof ConnectionClosedException;
    }

    @Override
    public boolean createIndex(String indexName, String alias) throws IOException {
        log.info("creating index {} with alias {}", indexName, alias);
        final String file = (alias.equalsIgnoreCase(GLOBAL_SEARCH))
            ? GLOBAL_SEARCH_CASES_INDEX_SETTINGS_JSON : CASES_INDEX_SETTINGS_JSON;
        log.info("file: {}", file);

        // Load settings from JSON file as a Map
        Map<String, Object> settings = casesIndexSettings(file);
        log.info("settings: {}", settings);

        CreateIndexResponse createIndexResponse = executeWithRetry(
            client -> client.indices().create(b -> b
                .index(indexName)
                .settings(s -> {
                    try {
                        return s.withJson(new StringReader(
                            new ObjectMapper().writeValueAsString(settings)
                        ));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .aliases(Map.of(alias, new Alias.Builder().isWriteIndex(true).build()))
            ),
            "create index"
        );

        log.info("index created: {}, aliasExists: {}",
            null == createIndexResponse ? null : createIndexResponse.acknowledged(),
            aliasExists(alias));
        return null != createIndexResponse && createIndexResponse.acknowledged();
    }

    @Override
    public boolean upsertMapping(String aliasName, String caseTypeMapping) throws IOException {
        log.info("upsert mapping of most recent index for alias {}", aliasName);
        // Get aliases with retry
        var aliasesResponse = executeWithRetry(
            client -> client.indices().getAlias(b -> b.name(aliasName)),
            "get alias for upsert mapping"
        );
        String currentIndex = getCurrentAliasIndex(aliasName, aliasesResponse);
        log.info("upsert mapping of index {}", currentIndex);

        // Put mapping with retry
        var putMappingResponse = executeWithRetry(
            client -> client.indices().putMapping(b -> b
                .index(currentIndex)
                .withJson(new StringReader(caseTypeMapping))
            ),
            "put mapping"
        );
        log.info("mapping upserted: {}", putMappingResponse.acknowledged());
        return null != putMappingResponse && putMappingResponse.acknowledged();
    }

    @Override
    public boolean aliasExists(String alias) throws IOException {
        return executeWithRetry((client) -> {
            try {
                var response = client.indices().getAlias(b -> b.name(alias));
                boolean exists = response != null
                    && response.aliases() != null
                    && !response.aliases().isEmpty();
                log.debug("alias {} exists: {}", alias, exists);
                return exists;
            } catch (ElasticsearchException e) {
                if (e.status() == 404) {
                    return false;
                }
                throw e;
            }
        }, "check alias existence");
    }

    @FunctionalInterface
    private interface ElasticOperation<T> {
        T execute(ElasticsearchClient client) throws IOException;
    }

    @Override
    public void close() {
    }

    public GetAliasResponse getAlias(String alias) throws IOException {
        return getElasticClient().indices().getAlias(b -> b.name(alias));
    }

    private Map<String, Object> casesIndexSettings(String file) throws IOException {
        Map<String, Object> settings;
        try (InputStream inputStream = getClass().getResourceAsStream(file)) {
            if (inputStream == null) {
                throw new IOException("Settings file not found: " + file);
            }
            settings = new ObjectMapper().readValue(inputStream, Map.class);
        }
        settings.put("index.number_of_shards", config.getIndexShards());
        settings.put("index.number_of_replicas", config.getIndexShardsReplicas());
        settings.put("index.mapping.total_fields.limit", config.getCasesIndexMappingFieldsLimit());
        return settings;
    }

    private String getCurrentAliasIndex(String indexName, GetAliasResponse aliasesResponse) {
        ArrayList<String> indices = new ArrayList<>(aliasesResponse.aliases().keySet());
        Collections.sort(indices);
        log.info("found following indexes for alias {}: {}", indexName, indices);
        return Iterables.getLast(indices);
    }

    public void setIndexReadOnly(String indexName, boolean readOnly) throws IOException {
        getElasticClient().indices().putSettings(b -> b
            .index(indexName)
            .settings(s -> s
                .withJson(new java.io.StringReader(
                    "{\"index.blocks.read_only\": " + readOnly + "}"
                ))
            )
        );
        log.info("Set index '{}' read_only to {}", indexName, readOnly);
    }

    public boolean createIndexAndMapping(String indexName, String caseTypeMapping) throws IOException {
        // Load settings from JSON file as a Map
        Map<String, Object> settings;
        try (InputStream inputStream = getClass().getResourceAsStream(CASES_INDEX_SETTINGS_JSON)) {
            settings = new ObjectMapper().readValue(inputStream, Map.class);
        }

        var createIndexResponse = executeWithRetry(
            client -> client.indices().create(b -> b
                .index(indexName)
                .settings(s -> {
                    try {
                        return s.withJson(new java.io.StringReader(
                            new ObjectMapper().writeValueAsString(settings)
                        ));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
            ),
            "create index with mapping"
        );
        log.info("index created: {}", createIndexResponse.acknowledged());

        // Upsert mapping to new index
        var putMappingResponse = getElasticClient().indices().putMapping(b -> b
            .index(indexName)
            .withJson(new java.io.StringReader(caseTypeMapping))
        );
        log.info("mapping upserted: {}", putMappingResponse.acknowledged());

        return createIndexResponse.acknowledged();
    }

    public boolean updateAlias(String aliasName, String oldIndex, String newIndex) throws IOException {
        var aliasResponse = getElasticClient().indices().updateAliases(b -> b
            .actions(
                Action.of(a -> a.remove(r -> r.index(oldIndex).alias(aliasName))),
                Action.of(a -> a.add(ad -> ad.index(newIndex).alias(aliasName)))
            )
        );
        if (aliasResponse.acknowledged()) {
            log.info("alias successfully updated: {} now points to {}", oldIndex, newIndex);
        } else {
            log.info("alias update failed: {} still points to {}", oldIndex, oldIndex);
        }
        return null != aliasResponse && aliasResponse.acknowledged();
    }

    public boolean removeIndex(String indexName) throws IOException {
        var deleteResponse = getElasticClient().indices().delete(b -> b.index(indexName));
        if (deleteResponse.acknowledged()) {
            log.info("successfully deleted index: {}", indexName);
        } else {
            log.info("failed to delete index: {}", indexName);
        }
        return null != deleteResponse && deleteResponse.acknowledged();
    }

    public void reindexData(String oldIndex, String newIndex,
                            ActionListener<co.elastic.clients.elasticsearch.core.ReindexResponse> listener) {
        CompletableFuture.runAsync(() -> {
            try {
                var response = getElasticClient().reindex(b -> b
                    .source(s -> s.index(oldIndex))
                    .dest(d -> d.index(newIndex))
                    .refresh(true)
                );
                listener.onResponse(response);
            } catch (Exception e) {
                listener.onFailure(e);
            }
        });
    }
}
