package uk.gov.hmcts.ccd.definition.store;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;

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
    private ElasticIndexCreator indexCreator;

    @Async
    @TransactionalEventListener
    void onDefinitionImported(DefinitionImportedEvent event) throws InterruptedException {
        try {
            List<CaseTypeEntity> caseTypes = event.getCaseTypes();
            log.info("initialising ElasticSearch for newly imported case types: {}", casesName(caseTypes));

            caseTypes.forEach(ct -> {
                GetIndexRequest getReq = new GetIndexRequest();
                String indexName = indexCreator.indexName(ct);
                getReq.indices(indexName);

                boolean exists = false;
                try {
                    exists = elasticClient.indices().exists(getReq);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                log.info("index {} exists: {}", indexName, exists);

                if(!exists) {
                    try {
                        indexCreator.createIndex(ct);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            log.warn("error while initialising ElasticSearch for new imported cases. Your cases might not be searchable ", e);
        }
    }

    private List<String> casesName(List<CaseTypeEntity> caseTypes) {
        return caseTypes.stream().map(CaseTypeEntity::getReference).collect(toList());
    }

}
