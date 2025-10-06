package uk.gov.hmcts.ccd.definition.store.elastic.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
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
        GetAliasesResponse aliasResponse = elasticClient.getAlias(baseIndexName);
        String indexName = aliasResponse.getAliases().keySet().iterator().next();
        String newIndexName = incrementIndexNumber(indexName);
        boolean reindexStarted = false;

        //prepare for db
        ReindexEntity reindexEntity = saveEntity(event.isReindex(),
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
                updateEntity(newIndexName, e);
            }
            throw new ElasticSearchInitialisationException(e);
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
                    updateEntity(newIndexName, bulkByScrollResponse.toString());
                    log.info("saved reindex entity"
                             + " metadata for case type {} to DB", baseIndexName);
                } catch (IOException e) {
                    log.error("failed to clean up after reindexing success", e);
                    updateEntity(newIndexName, e);
                    throw new CompletionException(e);
                }
            }

            @Override
            public void onFailure(Exception ex) {
                try (elasticClient; HighLevelCCDElasticClient highLevelCCDElasticClient = clientFactory.getObject()) {
                    //set failure status, end time and ex for db
                    updateEntity(newIndexName, ex);

                    //if failed delete new index, set old index writable
                    log.debug("reindexing failed", ex);
                    highLevelCCDElasticClient.removeIndex(newIndexName);
                    log.debug("{} deleted", newIndexName);
                    highLevelCCDElasticClient.setIndexReadOnly(oldIndexName, false);
                    log.debug("{} set to writable", oldIndexName);
                } catch (IOException e) {
                    log.error("failed to clean up after reindexing failure", e);
                    updateEntity(newIndexName, e);
                    throw new CompletionException(e);
                }
                throw new CompletionException(ex);
            }
        });
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

    public ReindexEntity saveEntity(Boolean reindex, Boolean deleteOldIndex, CaseTypeEntity caseType,
                                    String newIndexName) {
        ReindexEntity entity = new ReindexEntity();
        entity.setReindex(reindex);
        entity.setDeleteOldIndex(deleteOldIndex);
        entity.setCaseType(caseType.getReference());
        entity.setJurisdiction(caseType.getJurisdiction().getReference());
        entity.setIndexName(newIndexName);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus("STARTED");
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
        reindexEntity.setStatus("SUCCESS");
        reindexEntity.setEndTime(LocalDateTime.now());
        reindexEntity.setReindexResponse(response);
        reindexRepository.save(reindexEntity);
    }

    public void updateEntity(String newIndexName, Exception ex) {
        ReindexEntity reindexEntity = reindexRepository.findByIndexName(newIndexName).orElse(null);
        if (reindexEntity == null) {
            String message = String.format("No reindex entity metadata found for index name: %s", newIndexName);
            log.error(message);
            return;
        }
        log.info("Persisting FAILED status for index '{}'", newIndexName);
        reindexEntity.setStatus("FAILED");
        reindexEntity.setEndTime(LocalDateTime.now());
        Throwable rootCause = unwrapCompletionException(ex);
        reindexEntity.setExceptionMessage(rootCause.getClass().getName() + ": " + rootCause.getMessage());
        reindexRepository.save(reindexEntity);
    }

    private Throwable unwrapCompletionException(Throwable exc) {
        if (exc instanceof CompletionException && exc.getCause() != null) {
            return exc.getCause();
        }
        return exc;
    }
}
