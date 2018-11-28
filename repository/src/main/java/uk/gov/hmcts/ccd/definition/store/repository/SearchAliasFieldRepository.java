package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

public interface SearchAliasFieldRepository extends JpaRepository<SearchAliasFieldEntity, Long> {

    List<SearchAliasFieldEntity> findByReference(String reference);

}
