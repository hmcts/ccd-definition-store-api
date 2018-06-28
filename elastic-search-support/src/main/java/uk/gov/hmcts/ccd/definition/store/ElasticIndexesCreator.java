package uk.gov.hmcts.ccd.definition.store;

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
public class ElasticIndexesCreator {

    private RestHighLevelClient elasticClient;

    @Autowired
    public ElasticIndexesCreator(RestHighLevelClient elasticClient) {
        this.elasticClient = elasticClient;
    }

    public void createIndexes(List<CaseTypeEntity> caseTypes) throws IOException {
        log.info("creating indexes for {} case types", caseTypes.size());

        caseTypes.forEach(ct -> {
            GetIndexRequest getReq = new GetIndexRequest();
            String indexName = indexName(ct);
            getReq.indices(indexName);

            boolean exists = false;
            try {
                exists = elasticClient.indices().exists(getReq);
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("index {} exists: {}", indexName, exists);
            if(!exists) {
                CreateIndexRequest createReq = new CreateIndexRequest(indexName);
                createReq.settings(Settings.builder()
                        .put("index.number_of_shards", 3)
                        .put("index.number_of_replicas", 2)
                );
                try {
                    CreateIndexResponse createIndexResponse = elasticClient.indices().create(createReq);
                    boolean acknowledged = createIndexResponse.isAcknowledged();
                    log.info("index {} created: {}", indexName, acknowledged);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    private String indexName(CaseTypeEntity caseType) {
        String jurisdiction = caseType.getJurisdiction().getName();
        String caseTypeId = caseType.getReference();
        return String.format("%s_%s_cases", jurisdiction.toLowerCase(), caseTypeId.toLowerCase());
    }
}
