package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;

@Service
@ConditionalOnExpression("'${elasticsearch.enabled}'=='true' && '${elasticsearch.failImportIfError}'=='false'")
@Slf4j
public class AsynchronousElasticDefinitionImportListener extends ElasticDefinitionImportListener {

    @Async
    @TransactionalEventListener
    @EventListener
    public void onDefinitionImported(DefinitionImportedEvent event) {
        log.warn("Errors initialising ElasticSearch will not fail the definition import");
        super.initialiseElasticSearch(event.getContent());
    }
}
