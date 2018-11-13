package uk.gov.hmcts.ccd.definition.store.elastic;

import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Slf4j
public abstract class ElasticDefinitionImportListener {

    private static final String FIRST_INDEX_SUFFIX = "-000001";

    private CcdElasticSearchProperties config;

    private CaseMappingGenerator mappingGenerator;

    private ObjectFactory<HighLevelCCDElasticClient> clientFactory;

    public ElasticDefinitionImportListener(CcdElasticSearchProperties config, CaseMappingGenerator mappingGenerator,
                                           ObjectFactory<HighLevelCCDElasticClient> clientFactory) {
        this.config = config;
        this.mappingGenerator = mappingGenerator;
        this.clientFactory = clientFactory;
    }

    public abstract void onDefinitionImported(DefinitionImportedEvent event) throws IOException;

    /**
     * NOTE: imports happens seldom. To prevent unused connections to the ES cluster hanging around, we create a new HighLevelCCDElasticClient on each import
     * and we close it once the import is completed. The HighLevelCCDElasticClient is injected every time with a new ES client which opens new connections
     */
    protected void initialiseElasticSearch(List<CaseTypeEntity> caseTypes) {
        HighLevelCCDElasticClient elasticClient = null;
        String caseMapping = null;
        try {
            elasticClient = clientFactory.getObject();
            for (CaseTypeEntity caseType : caseTypes) {
                String baseIndexName = baseIndexName(caseType);

                if (!elasticClient.aliasExists(baseIndexName)) {
                    String actualIndexName = baseIndexName + FIRST_INDEX_SUFFIX;
                    String alias = baseIndexName;
                    elasticClient.createIndex(actualIndexName, alias);
                }

                caseMapping = mappingGenerator.generateMapping(caseType);
                elasticClient.upsertMapping(baseIndexName, caseMapping);
            }
        } catch (Exception exc) {
            if (caseMapping != null) {
                log.error("elastic search initialisation error on import. Case mapping: {}", caseMapping);
            }
            throw new ElasticSearchInitialisationException(exc);
        } finally {
            if (elasticClient != null) {
                elasticClient.close();
            }
        }
    }

    private String baseIndexName(CaseTypeEntity caseType) {
        String caseTypeId = caseType.getReference();
        return String.format(config.getCasesIndexNameFormat(), caseTypeId.toLowerCase());
    }
}
