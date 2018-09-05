package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.elastic.client.CCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.io.IOException;
import java.util.List;

@Slf4j
public abstract class ElasticDefinitionImportListener {

    @Autowired
    private CcdElasticSearchProperties config;

    @Autowired
    private CaseMappingGenerator mappingGenerator;

    @Autowired
    private CCDElasticClient elasticClient;

    public abstract void onDefinitionImported(DefinitionImportedEvent event) throws IOException;

    protected void initialiseElasticSearch(List<CaseTypeEntity> caseTypes) {
        try {
            for (CaseTypeEntity caseType : caseTypes) {
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
        } catch (Exception exc) {
            throw new ElasticSearchInitialisationException(exc);
        }
    }

    private String indexName(CaseTypeEntity caseType) {
        String caseTypeId = caseType.getReference();
        return String.format(config.getCasesIndexNameFormat(), caseTypeId.toLowerCase());
    }
}
