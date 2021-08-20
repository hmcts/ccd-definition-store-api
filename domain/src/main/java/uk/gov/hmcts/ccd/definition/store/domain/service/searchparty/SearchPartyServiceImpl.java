package uk.gov.hmcts.ccd.definition.store.domain.service.searchparty;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.SearchPartyRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchParty;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchPartyServiceImpl implements SearchPartyService {

    private final SearchPartyRepository repository;

    private final EntityToResponseDTOMapper dtoMapper;

    public SearchPartyServiceImpl(SearchPartyRepository repository,
                                  EntityToResponseDTOMapper dtoMapper) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public void saveAll(List<SearchPartyEntity> entityList) {
        repository.saveAll(entityList);
    }

    @Override
    public List<SearchParty> findSearchPartyName(String searchPartyName) {
        List<SearchPartyEntity> searchPartyEntities = repository
            .findBySearchPartyName(searchPartyName);
        return searchPartyEntities.stream()
            .map(dtoMapper::map)
            .collect(Collectors.toList());
    }
}
