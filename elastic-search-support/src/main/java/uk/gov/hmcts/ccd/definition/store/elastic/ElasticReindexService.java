package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexDBService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ElasticReindexService {

    private final CaseMappingGenerator mappingGenerator;

    private final ObjectFactory<HighLevelCCDElasticClient> clientFactory;

    private final ReindexDBService reindexDBService;

    public ElasticReindexService(CaseMappingGenerator mappingGenerator,
                                 ObjectFactory<HighLevelCCDElasticClient> clientFactory,
                                 ReindexDBService reindexDBService) {
        this.mappingGenerator = mappingGenerator;
        this.clientFactory = clientFactory;
        this.reindexDBService = reindexDBService;
    }

    @Async("reindexExecutor")
    public void asyncReindex(DefinitionImportedEvent event,
                             String baseIndexName,
                             CaseTypeEntity caseType) throws IOException {
        HighLevelCCDElasticClient elasticClient = clientFactory.getObject();
        GetAliasesResponse aliasResponse = elasticClient.getAlias(baseIndexName);
        String indexName = aliasResponse.getAliases().keySet().iterator().next();
        String newIndexName = incrementIndexNumber(indexName);
        boolean reindexStarted = false;

        //prepare for db
        ReindexEntity reindexEntity = reindexDBService.saveEntity(event.isReindex(),
            event.isDeleteOldIndex(), caseType, newIndexName);

        try {
            //create new index with generated mapping and incremented case type name (no alias update yet)
            String caseMapping = mappingGenerator.generateMapping(caseType);
            log.debug("case mapping: {}", caseMapping);
            elasticClient.setIndexReadOnly(baseIndexName, true);
            elasticClient.createIndexAndMapping(newIndexName, caseMapping);

            //initiate reindexing
            reindexStarted = true;
            handleReindexing(elasticClient, baseIndexName, indexName, newIndexName, event.isDeleteOldIndex());
            //dummy value for phase 1
            event.setTaskId("taskID");
            log.debug("reindexing successful for case type: {}", caseType.getReference());
            log.debug("task id returned from the import: {}", event.getTaskId());
        } catch (Exception e) {
            if (!reindexStarted) {
                reindexDBService.updateEntity(newIndexName, e);
            }
            throw new CompletionException(e);
        }
    }

    private void handleReindexing(HighLevelCCDElasticClient elasticClient, String baseIndexName,
                                  String oldIndexName, String newIndexName,
                                  boolean deleteOldIndex) {
        elasticClient.reindexData(oldIndexName, newIndexName, new ActionListener<>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                try (elasticClient; HighLevelCCDElasticClient highLevelCCDElasticClient = clientFactory.getObject()) {
                    //if success set writable and update alias to new index
                    log.debug("updating alias from {} to {}", oldIndexName, newIndexName);
                    highLevelCCDElasticClient.setIndexReadOnly(baseIndexName, false);
                    highLevelCCDElasticClient.updateAlias(baseIndexName, oldIndexName, newIndexName);
                    if (deleteOldIndex) {
                        log.debug("deleting old index {}", oldIndexName);
                        highLevelCCDElasticClient.removeIndex(oldIndexName);
                    }
                    //set success status and end time for db
                    reindexDBService.updateEntity(newIndexName, bulkByScrollResponse.toString());
                    log.info("saved reindex entity"
                             + " metadata for case type {} to DB", baseIndexName);
                } catch (IOException e) {
                    log.error("failed to clean up after reindexing success", e);
                    reindexDBService.updateEntity(newIndexName, e);
                    throw new CompletionException(e);
                }
            }

            @Override
            public void onFailure(Exception ex) {
                try (elasticClient; HighLevelCCDElasticClient highLevelCCDElasticClient = clientFactory.getObject()) {
                    //set failure status, end time and ex for db
                    reindexDBService.updateEntity(newIndexName, ex);

                    //if failed delete new index, set old index writable
                    log.debug("reindexing failed", ex);
                    highLevelCCDElasticClient.removeIndex(newIndexName);
                    log.debug("{} deleted", newIndexName);
                    highLevelCCDElasticClient.setIndexReadOnly(oldIndexName, false);
                    log.debug("{} set to writable", oldIndexName);
                } catch (IOException e) {
                    log.error("failed to clean up after reindexing failure", e);
                    reindexDBService.updateEntity(newIndexName, e);
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
        return incrementedIndexName;
    }

}