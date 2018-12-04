package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

public interface SearchAliasFieldRepository extends JpaRepository<SearchAliasFieldEntity, Long> {

    @Query("select s from SearchAliasFieldEntity s "
               + "where s.reference=:reference "
               + "and s.caseType = (select c from CaseTypeEntity c "
                                    + "where c.reference=s.caseType.reference "
                                    + "and c.version = (select max(cm.version) from CaseTypeEntity as cm "
                                                        + "where cm.reference = c.reference))")
    List<SearchAliasFieldEntity> findByReference(String reference);

}
