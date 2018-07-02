package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Service
@Slf4j
public class ElasticDefinitionImportListener extends AbstractElasticSearchSupport {

    @Autowired
    private ElasticCasesIndexCreator indexCreator;

    @Autowired
    private ElasticCasesMappingCreator mappingCreator;

    @Async
    @TransactionalEventListener
    void onDefinitionImported(DefinitionImportedEvent event) throws IOException {

        for (CaseTypeEntity ct : event.getCaseTypes()) {
            if (!indexExists(ct)) {
                indexCreator.createIndex(ct);
            }
            mappingCreator.createMapping(ct);
        }
    }

    private boolean indexExists(CaseTypeEntity caseType) throws IOException {
        String indexName = indexName(caseType);
        GetIndexRequest getReq = new GetIndexRequest();
        getReq.indices(indexName);
        boolean exists = elasticClient.indices().exists(getReq);
        log.info("index for case type {} exists: {}", caseType.getReference(), exists);
        return exists;
    }
}
