package uk.gov.hmcts.ccd.definition.store;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
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

    public void createIndexes(List<CaseTypeEntity> caseTypes) {
        log.info("creating indexes for {} case types", caseTypes.size());

        GetIndexRequest r = new GetIndexRequest();
        List<String> allIndex = Arrays.asList(r.indices());
        log.info("existing indexes: {}", allIndex);

    }
}
