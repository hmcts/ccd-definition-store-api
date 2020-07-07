package uk.gov.hmcts.ccd.definition.store.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import uk.gov.hmcts.ccd.definition.store.repository.entity.Versionable;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public class VersionedDefinitionRepositoryDecorator<T extends Versionable, ID extends Serializable>
    extends AbstractDefinitionRepositoryDecorator<T, ID, VersionedDefinitionRepository<T, ID>> {

    public VersionedDefinitionRepositoryDecorator(VersionedDefinitionRepository repository) {
        super(repository);
    }

    @Override
    public <S extends T> S save(S s) {
        final Optional<Integer> version = repository.findLastVersion(s.getReference());
        s.setVersion(1 + version.orElse(0));
        return repository.save(s);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        for (S s : iterable) {
            final Optional<Integer> version = repository.findLastVersion(s.getReference());
            s.setVersion(1 + version.orElse(0));
        }
        return repository.saveAll(iterable);
    }
}
