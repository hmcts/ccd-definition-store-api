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

    private static final int MAX_SAVE_RETRIES = 3;

    public VersionedDefinitionRepositoryDecorator(VersionedDefinitionRepository repository) {
        super(repository);
    }

    @Override
    public <S extends T> S save(S s) {
        for (int attempt = 1; attempt <= MAX_SAVE_RETRIES; attempt++) {
            assignNextVersion(s);
            try {
                return repository.save(s);
            } catch (DataIntegrityViolationException ex) {
                if (attempt == MAX_SAVE_RETRIES) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        for (int attempt = 1; attempt <= MAX_SAVE_RETRIES; attempt++) {
            assignNextVersions(iterable);
            try {
                return repository.saveAll(iterable);
            } catch (DataIntegrityViolationException ex) {
                if (attempt == MAX_SAVE_RETRIES) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    private <S extends T> void assignNextVersion(S s) {
        final Optional<Integer> version = repository.findLastVersion(s.getReference());
        s.setVersion(1 + version.orElse(0));
    }

    private <S extends T> void assignNextVersions(Iterable<S> iterable) {
        Map<String, Integer> nextVersionsByReference = new HashMap<>();
        for (S s : iterable) {
            String reference = s.getReference();
            Integer nextVersion = nextVersionsByReference.get(reference);
            if (nextVersion == null) {
                final Optional<Integer> version = repository.findLastVersion(reference);
                nextVersion = 1 + version.orElse(0);
            }
            s.setVersion(nextVersion);
            nextVersionsByReference.put(reference, nextVersion + 1);
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
