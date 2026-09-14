package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
@NoRepositoryBean
public interface VersionedDefinitionRepository<T, ID extends Serializable> extends DefinitionRepository<T, ID> {

    Optional<Integer> findLastVersion(String reference);

    @Query("""
        select entity.reference as reference, max(entity.version) as version
        from #{#entityName} entity
        where entity.reference in :references
        group by entity.reference
        """)
    List<ReferenceVersion> findLastVersions(@Param("references") Collection<String> references);

    Optional<T> findFirstByReferenceOrderByVersionDesc(String reference);

    interface ReferenceVersion {
        String getReference();

        Integer getVersion();
    }
}
