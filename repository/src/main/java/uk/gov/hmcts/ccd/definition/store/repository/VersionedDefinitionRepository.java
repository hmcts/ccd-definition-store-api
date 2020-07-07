package uk.gov.hmcts.ccd.definition.store.repository;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
@NoRepositoryBean
public interface VersionedDefinitionRepository<T, ID extends Serializable> extends DefinitionRepository<T, ID> {

    Optional<Integer> findLastVersion(String reference);

    Optional<T> findFirstByReferenceOrderByVersionDesc(String reference);

}
