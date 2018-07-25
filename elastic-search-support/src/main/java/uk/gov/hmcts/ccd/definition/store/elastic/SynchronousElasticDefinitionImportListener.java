package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;

@Service
@ConditionalOnExpression("'${elasticsearch.enabled}'=='true' && '${elasticsearch.failImportIfError}'=='true'")
@Slf4j
public class SynchronousElasticDefinitionImportListener extends ElasticDefinitionImportListener {

    @EventListener
    public void onDefinitionImported(DefinitionImportedEvent event) throws IOException {
        log.info("import listener executing synchronously");
        super.initialiseElasticSearch(event.getCaseTypes());
    }
}
