package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.elastic.client.CCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;

@Service
@ConditionalOnExpression("'${elasticsearch.enabled}'=='true' && '${elasticsearch.failImportIfError}'=='true'")
@Slf4j
public class SynchronousElasticDefinitionImportListener extends ElasticDefinitionImportListener {

    @Autowired
    public SynchronousElasticDefinitionImportListener(CcdElasticSearchProperties config,
        CaseMappingGenerator mappingGenerator,
        CCDElasticClient elasticClient) {
        super(config, mappingGenerator, elasticClient);
    }

    @EventListener
    public void onDefinitionImported(DefinitionImportedEvent event) {
        log.info("Errors initialising ElasticSearch will fail the definition import");
        super.initialiseElasticSearch(event.getContent());
    }
}
