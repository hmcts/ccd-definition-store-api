package uk.gov.hmcts.ccd.definition.store.write.repository;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ccd.definition.store.repository.DefinitionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Versionable;

@NoRepositoryBean
public interface VersionedWriteDefinitionRepository<T, ID extends Serializable> extends WriteDefinitionRepository<T, ID> {

    Optional<Integer> findLastVersion(String reference);

    Optional<T> findFirstByReferenceOrderByVersionDesc(String reference);

}
