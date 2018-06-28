package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Service
@Slf4j
public class ElasticDefinitionImportListener extends AbstractElasticSearchSupport {

    @Value("${ccd.elasticsearch.index.cases.name}")
    private String indexNameFormat;

    @Autowired
    private ElasticIndexCreator indexCreator;

    @Autowired
    private ElasticMappingCreator mappingCreator;

    @Async
    @TransactionalEventListener
    void onDefinitionImported(DefinitionImportedEvent importEvent) throws InterruptedException {
            importEvent.getCaseTypes().forEach(ct -> {
                try {
                    log.info("initialising ElasticSearch for newly imported case type: {}", ct.getReference());
                    String indexName = indexName(ct);
                    GetIndexRequest getReq = createGetIndexRequest(indexName);

                    boolean exists = elasticClient.indices().exists(getReq);

                    log.info("index {} exists: {}", indexName, exists);

                    if(!exists) {
                        indexCreator.createIndex(indexName, ct);
                    }

                    mappingCreator.createMapping(indexName, ct);
                } catch (Exception e) {
                    log.warn("error while initialising ElasticSearch for new imported case {}. Your case might not be searchable ", ct.getReference(), e);
                }
            });
    }

    private GetIndexRequest createGetIndexRequest(String indexName) {
        GetIndexRequest getReq = new GetIndexRequest();
        getReq.indices(indexName);
        return getReq;
    }


    private String indexName(CaseTypeEntity caseType) {
        String jurisdiction = caseType.getJurisdiction().getName();
        String caseTypeId = caseType.getReference();
        return String.format(indexNameFormat, jurisdiction.toLowerCase(), caseTypeId.toLowerCase());
    }
}
