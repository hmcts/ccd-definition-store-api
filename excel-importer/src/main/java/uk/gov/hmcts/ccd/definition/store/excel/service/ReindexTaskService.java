package uk.gov.hmcts.ccd.definition.store.excel.service;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.util.List;

public interface ReindexTaskService {
    List<ReindexEntity> getAllReindexTasks();
    List<ReindexEntity> getTasksByCaseType(String caseType);
}
