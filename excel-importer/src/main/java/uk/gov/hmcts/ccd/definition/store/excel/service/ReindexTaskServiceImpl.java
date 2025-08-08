package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.util.List;
@Service
public class ReindexTaskServiceImpl implements ReindexTaskService {

    private final ReindexRepository reindexRepository;

    @Autowired
    public ReindexTaskServiceImpl(ReindexRepository reindexRepository) {
        this.reindexRepository = reindexRepository;
    }

    @Override
    public List<ReindexEntity> getAllReindexTasks() {
        return reindexRepository.findAll();
    }

    @Override
    public List<ReindexEntity> getTasksByCaseType(String caseType) {
        if (caseType == null || caseType.isBlank()) {
            return reindexRepository.findAll();
        }
        return reindexRepository.findByCaseType(caseType.toLowerCase());
    }
}
