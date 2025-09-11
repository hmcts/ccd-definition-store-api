package uk.gov.hmcts.ccd.definition.store.elastic.service;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexTask;

import java.util.List;

public interface ReindexTaskService {
    List<ReindexTask> getAll();

    List<ReindexTask> getTasksByCaseType(String caseType);

    ReindexEntity saveEntity(Boolean reindex, Boolean deleteOldIndex, CaseTypeEntity caseType,
                             String newIndexName);

    void updateEntity(String newIndexName, String response);

    void updateEntity(String newIndexName, Exception exception);
}
