package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.handler.ElasticsearchErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ElasticGlobalSearchListener {

    private static final String FIRST_INDEX_SUFFIX = "-000001";
    public static final String GLOBAL_SEARCH = "global_search";

    private final ObjectFactory<HighLevelCCDElasticClient> clientFactory;

    private final ElasticsearchErrorHandler elasticsearchErrorHandler;

    public ElasticGlobalSearchListener(ObjectFactory<HighLevelCCDElasticClient> clientFactory,
                                       ElasticsearchErrorHandler elasticsearchErrorHandler) {

        this.clientFactory = clientFactory;
        this.elasticsearchErrorHandler = elasticsearchErrorHandler;
    }

    @Transactional
    public void initialiseElasticSearchForGlobalSearch() {
        HighLevelCCDElasticClient elasticClient = null;
        try (InputStream inputStream = getClass().getResourceAsStream("/globalSearchCasesMapping.json")) {
            elasticClient = clientFactory.getObject();
            if (!elasticClient.aliasExists(GLOBAL_SEARCH)) {
                String actualIndexName = GLOBAL_SEARCH + FIRST_INDEX_SUFFIX;
                elasticClient.createIndex(actualIndexName, GLOBAL_SEARCH);
            }

            String mapping = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.debug("case mapping: {}", mapping);
            elasticClient.upsertMapping(GLOBAL_SEARCH, mapping);

        } catch (IOException | ElasticsearchStatusException e) {
            throw new ElasticSearchInitialisationException(e);
        }
    }
}
