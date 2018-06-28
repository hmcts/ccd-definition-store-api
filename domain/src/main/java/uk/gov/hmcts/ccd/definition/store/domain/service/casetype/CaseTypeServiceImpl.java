package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.LegacyCaseTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityValidator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CaseTypeServiceImpl implements CaseTypeService {

    private final CaseTypeRepository repository;
    private final EntityToResponseDTOMapper caseTypeMapper;
    private final LegacyCaseTypeValidator legacyCaseTypeValidator;
    private final List<CaseTypeEntityValidator> caseTypeEntityValidators;
    private final VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> versionedRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public CaseTypeServiceImpl(CaseTypeRepository repository,
                               EntityToResponseDTOMapper caseTypeMapper,
                               LegacyCaseTypeValidator legacyCaseTypeValidator,
                               List<CaseTypeEntityValidator> caseTypeEntityValidators,
                               ApplicationEventPublisher applicationEventPublisher
    ) {
        this.repository = repository;
        this.caseTypeMapper = caseTypeMapper;
        this.legacyCaseTypeValidator = legacyCaseTypeValidator;
        this.caseTypeEntityValidators = caseTypeEntityValidators;
        this.versionedRepository = new VersionedDefinitionRepositoryDecorator<>(repository);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void createAll(JurisdictionEntity jurisdiction, Collection<CaseTypeEntity> caseTypes) {

        ValidationResult validationResult = new ValidationResult();

        caseTypes.forEach(
            caseTypeEntity -> {
                caseTypeEntity.setJurisdiction(jurisdiction);
                legacyCaseTypeValidator.validateCaseType(caseTypeEntity);
                validationResult.merge(validate(caseTypeEntity));
            }
        );

        if (validationResult.isValid()) {
            versionedRepository.save(caseTypes);
        }
        else {
            throw new ValidationException(validationResult);
        }
        applicationEventPublisher.publishEvent(new DefinitionImportedEvent(new ArrayList(caseTypes)));
    }

    @Override
    public List<CaseType> findByJurisdictionId(String jurisdictionId) {
        Optional<List<CaseTypeEntity>> caseTypeEntities
            = Optional.ofNullable(repository.findByJurisdictionId(jurisdictionId));

        return caseTypeEntities.orElse(Collections.emptyList())
             .stream()
             .map(caseTypeMapper::map)
             .collect(Collectors.toList());
    }

    @Override
    public Optional<CaseType> findByCaseTypeId(String id) {
        return repository.findCurrentVersionForReference(id)
            .map(caseTypeMapper::map);

    }

    @Override
    public Optional<CaseTypeVersionInformation> findVersionInfoByCaseTypeId(final String id) {
        return repository.findLastVersion(id)
                         .map(CaseTypeVersionInformation::new);
    }

    private ValidationResult validate(CaseTypeEntity caseTypeEntity) {
        ValidationResult validationResult = new ValidationResult();
        for (CaseTypeEntityValidator caseTypeEntityValidator : caseTypeEntityValidators) {
            validationResult.merge(caseTypeEntityValidator.validate(caseTypeEntity));
        }
        return validationResult;
    }

}
