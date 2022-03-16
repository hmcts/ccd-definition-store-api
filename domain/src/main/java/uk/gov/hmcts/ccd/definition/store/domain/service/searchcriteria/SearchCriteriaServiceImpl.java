package uk.gov.hmcts.ccd.definition.store.domain.service.searchcriteria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleServiceImpl;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SearchCriteriaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;

import java.util.List;

@Component
public class SearchCriteriaServiceImpl implements SearchCriteriaService {

    private static final Logger LOG = LoggerFactory.getLogger(CaseRoleServiceImpl.class);
    private final SearchCriteriaRepository repository;
    private final EntityToResponseDTOMapper dtoMapper;
    private final CaseTypeRepository caseTypeRepository;


    @Autowired
    public SearchCriteriaServiceImpl(SearchCriteriaRepository repository,
                                     EntityToResponseDTOMapper dtoMapper, CaseTypeRepository caseTypeRepository) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.caseTypeRepository = caseTypeRepository;
    }

    @Override
    public void saveAll(List<SearchCriteriaEntity> entityList) {
        repository.saveAll(entityList);
    }

}
