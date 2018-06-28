package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;


@Service
@Slf4j
public class ElasticIndexCreator extends AbstractElasticSearchSupport {

    public void createIndex(String indexName, CaseTypeEntity caseType) throws IOException {
        log.info("creating index for case type {}", caseType.getReference());

        CreateIndexRequest request = createCreateIndexRequest(indexName);
        CreateIndexResponse createIndexResponse = elasticClient.indices().create(request);
        boolean acknowledged = createIndexResponse.isAcknowledged();
        log.info("index {} created: {}", indexName, acknowledged);
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
