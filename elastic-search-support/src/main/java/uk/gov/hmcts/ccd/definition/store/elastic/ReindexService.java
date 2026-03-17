package uk.gov.hmcts.ccd.definition.store.elastic;

import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReindexService {

    private static final Logger log = LoggerFactory.getLogger(ReindexService.class);
    private final CaseMappingGenerator mappingGenerator;

    private final ObjectFactory<HighLevelCCDElasticClient> clientFactory;

    public ReindexService(CaseMappingGenerator mappingGenerator,
                          ObjectFactory<HighLevelCCDElasticClient> clientFactory) {
        this.mappingGenerator = mappingGenerator;
        this.clientFactory = clientFactory;
    }

    @Async("reindexExecutor")
    public void asyncReindex(DefinitionImportedEvent event,
                             String baseIndexName,
                             CaseTypeEntity caseType) throws IOException {
        HighLevelCCDElasticClient elasticClient = clientFactory.getObject();
        GetAliasResponse aliasResponse = elasticClient.getAlias(baseIndexName);
        String indexName = aliasResponse.aliases().keySet().iterator().next();
        String newIndexName = incrementIndexNumber(indexName);

        long sourceIndexDocumentCount = elasticClient.countDocuments(indexName);
        log.info("Begin reindexing. Source index '{}' contains {} cases/documents",
            indexName, sourceIndexDocumentCount);

        //create new index with generated mapping and incremented case type name (no alias update yet)
        String caseMapping = mappingGenerator.generateMapping(caseType);
        log.info("case mapping: {}", caseMapping);
        log.info("Reindex process for case type {} is started, Time {}",
            newIndexName, System.currentTimeMillis());
        try {
            elasticClient.createIndexAndMapping(newIndexName, caseMapping);
            elasticClient.setIndexReadOnly(indexName, true);
        } catch (Exception e) {
            onFailure(e, elasticClient, newIndexName, indexName);
            throw e;
        }

        //initiate reindexing
        String taskId = handleReindexing(elasticClient,
            baseIndexName,
            indexName,
            newIndexName,
            event.isDeleteOldIndex());
        event.setTaskId(taskId);
        log.info("task id returned from the import: {}", event.getTaskId());
    }

    private String handleReindexing(HighLevelCCDElasticClient elasticClient,
                                    String baseIndexName,
                                    String oldIndex,
                                    String newIndex,
                                    boolean deleteOldIndex) {

        return elasticClient.reindexData(oldIndex, newIndex, new ReindexListener() {
            @Override
            public void onSuccess() {
                try (elasticClient; HighLevelCCDElasticClient highLevelCCDElasticClient = clientFactory.getObject()) {
                    //if success set writable and update alias to new index
                    ReindexService.onSuccess(highLevelCCDElasticClient,
                        oldIndex,
                        newIndex,
                        baseIndexName,
                        elasticClient,
                        deleteOldIndex);
                } catch (IOException e) {
                    throw new CompletionException("Failed cleanup after reindexing failure for index " + newIndex, e);
                }
            }

            @Override
            public void onFailure(Exception ex) {
                try (elasticClient; HighLevelCCDElasticClient highLevelCCDElasticClient = clientFactory.getObject()) {
                    //if failed delete new index, set old index writable
                    ReindexService.onFailure(ex, highLevelCCDElasticClient, newIndex, baseIndexName);
                } catch (IOException e) {
                    throw new CompletionException("Failed cleanup after reindexing failure for index " + newIndex, e);
                }
                throw new CompletionException(ex);
            }
        });
    }

    private static void onFailure(Exception ex,
                                  HighLevelCCDElasticClient highLevelCCDElasticClient,
                                  String newIndex,
                                  String oldIndex) throws IOException {
        log.error("Reindex process for case type {} is failed, Time {}",
            newIndex, System.currentTimeMillis());
        log.error("reindexing failed", ex);
        highLevelCCDElasticClient.removeIndex(newIndex);
        log.info("{} deleted", newIndex);
        highLevelCCDElasticClient.setIndexReadOnly(oldIndex, false);
        log.info("{} set to writable", oldIndex);
    }

    private static void onSuccess(HighLevelCCDElasticClient highLevelCCDElasticClient,
                                  String oldIndex,
                                  String newIndex,
                                  String baseIndexName,
                                  HighLevelCCDElasticClient elasticClient,
                                  boolean deleteOldIndex) throws IOException {
        log.info("updating alias from {} to {}", oldIndex, newIndex);
        highLevelCCDElasticClient.setIndexReadOnly(oldIndex, false);
        highLevelCCDElasticClient.updateAlias(baseIndexName, oldIndex, newIndex);
        // After reindexAsync completes:
        highLevelCCDElasticClient.refresh(newIndex);
        long targetIndexDocumentCount = elasticClient.countDocuments(newIndex);
        log.info("Successfully completed reindexing. New index '{}' contains {} cases/documents",
            newIndex, targetIndexDocumentCount);
        if (deleteOldIndex) {
            log.info("deleting old index {}", oldIndex);
            highLevelCCDElasticClient.removeIndex(oldIndex);
        }
        log.info("Reindex process for case type {} completed", newIndex);
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
        return prefix + formattedNumber;
    }

}
