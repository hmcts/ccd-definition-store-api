package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.handler.ElasticsearchErrorHandler;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class ElasticDefinitionImportListener {

    private static final String FIRST_INDEX_SUFFIX = "-000001";

    private final CcdElasticSearchProperties config;

    private final CaseMappingGenerator mappingGenerator;

    private final ObjectFactory<HighLevelCCDElasticClient> clientFactory;

    private final ElasticsearchErrorHandler elasticsearchErrorHandler;

    private final ReindexRepository reindexRepository;

    public ElasticDefinitionImportListener(CcdElasticSearchProperties config, CaseMappingGenerator mappingGenerator,
                                           ObjectFactory<HighLevelCCDElasticClient> clientFactory,
                                           ElasticsearchErrorHandler elasticsearchErrorHandler,
                                           ReindexRepository reindexRepository) {
        this.config = config;
        this.mappingGenerator = mappingGenerator;
        this.clientFactory = clientFactory;
        this.elasticsearchErrorHandler = elasticsearchErrorHandler;
        this.reindexRepository = reindexRepository;
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
        boolean reindex = true;
        boolean deleteOldIndex = true;

        String caseMapping = null;
        CaseTypeEntity currentCaseType = null;

        try (HighLevelCCDElasticClient elasticClient = clientFactory.getObject()) {
            for (CaseTypeEntity caseType : caseTypes) {
                currentCaseType = caseType;
                String baseIndexName = baseIndexName(caseType);
                //if alias doesn't exist create index and alias
                if (!elasticClient.aliasExists(baseIndexName)) {
                    String actualIndexName = baseIndexName + FIRST_INDEX_SUFFIX;
                    elasticClient.createIndex(actualIndexName, baseIndexName);
                }
                //get current alias index
                GetAliasesResponse aliasResponse = elasticClient.getAlias(baseIndexName);
                String caseTypeName = aliasResponse.getAliases().keySet().iterator().next();

                //prepare for db
                ReindexEntity metadata = new ReindexEntity();
                metadata.setReindex(reindex);
                metadata.setDeleteOldIndex(deleteOldIndex);
                metadata.setIndexName(caseTypeName);
                metadata.setStartTime(LocalDateTime.now());
                metadata.setStatus("STARTED");
                metadata.setJurisdiction(caseType.getJurisdiction().getReference());
                metadata.setCaseType(currentCaseType.getName());
                metadata = reindexRepository.save(metadata);
                if (metadata == null) {
                    throw new ElasticSearchInitialisationException(new IllegalStateException("Failed to persist reindex metadata"));
                }

                if (reindex) {
                    //create new index with generated mapping and incremented case type name (no alias update yet)
                    caseMapping = mappingGenerator.generateMapping(caseType);
                    log.debug("case mapping: {}", caseMapping);
                    String incrementedCaseTypeName = incrementIndexNumber(caseTypeName);
                    //update index name for db
                    metadata.setIndexName(incrementedCaseTypeName);
                    elasticClient.setIndexReadOnly(baseIndexName, true);
                    elasticClient.createIndexAndMapping(incrementedCaseTypeName, caseMapping);

                    //initiate reindexing
                    handleReindexing(baseIndexName, caseTypeName, incrementedCaseTypeName,
                        deleteOldIndex, metadata);
                    //dummy value for phase 1
                    event.setTaskId("taskID");
                    log.info("reindexing successful for case type: {}", caseType.getReference());
                    log.info("task id returned from the import: {}", event.getTaskId());


                } else {
                    caseMapping = mappingGenerator.generateMapping(caseType);
                    log.debug("case mapping: {}", caseMapping);
                    elasticClient.upsertMapping(baseIndexName, caseMapping);
                }
            }
        } catch (ElasticsearchStatusException exc) {
            logMapping(caseMapping);
            throw elasticsearchErrorHandler.createException(exc, currentCaseType);
        } catch (Exception exc) {
            logMapping(caseMapping);
            throw new ElasticSearchInitialisationException(exc);
        }
    }

    private void handleReindexing(String baseIndexName,
                                  String oldIndex, String newIndex,
                                  boolean deleteOldIndex, ReindexEntity metadata) {
        HighLevelCCDElasticClient elasticClient = clientFactory.getObject();
        elasticClient.reindexData(oldIndex, newIndex, new ActionListener<>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                try (elasticClient; HighLevelCCDElasticClient asyncElasticClient = clientFactory.getObject()) {
                    //if success set writable and update alias to new index
                    log.info("updating alias from {} to {}", oldIndex, newIndex);
                    asyncElasticClient.setIndexReadOnly(baseIndexName, false);
                    asyncElasticClient.updateAlias(baseIndexName, oldIndex, newIndex);
                    if (deleteOldIndex) {
                        log.info("deleting old index {}", oldIndex);
                        asyncElasticClient.removeIndex(oldIndex);
                    }

                    //for db
                    metadata.setStatus("SUCCESS");
                    metadata.setEndTime(LocalDateTime.now());
                    reindexRepository.save(metadata);

                    log.info("Saved reindex metadata for case type {}", baseIndexName);
                } catch (IOException e) {
                    log.error("failed to clean up after reindexing success", e);
                    throw new CompletionException(e);
                }
            }

            @Override
            public void onFailure(Exception ex) {
                try (elasticClient; HighLevelCCDElasticClient asyncElasticClient = clientFactory.getObject()) {
                    //for db
                    metadata.setStatus("FAILED");
                    metadata.setMessage(ex.getMessage());
                    metadata.setEndTime(LocalDateTime.now());
                    reindexRepository.save(metadata);

                    //if failed delete new index, set old index writable
                    log.error("reindexing failed", ex);
                    asyncElasticClient.removeIndex(newIndex);
                    log.info("{} deleted", newIndex);
                    asyncElasticClient.setIndexReadOnly(oldIndex, false);
                    log.info("{} set to writable", oldIndex);
                } catch (IOException e) {
                    log.error("failed to clean up after reindexing failure", e);
                    throw new CompletionException(e);
                }
                throw new CompletionException(ex);
            }
        });
    }

    String incrementIndexNumber(String indexName) {
        Pattern pattern = Pattern.compile("(.+_cases-)(\\d+)$");
        Matcher matcher = pattern.matcher(indexName);

        if (!matcher.matches() || matcher.groupCount() < 2) {
            throw new IllegalArgumentException("invalid index name format: " + indexName);
        }

        String prefix = matcher.group(1);
        String numberStr = matcher.group(2);

        int incremented = Integer.parseInt(numberStr) + 1;
        String formattedNumber = StringUtils.leftPad(String.valueOf(incremented), numberStr.length(), '0');

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
