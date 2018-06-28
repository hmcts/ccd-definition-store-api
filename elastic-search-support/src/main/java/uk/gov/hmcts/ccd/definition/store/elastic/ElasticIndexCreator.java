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
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;


@Service
@Slf4j
public class ElasticIndexCreator extends AbstractElasticSearchSupport {

    public void createIndex(CaseTypeEntity caseType) throws IOException {
        log.info("creating index for case type {}", caseType.getReference());

        String indexName = indexName(caseType);

        CreateIndexRequest createReq = new CreateIndexRequest(indexName);
        createReq.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );
        CreateIndexResponse createIndexResponse = elasticClient.indices().create(createReq);
        boolean acknowledged = createIndexResponse.isAcknowledged();
        log.info("index {} created: {}", indexName, acknowledged);
    }

    public String indexName(CaseTypeEntity caseType) {
        String jurisdiction = caseType.getJurisdiction().getName();
        String caseTypeId = caseType.getReference();
        return String.format("%s_%s_cases", jurisdiction.toLowerCase(), caseTypeId.toLowerCase());
    }
}
