package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.GetAliasesResponse;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.handler.ElasticsearchErrorHandler;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
public abstract class ElasticDefinitionImportListener {

    private static final String FIRST_INDEX_SUFFIX = "-000001";

    private final CcdElasticSearchProperties config;

    private final CaseMappingGenerator mappingGenerator;

    private final ObjectFactory<HighLevelCCDElasticClient> clientFactory;

    private final ElasticsearchErrorHandler elasticsearchErrorHandler;

    public ElasticDefinitionImportListener(CcdElasticSearchProperties config, CaseMappingGenerator mappingGenerator,
                                           ObjectFactory<HighLevelCCDElasticClient> clientFactory,
                                           ElasticsearchErrorHandler elasticsearchErrorHandler) {
        this.config = config;
        this.mappingGenerator = mappingGenerator;
        this.clientFactory = clientFactory;
        this.elasticsearchErrorHandler = elasticsearchErrorHandler;
    }

    public abstract void onDefinitionImported(DefinitionImportedEvent event) throws IOException;

    /**
     * NOTE: imports happens seldom. To prevent unused connections to the ES cluster hanging around, we create a new
     * HighLevelCCDElasticClient on each import, we close it once the import is completed.
     * The HighLevelCCDElasticClient is injected every time with a new ES client which opens new connections
     */
    @Transactional
    public void initialiseElasticSearch(DefinitionImportedEvent event) {
        List<CaseTypeEntity> caseTypes = event.getContent();
        boolean reindex = event.isReindex();
        boolean deleteOldIndex = event.isDeleteOldIndex();

        HighLevelCCDElasticClient elasticClient = null;
        String caseMapping = null;
        CaseTypeEntity currentCaseType = null;
        try {
            elasticClient = clientFactory.getObject();
            for (CaseTypeEntity caseType : caseTypes) {
                currentCaseType = caseType;
                String baseIndexName = baseIndexName(caseType);
                if (!elasticClient.aliasExists(baseIndexName)) {
                    String actualIndexName = baseIndexName + FIRST_INDEX_SUFFIX;
                    elasticClient.createIndex(actualIndexName, baseIndexName);
                }
                if (reindex) {
                    //set readonly
                    elasticClient.setIndexReadOnly(baseIndexName, true);

                    //get current alias index
                    GetAliasesResponse aliasResponse = elasticClient.getAlias(baseIndexName);
                    String caseTypeName = aliasResponse.getAliases().keySet().iterator().next();

                    //create new index and mapping with incremented number
                    String incrementedCaseTypeName = incrementIndexNumber(caseTypeName);
                    caseMapping = mappingGenerator.generateMapping(caseType);
                    log.debug("case mapping: {}", caseMapping);
                    elasticClient.createIndexAndMapping(incrementedCaseTypeName, caseMapping);

                    //initiate reindexing
                    CompletableFuture<String> taskId = handleReindexing(elasticClient, baseIndexName, caseTypeName,
                        incrementedCaseTypeName, deleteOldIndex);
                    event.setTaskId(taskId.get());

                } else {
                    caseMapping = mappingGenerator.generateMapping(caseType);
                    log.debug("case mapping: {}", caseMapping);
                    elasticClient.upsertMapping(baseIndexName, caseMapping);
                }
            }
        } catch (ElasticsearchStatusException exc) {
            logMapping(caseMapping);
            throw elasticsearchErrorHandler.createException(exc, currentCaseType);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logMapping(caseMapping);
        } catch (Exception exc) {
            logMapping(caseMapping);
            throw new ElasticSearchInitialisationException(exc);
        } finally {
            if (elasticClient != null) {
                elasticClient.close();
            }
        }
    }

    private CompletableFuture<String> handleReindexing(HighLevelCCDElasticClient elasticClient, String baseIndexName,
                                                       String caseTypeName, String incrementedCaseTypeName,
                                                       boolean deleteOldIndex) {
        //initiate async elasticsearch reindexing request
        CompletableFuture<String> taskIdFuture = elasticClient.reindexData(caseTypeName, incrementedCaseTypeName);

        taskIdFuture
            .thenApply(taskId -> {
                try {
                    HighLevelCCDElasticClient asyncElasticClient = clientFactory.getObject();
                    //if success set writable and update alias to new index
                    log.info("updating alias from {} to {}", caseTypeName, incrementedCaseTypeName);
                    asyncElasticClient.setIndexReadOnly(baseIndexName, false);
                    asyncElasticClient.updateAlias(baseIndexName, caseTypeName, incrementedCaseTypeName);

                    if (deleteOldIndex) {
                        log.info("deleting old index {}", caseTypeName);
                        asyncElasticClient.removeIndex(caseTypeName);
                    }
                    return taskId;
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            }).exceptionally(ex -> {
                try {
                    //if failed delete new index, set old index writable
                    HighLevelCCDElasticClient asyncElasticClient = clientFactory.getObject();
                    log.info("reindexing failed, error: {}", ex.getMessage());
                    asyncElasticClient.removeIndex(incrementedCaseTypeName);
                    log.info("{} deleted", incrementedCaseTypeName);
                    asyncElasticClient.setIndexReadOnly(caseTypeName, false);
                    log.info("{} set to writable", caseTypeName);
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
                return "Failed";
            });
        return taskIdFuture;
    }

    private String incrementIndexNumber(String indexName) {
        String[] parts = indexName.split("-");
        String prefix = parts[0] + "-";
        String numberStr = parts[1];

        int incremented = Integer.parseInt(numberStr) + 1;
        String formattedNumber = String.format("%0" + numberStr.length() + "d", incremented);

        String incrementedIndexName = prefix + formattedNumber;
        log.info("incremented index name: {}", incrementedIndexName);
        return incrementedIndexName;
    }

    private String baseIndexName(CaseTypeEntity caseType) {
        String caseTypeId = caseType.getReference();
        return String.format(config.getCasesIndexNameFormat(), caseTypeId.toLowerCase());
    }

    private void logMapping(String caseMapping) {
        if (caseMapping != null) {
            log.error("elastic search initialisation error on import. Case mapping: {}", caseMapping);
        }
    }
}
