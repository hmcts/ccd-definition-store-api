package uk.gov.hmcts.ccd.definition.store.elastic.service;

import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexStatus;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexTask;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ReindexServiceImpl implements ReindexService {

    private final ReindexRepository reindexRepository;
    private final EntityToResponseDTOMapper mapper;
    private final CaseMappingGenerator mappingGenerator;
    private final ObjectFactory<HighLevelCCDElasticClient> clientFactory;

    @Autowired
    public ReindexServiceImpl(ReindexRepository reindexRepository, EntityToResponseDTOMapper mapper,
                              CaseMappingGenerator mappingGenerator,
                              ObjectFactory<HighLevelCCDElasticClient> clientFactory) {
        this.reindexRepository = reindexRepository;
        this.mapper = mapper;
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
        saveEntity(event.isDeleteOldIndex(), caseType, newIndexName);
        try {
            //create new index with generated mapping and incremented case type name (no alias update yet)
            String caseMapping = mappingGenerator.generateMapping(caseType);
            log.info("case mapping: {}", caseMapping);
            log.info("Reindex process for case type {} is started, Time {}",
                newIndexName, System.currentTimeMillis());

            elasticClient.createIndexAndMapping(newIndexName, caseMapping);
            elasticClient.setIndexReadOnly(indexName, true);
        } catch (Exception e) {
            updateEntity(newIndexName, e);
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
            public void onSuccess(String response) {
                try (elasticClient; HighLevelCCDElasticClient highLevelCCDElasticClient = clientFactory.getObject()) {
                    //if success set writable and update alias to new index
                    ReindexServiceImpl.onSuccess(highLevelCCDElasticClient,
                        oldIndex,
                        newIndex,
                        baseIndexName,
                        elasticClient,
                        deleteOldIndex);

                    //set success status and end time for db
                    updateEntity(newIndex, response);
                    log.info("saved reindex entity"
                        + " metadata for case type {} to DB", baseIndexName);

                    log.info("Reindex process for case type {} completed", newIndex);
                } catch (IOException e) {
                    throw new CompletionException("Failed cleanup after reindexing failure for index " + newIndex, e);
                }
            }

            @Override
            public void onFailure(Exception ex) {
                try (elasticClient; HighLevelCCDElasticClient highLevelCCDElasticClient = clientFactory.getObject()) {
                    updateEntity(newIndex, ex);
                    //if failed delete new index, set old index writable
                    ReindexServiceImpl.onFailure(ex, highLevelCCDElasticClient, newIndex, baseIndexName);
                } catch (IOException e) {
                    log.error("Failed cleanup after reindexing failure for index " + newIndex, e);
                    updateEntity(newIndex, e);
                    throw new CompletionException(e);
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
    }

    public String incrementIndexNumber(String indexName) {
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

    @Override
    public List<ReindexTask> getAll() {
        return reindexRepository.findAll()
            .stream()
            .map(mapper::map)
            .toList();
    }

    @Override
    public List<ReindexTask> getTasksByCaseType(String caseType) {
        if (StringUtils.isBlank(caseType)) {
            return getAll();
        }
        return reindexRepository.findByCaseType(caseType)
            .stream()
            .map(mapper::map)
            .toList();
    }

    public ReindexEntity saveEntity(Boolean deleteOldIndex, CaseTypeEntity caseType,
                                    String newIndexName) {
        ReindexEntity entity = new ReindexEntity();
        entity.setDeleteOldIndex(deleteOldIndex);
        entity.setCaseType(caseType.getReference());
        entity.setJurisdiction(caseType.getJurisdiction().getReference());
        entity.setIndexName(newIndexName);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus(ReindexStatus.STARTED.name());
        return reindexRepository.saveAndFlush(entity);
    }

    public void updateEntity(String newIndexName, String response) {
        ReindexEntity reindexEntity = reindexRepository.findByIndexName(newIndexName).orElse(null);
        if (reindexEntity == null) {
            String message = String.format("No reindex entity metadata found for index name: %s", newIndexName);
            log.error(message);
            throw new IllegalStateException(message);
        }
        log.info("Save to DB successful for case type: {}", newIndexName);
        reindexEntity.setStatus(ReindexStatus.SUCCESS.name());
        reindexEntity.setEndTime(LocalDateTime.now());
        reindexEntity.setReindexResponse(response);
        reindexRepository.saveAndFlush(reindexEntity);
    }

    public void updateEntity(String newIndexName, Exception ex) {
        ReindexEntity reindexEntity = reindexRepository.findByIndexName(newIndexName).orElse(null);
        if (reindexEntity == null) {
            String message = String.format("No reindex entity metadata found for index name: %s", newIndexName);
            log.error(message);
            return;
        }
        log.info("Persisting FAILED status for index '{}'", newIndexName);
        reindexEntity.setStatus(ReindexStatus.FAILED.name());
        reindexEntity.setEndTime(LocalDateTime.now());
        Throwable rootCause = unwrapCompletionException(ex);
        reindexEntity.setExceptionMessage(rootCause.getClass().getName() + ": " + rootCause.getMessage());
        reindexRepository.saveAndFlush(reindexEntity);
    }

    private Throwable unwrapCompletionException(Throwable exc) {
        if (exc instanceof CompletionException && exc.getCause() != null) {
            return exc.getCause();
        }
        return exc;
    }
}
