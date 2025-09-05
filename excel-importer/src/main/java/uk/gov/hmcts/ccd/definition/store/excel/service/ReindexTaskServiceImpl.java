package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexDTO;

import java.util.List;

@Service
public class ReindexTaskServiceImpl implements ReindexTaskService {

    private final ReindexRepository reindexRepository;
    private final EntityToResponseDTOMapper mapper;

    @Autowired
    public ReindexTaskServiceImpl(ReindexRepository reindexRepository, EntityToResponseDTOMapper mapper) {
        this.reindexRepository = reindexRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ReindexDTO> getAll() {
        return reindexRepository.findAll()
            .stream()
            .map(mapper::map)
            .toList();
    }

    @Override
    public List<ReindexDTO> getTasksByCaseType(String caseType) {
        if (StringUtils.isBlank(caseType)) {
            return getAll();
        }
        return reindexRepository.findByCaseType(caseType)
            .stream()
            .map(mapper::map)
            .toList();
    }
}
