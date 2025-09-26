package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public abstract class ReindexService {

    private final CaseMappingGenerator mappingGenerator;

    public ReindexService(CaseMappingGenerator mappingGenerator) {
        this.mappingGenerator = mappingGenerator;
    }

    @Lookup
    protected abstract HighLevelCCDElasticClient getElasticClient();

    @Async("reindexExecutor")
    public void asyncReindex(DefinitionImportedEvent event,
                              String baseIndexName,
                              CaseTypeEntity caseType) throws IOException {
        HighLevelCCDElasticClient elasticClient = getElasticClient();
        GetAliasesResponse aliasResponse = elasticClient.getAlias(baseIndexName);
        String caseTypeName = aliasResponse.getAliases().keySet().iterator().next();

        //create new index with generated mapping and incremented case type name (no alias update yet)
        String caseMapping = mappingGenerator.generateMapping(caseType);
        log.debug("case mapping: {}", caseMapping);
        String incrementedCaseTypeName = incrementIndexNumber(caseTypeName);
        elasticClient.setIndexReadOnly(baseIndexName, true);
        elasticClient.createIndexAndMapping(incrementedCaseTypeName, caseMapping);

        //initiate reindexing
        handleReindexing(elasticClient, baseIndexName, caseTypeName, incrementedCaseTypeName,
            event.isDeleteOldIndex());
        //dummy value for phase 1
        event.setTaskId("taskID");
        log.info("reindexing successful for case type: {}", caseType.getReference());
        log.info("task id returned from the import: {}", event.getTaskId());
    }

    private void handleReindexing(HighLevelCCDElasticClient elasticClient, String baseIndexName,
                                  String oldIndex, String newIndex,
                                  boolean deleteOldIndex) {
        elasticClient.reindexData(oldIndex, newIndex, new ActionListener<>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                HighLevelCCDElasticClient highLevelCCDElasticClient = getElasticClient();
                try {
                    //if success set writable and update alias to new index
                    log.info("updating alias from {} to {}", oldIndex, newIndex);
                    highLevelCCDElasticClient.setIndexReadOnly(baseIndexName, false);
                    highLevelCCDElasticClient.updateAlias(baseIndexName, oldIndex, newIndex);
                    if (deleteOldIndex) {
                        log.info("deleting old index {}", oldIndex);
                        highLevelCCDElasticClient.removeIndex(oldIndex);
                    }
                } catch (IOException e) {
                    log.error("failed to clean up after reindexing success", e);
                    throw new CompletionException(e);
                } finally {
                    highLevelCCDElasticClient.close();
                    elasticClient.close();
                }
            }

            @Override
            public void onFailure(Exception ex) {
                HighLevelCCDElasticClient highLevelCCDElasticClient = getElasticClient();
                try {
                    //if failed delete new index, set old index writable
                    log.error("reindexing failed", ex);
                    highLevelCCDElasticClient.removeIndex(newIndex);
                    log.info("{} deleted", newIndex);
                    highLevelCCDElasticClient.setIndexReadOnly(oldIndex, false);
                    log.info("{} set to writable", oldIndex);
                } catch (IOException e) {
                    log.error("failed to clean up after reindexing failure", e);
                    throw new CompletionException(e);
                } finally {
                    highLevelCCDElasticClient.close();
                    elasticClient.close();
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
