package uk.gov.hmcts.ccd.definition.store.write.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import uk.gov.hmcts.ccd.definition.store.repository.entity.Versionable;

public class VersionedDefinitionRepositoryDecorator<T extends Versionable, ID extends Serializable> extends AbstractWriteDefinitionRepositoryDecorator<T, ID,
    VersionedWriteDefinitionRepository<T, ID>> {

    public VersionedDefinitionRepositoryDecorator(VersionedWriteDefinitionRepository repository) {
        super(repository);
    }

    @Override
    public <S extends T> S save(S s) {
        final Optional<Integer> version = repository.findLastVersion(s.getReference());
        s.setVersion(1 + version.orElse(0));
        return repository.save(s);
    }

    @Override
    public <S extends T> List<S> save(Iterable<S> iterable) {
        for (S s : iterable) {
            final Optional<Integer> version = repository.findLastVersion(s.getReference());
            s.setVersion(1 + version.orElse(0));
        }
        return repository.save(iterable);
    }
}
