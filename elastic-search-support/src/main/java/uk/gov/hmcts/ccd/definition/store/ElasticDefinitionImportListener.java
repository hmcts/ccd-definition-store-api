package uk.gov.hmcts.ccd.definition.store;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Service
@Slf4j
public class ElasticDefinitionImportListener {

    private ElasticIndexesCreator elasticIndexesCreator;

    @Autowired
    public ElasticDefinitionImportListener(ElasticIndexesCreator elasticIndexesCreator) {
        System.out.println("ElasticDefinitionImportListener created");
        this.elasticIndexesCreator = elasticIndexesCreator;
    }

    @Async
    @TransactionalEventListener
    void onDefinitionImported(DefinitionImportedEvent event) throws InterruptedException {
        try {
            List<CaseTypeEntity> caseTypes = event.getCaseTypes();
            Thread.sleep(6000);
            log.info("notified of imported definition");
            elasticIndexesCreator.createIndexes(caseTypes);
            throw new RuntimeException("failed");
        } catch (Exception e) {
            log.warn("error while creating index", e);
        }

    }

}
