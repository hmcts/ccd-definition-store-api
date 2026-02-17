package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Versionable;

import java.io.Serializable;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings({"checkstyle:InterfaceTypeParameterName", "checkstyle:ClassTypeParameterName"})
public class VersionedDefinitionRepositoryDecorator<T extends Versionable, ID extends Serializable>
    extends AbstractDefinitionRepositoryDecorator<T, ID, VersionedDefinitionRepository<T, ID>> {

    public VersionedDefinitionRepositoryDecorator(VersionedDefinitionRepository repository) {
        super(repository);
    }

    @Override
    public <S extends T> S save(S s) {
        try {
            assignVersion(s);
            return repository.save(s);
        } catch (DataIntegrityViolationException e) {
            // Same file uploaded from another invocation may have committed a version we did not see
            assignVersion(s);
            return repository.save(s);
        }
    }

    private <S extends T> void assignVersion(S s) {
        final Optional<Integer> version = repository.findLastVersion(s.getReference());
        s.setVersion(1 + version.orElse(0));
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        final List<S> list = toList(iterable);
        assignVersionsForBatch(list);
        try {
            return repository.saveAll(list);
        } catch (DataIntegrityViolationException e) {
            // Same file uploaded from another invocation (e.g. concurrent or sequential) may have
            // committed versions we did not see; re-assign versions from current DB and retry once
            assignVersionsForBatch(list);
            return repository.saveAll(list);
        }
    }

    private <S extends T> List<S> toList(Iterable<S> iterable) {
        if (iterable instanceof List) {
            return (List<S>) iterable;
        }
        List<S> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    /**
     * Assigns versions so that (1) each reference gets the next version after the last in DB,
     * and (2) within the same batch, duplicate references get incrementing versions to avoid
     * unique constraint violations when saveAll is called.
     */
    private <S extends T> void assignVersionsForBatch(List<S> list) {
        final Map<String, Integer> nextVersionPerReference = new HashMap<>();
        for (S s : list) {
            final String reference = s.getReference();
            final int lastFromDb = repository.findLastVersion(reference).orElse(0);
            final int lastInBatch = nextVersionPerReference.getOrDefault(reference, lastFromDb);
            final int nextVersion = lastInBatch + 1;
            nextVersionPerReference.put(reference, nextVersion);
            s.setVersion(nextVersion);
        }
    }

    @Override
    public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
        return new ArrayList<>();
    }

    @Override
    public void deleteAllInBatch(Iterable<T> entities) {
        //empty boilerplate implementation required
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        //empty boilerplate implementation required
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<ID> ids) {
        //empty boilerplate implementation required
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
