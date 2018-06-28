package uk.gov.hmcts.ccd.definition.store;

import java.util.List;

import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;
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
        log.warn("creating indexes for {} case types", caseTypes.size());

    }

    @PreDestroy
    public void destroy() {
        try {
            elasticClient.close();
        } catch (Exception e) {
            log.error("Error closing ElasticSearch client: ", e);
        }
    }
}
