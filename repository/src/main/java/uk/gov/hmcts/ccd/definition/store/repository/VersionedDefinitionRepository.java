package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
@NoRepositoryBean
public interface VersionedDefinitionRepository<T, ID extends Serializable> extends DefinitionRepository<T, ID> {

    Optional<Integer> findLastVersion(String reference);

    Optional<T> findFirstByReferenceOrderByVersionDesc(String reference);

}
