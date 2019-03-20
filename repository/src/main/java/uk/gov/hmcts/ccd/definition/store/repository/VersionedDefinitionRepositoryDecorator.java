package uk.gov.hmcts.ccd.definition.store.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uk.gov.hmcts.ccd.definition.store.repository.entity.VersionableDefEntity;
import uk.gov.hmcts.ccd.definition.store.write.repository.DefEntityWriteRepository;

public class VersionedDefinitionRepositoryDecorator<T extends VersionableDefEntity, ID extends Serializable>
    extends AbstractDefinitionRepositoryDecorator<T, ID, VersionedDefinitionRepository<T, ID>> {

    private DefEntityWriteRepository defEntityRepository;

    public VersionedDefinitionRepositoryDecorator(VersionedDefinitionRepository repository, DefEntityWriteRepository defEntityRepository) {
        super(repository);
        this.defEntityRepository = defEntityRepository;
    }

    @Override
    public <S extends T> S save(S s) {
        final Optional<Integer> version = repository.findLastVersion(s.getReference());
        s.setVersion(1 + version.orElse(0));
        return defEntityRepository.save(s);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        List<S> result = new ArrayList<>();
        for (S s : iterable) {
            final Optional<Integer> version = repository.findLastVersion(s.getReference());
            s.setVersion(1 + version.orElse(0));
            result.add(defEntityRepository.save(s));
        }
        return result;
    }
}
