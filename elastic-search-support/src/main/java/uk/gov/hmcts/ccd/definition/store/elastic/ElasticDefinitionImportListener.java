package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
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

    private static final String FIRST_INDEX_SUFFIX = "-000001";

    private CcdElasticSearchProperties config;

    private CaseMappingGenerator mappingGenerator;

    private CCDElasticClient elasticClient;

    public ElasticDefinitionImportListener(CcdElasticSearchProperties config, CaseMappingGenerator mappingGenerator, CCDElasticClient elasticClient) {
        this.config = config;
        this.mappingGenerator = mappingGenerator;
        this.elasticClient = elasticClient;
    }

    public abstract void onDefinitionImported(DefinitionImportedEvent event) throws IOException;

    protected void initialiseElasticSearch(List<CaseTypeEntity> caseTypes) {
        try {
            for (CaseTypeEntity caseType : caseTypes) {
                String baseIndexName = baseIndexName(caseType);

                if (!elasticClient.aliasExists(baseIndexName)) {
                    String actualIndexName = baseIndexName + FIRST_INDEX_SUFFIX;
                    String alias = baseIndexName;
                    elasticClient.createIndex(actualIndexName, alias);
                }

                String caseMapping = mappingGenerator.generateMapping(caseType);
                elasticClient.upsertMapping(baseIndexName, caseMapping);
            }
        } catch (Exception exc) {
            throw new ElasticSearchInitialisationException(exc);
        }
    }

    private String baseIndexName(CaseTypeEntity caseType) {
        String caseTypeId = caseType.getReference();
        return String.format(config.getCasesIndexNameFormat(), caseTypeId.toLowerCase());
    }
}
