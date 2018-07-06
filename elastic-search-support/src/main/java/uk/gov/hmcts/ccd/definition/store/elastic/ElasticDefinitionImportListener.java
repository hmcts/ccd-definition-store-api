package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.gov.hmcts.ccd.definition.store.elastic.client.CCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
@Slf4j
public class ElasticDefinitionImportListener {

    @Autowired
    CcdElasticSearchProperties config;

    @Autowired
    private CaseMappingGenerator mappingGenerator;

    @Autowired
    private CCDElasticClient elasticClient;

    @Async
    @TransactionalEventListener
    public void onDefinitionImported(DefinitionImportedEvent event) throws IOException {
        for (CaseTypeEntity caseType : event.getCaseTypes()) {
            String indexName = indexName(caseType);

            if (!elasticClient.indexExists(indexName)) {
                log.info("creating index {} for case type {}", indexName, caseType.getReference());
                boolean acknowledged = elasticClient.createIndex(indexName);
                log.info("index created: {}", acknowledged);
            }

            String caseMapping = mappingGenerator.generateMapping(caseType);
            boolean acknowledged = elasticClient.upsertMapping(indexName, caseMapping);
            log.info("mapping created: {}", acknowledged);
        }
    }

    private String indexName(CaseTypeEntity caseType) {
        String jurisdiction = caseType.getJurisdiction().getReference();
        String caseTypeId = caseType.getReference();
        return String.format(config.getIndexCasesNameFormat(), jurisdiction.toLowerCase(), caseTypeId.toLowerCase());
    }
}
