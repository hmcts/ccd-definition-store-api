package uk.gov.hmcts.ccd.definition.store;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @TransactionalEventListener
    void onDefinitionImported(DefinitionImportedEvent event) {

        List<CaseTypeEntity> caseTypes = event.getCaseTypes();

        log.info("notified of imported definition");
        elasticIndexesCreator.createIndexes(caseTypes);
    }

}
