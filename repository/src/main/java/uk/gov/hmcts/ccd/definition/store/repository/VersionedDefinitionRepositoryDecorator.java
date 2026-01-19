package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Versionable;

import java.io.Serializable;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

@SuppressWarnings({"checkstyle:InterfaceTypeParameterName", "checkstyle:ClassTypeParameterName"})
public class VersionedDefinitionRepositoryDecorator<T extends Versionable, ID extends Serializable>
    extends AbstractDefinitionRepositoryDecorator<T, ID, VersionedDefinitionRepository<T, ID>> {

    private static final Set<String> IGNORED_FIELD_NAMES = new HashSet<>(Arrays.asList(
        "id",
        "version",
        "createdAt"
    ));

    private final boolean skipDuplicateEntries;

    public VersionedDefinitionRepositoryDecorator(VersionedDefinitionRepository repository) {
        this(repository, false);
    }

    public VersionedDefinitionRepositoryDecorator(VersionedDefinitionRepository repository,
                                                  boolean skipDuplicateEntries) {
        super(repository);
        this.skipDuplicateEntries = skipDuplicateEntries;
    }

    @Override
    public <S extends T> S save(S s) {
        if (skipDuplicateEntries) {
            Optional<T> existing = repository.findFirstByReferenceOrderByVersionDesc(s.getReference());
            if (existing.isPresent() && isEquivalent(existing.get(), s)) {
                @SuppressWarnings("unchecked")
                S existingEntity = (S) existing.get();
                return existingEntity;
            }
        }
        final Optional<Integer> version = repository.findLastVersion(s.getReference());
        s.setVersion(1 + version.orElse(0));
        return repository.save(s);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        if (skipDuplicateEntries) {
            List<S> results = new ArrayList<>();
            for (S s : iterable) {
                results.add(save(s));
            }
            return results;
        }
        for (S s : iterable) {
            final Optional<Integer> version = repository.findLastVersion(s.getReference());
            s.setVersion(1 + version.orElse(0));
        }
        return repository.saveAll(iterable);
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

    private boolean isEquivalent(T existing, T candidate) {
        if (existing == null || candidate == null) {
            return false;
        }
        if (!Hibernate.getClass(existing).equals(Hibernate.getClass(candidate))) {
            return false;
        }
        for (Field field : getAllFields(candidate.getClass())) {
            if (shouldIgnoreField(field)) {
                continue;
            }
            Object existingValue = readField(field, existing);
            Object candidateValue = readField(field, candidate);
            if (isEntityAssociationField(field)) {
                Object existingId = extractEntityId(existingValue);
                Object candidateId = extractEntityId(candidateValue);
                if ((existingId != null || candidateId != null) && !Objects.equals(existingId, candidateId)) {
                    return false;
                }
                if (existingId == null && candidateId == null
                    && !Objects.deepEquals(existingValue, candidateValue)) {
                    return false;
                }
            } else if (!Objects.deepEquals(existingValue, candidateValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldIgnoreField(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            return true;
        }
        if (IGNORED_FIELD_NAMES.contains(field.getName())) {
            return true;
        }
        if (field.isAnnotationPresent(Id.class)
            || field.isAnnotationPresent(CreationTimestamp.class)
            || field.isAnnotationPresent(Transient.class)
            || field.isAnnotationPresent(OneToMany.class)
            || field.isAnnotationPresent(ManyToMany.class)) {
            return true;
        }
        return java.util.Collection.class.isAssignableFrom(field.getType());
    }

    private boolean isEntityAssociationField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
    }

    private Object readField(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access field for comparison: " + field.getName(), e);
        }
    }

    private Object extractEntityId(Object value) {
        if (value == null) {
            return null;
        }
        for (Field field : getAllFields(value.getClass())) {
            if (field.isAnnotationPresent(Id.class)) {
                return readField(field, value);
            }
        }
        return null;
    }

    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }
}
