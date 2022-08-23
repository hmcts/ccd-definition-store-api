package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Versionable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings({"checkstyle:InterfaceTypeParameterName", "checkstyle:ClassTypeParameterName"})
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

    @Override
    public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<T> entities) {

    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<ID> ids) {

    }

    @Override
    public T getById(ID id) {
        return null;
    }

    @Override
    public T getReferenceById(ID id) {
        return null;
    }

    @Override
    public <S extends T, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>,
        R> queryFunction) {
        return null;
    }
}
