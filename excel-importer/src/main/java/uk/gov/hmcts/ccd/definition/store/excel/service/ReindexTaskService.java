package uk.gov.hmcts.ccd.definition.store.excel.service;

import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexDTO;

import java.util.List;

public interface ReindexTaskService {
    List<ReindexDTO> getAll();

    List<ReindexDTO> getTasksByCaseType(String caseType);
}
