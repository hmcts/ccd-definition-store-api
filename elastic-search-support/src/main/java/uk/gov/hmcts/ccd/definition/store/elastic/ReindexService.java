package uk.gov.hmcts.ccd.definition.store.elastic;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.GetAliasesResponse;
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
        GetAliasesResponse aliasResponse = elasticClient.getAlias(baseIndexName);
        String caseTypeName = aliasResponse.getAliases().keySet().iterator().next();

        //create new index with generated mapping and incremented case type name (no alias update yet)
        String caseMapping = mappingGenerator.generateMapping(caseType);
        log.info("case mapping: {}", caseMapping);
        String incrementedCaseTypeName = incrementIndexNumber(caseTypeName);
        log.info("Reindex process for case type {} is started, Time {}",
            incrementedCaseTypeName, System.currentTimeMillis());
        elasticClient.setIndexReadOnly(baseIndexName, true);
        elasticClient.createIndexAndMapping(incrementedCaseTypeName, caseMapping);

        //initiate reindexing
        String taskId = handleReindexing(elasticClient,
                baseIndexName,
                caseTypeName,
                incrementedCaseTypeName,
                event.isDeleteOldIndex());
        event.setTaskId(taskId);
        log.info("reindexing successful for case type: {}", caseType.getReference());
        log.info("task id returned from the import: {}", event.getTaskId());
    }

    private String handleReindexing(HighLevelCCDElasticClient elasticClient, String baseIndexName,
                                  String oldIndex, String newIndex,
                                  boolean deleteOldIndex) {

        return elasticClient.reindexData(oldIndex, newIndex, new ReindexListener() {
            @Override
            public void onSuccess() {
                try (elasticClient; HighLevelCCDElasticClient highLevelCCDElasticClient = clientFactory.getObject()) {
                    //if success set writable and update alias to new index
                    log.info("updating alias from {} to {}", oldIndex, newIndex);
                    highLevelCCDElasticClient.setIndexReadOnly(baseIndexName, false);
                    highLevelCCDElasticClient.updateAlias(baseIndexName, oldIndex, newIndex);
                    // After reindexAsync completes:
                    highLevelCCDElasticClient.refresh(newIndex);
                    if (deleteOldIndex) {
                        log.info("deleting old index {}", oldIndex);
                        highLevelCCDElasticClient.removeIndex(oldIndex);
                    }
                    log.info("Reindex process for case type {} completed", newIndex);
                } catch (IOException e) {
                    log.error("Cleanup failed after reindexing error", e);
                    throw new CompletionException("Failed cleanup after reindexing failure for index " + newIndex, e);
                }
            }

            @Override
            public void onFailure(Exception ex) {
                try (elasticClient; HighLevelCCDElasticClient highLevelCCDElasticClient = clientFactory.getObject()) {
                    //if failed delete new index, set old index writable
                    log.error("Reindex process for case type {} is failed, Time {}",
                        newIndex, System.currentTimeMillis());
                    log.error("reindexing failed", ex);
                    highLevelCCDElasticClient.removeIndex(newIndex);
                    log.info("{} deleted", newIndex);
                    highLevelCCDElasticClient.setIndexReadOnly(oldIndex, false);
                    log.info("{} set to writable", oldIndex);
                } catch (IOException e) {
                    log.error("Cleanup failed after reindexing error", e);
                    throw new CompletionException("Failed cleanup after reindexing failure for index " + newIndex, e);
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
        return prefix + formattedNumber;
    }

}
