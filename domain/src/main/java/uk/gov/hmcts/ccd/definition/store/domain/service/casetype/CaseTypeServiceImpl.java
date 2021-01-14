package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.LegacyCaseTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataFieldService;
import uk.gov.hmcts.ccd.definition.store.domain.validation.MissingUserRolesException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityNonUniqueReferenceValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityReferenceSpellingValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Component
public class CaseTypeServiceImpl implements CaseTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(CaseTypeServiceImpl.class);

    private final CaseTypeRepository repository;
    private final EntityToResponseDTOMapper dtoMapper;
    private final LegacyCaseTypeValidator legacyCaseTypeValidator;
    private final List<CaseTypeEntityValidator> caseTypeEntityValidators;
    private final VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> versionedRepository;
    private final MetadataFieldService metadataFieldService;

    @Autowired
    public CaseTypeServiceImpl(CaseTypeRepository repository,
                               EntityToResponseDTOMapper dtoMapper,
                               LegacyCaseTypeValidator legacyCaseTypeValidator,
                               List<CaseTypeEntityValidator> caseTypeEntityValidators,
                               MetadataFieldService metadataFieldService
    ) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.legacyCaseTypeValidator = legacyCaseTypeValidator;
        this.caseTypeEntityValidators = caseTypeEntityValidators;
        this.versionedRepository = new VersionedDefinitionRepositoryDecorator<>(repository);
        this.metadataFieldService = metadataFieldService;
    }

    @Override
    public void createAll(JurisdictionEntity jurisdiction,
                          Collection<CaseTypeEntity> caseTypes,
                          Set<String> missingUserRoles) {
        validate(jurisdiction, caseTypes, missingUserRoles);
        versionedRepository.saveAll(caseTypes);
    }

    @Override
    public boolean caseTypeExistsInAnyJurisdiction(String reference, String jurisdictionId) {
        return repository.caseTypeExistsInAnyJurisdiction(reference, jurisdictionId) > 0;
    }

    @Override
    public List<CaseType> findByJurisdictionId(String jurisdictionId) {
        Optional<List<CaseTypeEntity>> caseTypeEntities
            = Optional.ofNullable(repository.findByJurisdictionId(jurisdictionId));

        return caseTypeEntities.orElse(Collections.emptyList())
            .stream()
            .map(dtoMapper::map)
            .map(this::addMetadataFields)
            .collect(toList());
    }

    @Override
    public Optional<CaseType> findByCaseTypeId(String id) {
        return repository.findCurrentVersionForReference(id)
            .map(dtoMapper::map)
            .map(this::addMetadataFields);
    }

    @Override
    public Optional<CaseTypeVersionInformation> findVersionInfoByCaseTypeId(final String id) {
        return repository.findLastVersion(id)
            .map(CaseTypeVersionInformation::new);
    }

    @Override
    public String findDefinitiveCaseTypeId(String id) {
        return repository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(id)
            .map(dtoMapper::map)
            .map(CaseType::getId)
            .orElse(null);
    }

    private ValidationResult validate(CaseTypeEntity caseTypeEntity) {
        ValidationResult validationResult = new ValidationResult();
        for (CaseTypeEntityValidator caseTypeEntityValidator : caseTypeEntityValidators) {
            validationResult.merge(caseTypeEntityValidator.validate(caseTypeEntity));
        }
        return validationResult;
    }

    private void validate(JurisdictionEntity jurisdiction,
                          Collection<CaseTypeEntity> caseTypes,
                          Set<String> missingUserRoles) {
        ValidationResult validationResult = new ValidationResult();
        caseTypes.forEach(
            caseTypeEntity -> {
                caseTypeEntity.setJurisdiction(jurisdiction);
                legacyCaseTypeValidator.validateCaseType(caseTypeEntity);
                validationResult.merge(validate(caseTypeEntity));
                String definitiveCaseTypeId = findDefinitiveCaseTypeId(caseTypeEntity.getReference());
                if (definitiveCaseTypeId != null && !caseTypeEntity.getReference().equals(definitiveCaseTypeId)) {
                    validationResult.addError(
                        new CaseTypeEntityReferenceSpellingValidationError(definitiveCaseTypeId, caseTypeEntity));
                }
                if (caseTypeExistsInAnyJurisdiction(caseTypeEntity.getReference(), jurisdiction.getReference())) {
                    validationResult.addError(
                        new CaseTypeEntityNonUniqueReferenceValidationError(caseTypeEntity));
                }
            }
        );

        validationResult.getValidationErrors().forEach(vr -> LOG.warn(vr.toString()));
        if (missingUserRoles.size() > 0) {
            throw new MissingUserRolesException(missingUserRoles, validationResult.getValidationErrors());
        }
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult);
        }
    }

    private CaseType addMetadataFields(CaseType caseType) {
        caseType.addCaseFields(metadataFieldService.getCaseMetadataFields());
        return caseType;
    }
}
