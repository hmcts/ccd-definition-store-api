package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus;
import uk.gov.hmcts.ccd.definition.store.write.repository.CustomDefEntityRepository;
import uk.gov.hmcts.ccd.definition.store.write.repository.WriteDefinitionRepository;

import java.util.List;
import java.util.Optional;

@Component
public class DraftDefinitionRepositoryDecorator {

    private final DraftDefinitionRepository repository;
    private CustomDefEntityRepository writeRepo;

    @Autowired
    public DraftDefinitionRepositoryDecorator(DraftDefinitionRepository repository, CustomDefEntityRepository writeRepo) {
        this.repository = repository;
        this.writeRepo = writeRepo;
    }

    public DefinitionEntity save(DefinitionEntity definitionEntity) {
        final Optional<Integer> version = repository.findLastVersion(definitionEntity.getJurisdiction().getReference());
        definitionEntity.setVersion(1 + version.orElse(0));
        if (definitionEntity.getStatus() == null) {
            definitionEntity.setStatus(DefinitionStatus.DRAFT);
        }
        return writeRepo.save(definitionEntity);
    }

    public DefinitionEntity findByJurisdictionIdAndVersion(final String jurisdiction, final Integer version) {
        if (null == version) {
            return repository.findLatestByJurisdictionId(jurisdiction);
        }
        return repository.findByJurisdictionIdAndVersion(jurisdiction, version);
    }

    public List<DefinitionEntity> findByJurisdictionId(final String jurisdiction) {
        return repository.findByJurisdictionId(jurisdiction);
    }

    public DefinitionEntity simpleSave(final DefinitionEntity definitionEntity) {
        return writeRepo.save(definitionEntity);
    }
}
