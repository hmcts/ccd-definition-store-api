package uk.gov.hmcts.ccd.definition.store.elastic.service;

import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexTask;

import java.io.IOException;
import java.util.List;

public interface ReindexService {
    void asyncReindex(DefinitionImportedEvent event, String baseIndexName, CaseTypeEntity caseType) throws IOException;

    String incrementIndexNumber(String indexName);

    List<ReindexTask> getAll();

    List<ReindexTask> getTasksByCaseType(String caseType);

    ReindexEntity saveEntity(Boolean reindex, Boolean deleteOldIndex, CaseTypeEntity caseType,
                             String newIndexName);

    void updateEntity(String newIndexName, String response);

    void updateEntity(String newIndexName, Exception exception);
}
