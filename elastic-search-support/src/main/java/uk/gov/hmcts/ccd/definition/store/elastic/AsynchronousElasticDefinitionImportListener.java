package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.handler.ElasticsearchErrorHandler;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;

import java.util.List;

@Service
@ConditionalOnExpression("'${elasticsearch.enabled}'=='true' && '${elasticsearch.failImportIfError}'=='false'")
@Slf4j
public class AsynchronousElasticDefinitionImportListener extends ElasticDefinitionImportListener {

    private final CaseTypeRepository caseTypeRepository;

    public AsynchronousElasticDefinitionImportListener(CcdElasticSearchProperties config,
                                                       CaseMappingGenerator mappingGenerator,
                                                       ObjectFactory<HighLevelCCDElasticClient> clientFactory,
                                                       ElasticsearchErrorHandler elasticsearchErrorHandler,
                                                       ReindexService reindexService,
                                                       CaseTypeRepository caseTypeRepository) {
        super(config, mappingGenerator, clientFactory, elasticsearchErrorHandler, reindexService);
        this.caseTypeRepository = caseTypeRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void onDefinitionImported(DefinitionImportedEvent event) {
        log.warn("Errors initialising ElasticSearch will not fail the definition import");
        List<String> caseTypeReferences = event.getContent().stream()
            .map(caseType -> caseType.getReference())
            .toList();
        DefinitionImportedEvent attachedEvent = new DefinitionImportedEvent(
            caseTypeRepository.findAllLatestVersions(caseTypeReferences),
            event.isReindex(),
            event.isDeleteOldIndex(),
            event.getUserEmailId()
        );
        attachedEvent.setTaskId(event.getTaskId());
        super.initialiseElasticSearch(attachedEvent);
    }
}
