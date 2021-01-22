package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

import java.util.List;

public interface SearchAliasFieldRepository extends JpaRepository<SearchAliasFieldEntity, Long> {

    @Query("select s from SearchAliasFieldEntity s "
        + "where s.reference=?1 "
        + "and s.caseType.id = (select c.id from CaseTypeEntity c "
        + "where s.caseType.id=c.id "
        + "and c.version = (select max(cm.version) from CaseTypeEntity as cm "
        + "where cm.reference = c.reference))")
    List<SearchAliasFieldEntity> findByReference(String reference);

}
