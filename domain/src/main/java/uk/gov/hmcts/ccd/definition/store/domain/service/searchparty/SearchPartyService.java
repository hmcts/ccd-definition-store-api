package uk.gov.hmcts.ccd.definition.store.domain.service.searchparty;

import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchParty;

import java.util.List;

public interface SearchPartyService {

    void saveAll(List<SearchPartyEntity> entityList);

    List<SearchParty> findSearchPartyName(String searchPartyName);

}
