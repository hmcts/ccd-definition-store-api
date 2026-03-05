package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Versionable;

import java.io.Serializable;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

@SuppressWarnings({"checkstyle:InterfaceTypeParameterName", "checkstyle:ClassTypeParameterName"})
public class VersionedDefinitionRepositoryDecorator<T extends Versionable, ID extends Serializable>
    extends AbstractDefinitionRepositoryDecorator<T, ID, VersionedDefinitionRepository<T, ID>> {

    private static final Logger LOG = LoggerFactory.getLogger(VersionedDefinitionRepositoryDecorator.class);
    private static final int MAX_DUPLICATE_RETRIES = 1;
    private static final String FIELD_TYPE_DUPLICATE_CONSTRAINT = "unique_field_type_reference_version_jurisdiction";

    public VersionedDefinitionRepositoryDecorator(VersionedDefinitionRepository repository) {
        super(repository);
    }

    @Override
    public <S extends T> S save(S s) {
        for (int attempt = 0; ; attempt++) {
            final Optional<Integer> version = repository.findLastVersion(s.getReference());
            s.setVersion(1 + version.orElse(0));
            try {
                return repository.save(s);
            } catch (DataIntegrityViolationException e) {
                if (shouldRetryFieldTypeDuplicate(e) && attempt < MAX_DUPLICATE_RETRIES) {
                    LOG.warn("Duplicate field_type version detected for reference '{}'; retrying with latest version",
                        s.getReference());
                    continue;
                }
                throw e;
            }
        }
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        List<S> entities = new ArrayList<>();
        for (S s : iterable) {
            entities.add(s);
        }

        for (int attempt = 0; ; attempt++) {
            for (S s : entities) {
                final Optional<Integer> version = repository.findLastVersion(s.getReference());
                s.setVersion(1 + version.orElse(0));
            }
            try {
                return repository.saveAll(entities);
            } catch (DataIntegrityViolationException e) {
                if (shouldRetryFieldTypeDuplicate(e) && attempt < MAX_DUPLICATE_RETRIES) {
                    LOG.warn("Duplicate field_type version detected during batch; retrying with latest versions");
                    continue;
                }
                throw e;
            }
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

    private boolean shouldRetryFieldTypeDuplicate(DataIntegrityViolationException e) {
        Throwable current = e;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(FIELD_TYPE_DUPLICATE_CONSTRAINT)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
