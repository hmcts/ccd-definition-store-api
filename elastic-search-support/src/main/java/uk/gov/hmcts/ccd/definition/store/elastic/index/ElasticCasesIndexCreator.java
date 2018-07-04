package uk.gov.hmcts.ccd.definition.store.elastic.index;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.elastic.AbstractElasticSearchSupport;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;


@Service
@Slf4j
public class ElasticCasesIndexCreator extends AbstractElasticSearchSupport {

    public void createIndex(CaseTypeEntity caseType) throws IOException {
        String indexName = indexName(caseType);
        log.info("creating index {} for case type {}", caseType.getReference());

        CreateIndexRequest request = createCreateIndexRequest(indexName);
        CreateIndexResponse createIndexResponse = elasticClient.indices().create(request);
        boolean acknowledged = createIndexResponse.isAcknowledged();
        log.info("index created: {}", acknowledged);
    }

    private CreateIndexRequest createCreateIndexRequest(String indexName) {
        CreateIndexRequest createReq = new CreateIndexRequest(indexName);
        createReq.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );
        return createReq;
    }
}
