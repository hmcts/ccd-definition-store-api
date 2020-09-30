package uk.gov.hmcts.ccd.definition.store.domain.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContextFactory;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidator;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class FieldTypeServiceImpl implements FieldTypeService {

    private final FieldTypeRepository repository;
    private final VersionedDefinitionRepositoryDecorator<FieldTypeEntity, Integer> versionedRepository;
    private final FieldTypeValidationContextFactory validationContextFactory;
    private final List<FieldTypeValidator> validators;

    @Autowired
    public FieldTypeServiceImpl(FieldTypeRepository repository,
                                FieldTypeValidationContextFactory validationContextFactory,
                                List<FieldTypeValidator> validators) {
        this.repository = repository;
        this.versionedRepository = new VersionedDefinitionRepositoryDecorator<>(repository);
        this.validationContextFactory = validationContextFactory;
        this.validators = validators;
    }

    @Override
    public List<FieldTypeEntity> getBaseTypes() {
        return repository.findCurrentBaseTypes();
    }

    @Override
    public List<FieldTypeEntity> getTypesByJurisdiction(String jurisdictionReference) {
        // FIXME
        return Lists.newArrayList();
    }

    @Override
    public List<FieldTypeEntity> getPredefinedComplexTypes() {
        return this.repository.findPredefinedComplexTypes();
    }

    @Override
    public void saveTypes(JurisdictionEntity jurisdiction, Collection<FieldTypeEntity> fieldTypes) {
        final FieldTypeValidationContext validationContext = validationContextFactory.create();

        ValidationResult result = new ValidationResult();

        for (FieldTypeEntity fieldTypeEntity : fieldTypes) {
            fieldTypeEntity.setJurisdiction(jurisdiction);

            for (FieldTypeValidator validator : validators) {
                result.merge(validator.validate(validationContext, fieldTypeEntity));
            }
        }

        if (!result.isValid()) {
            throw new ValidationException(result);
        }

        versionedRepository.saveAll(fieldTypes);
    }

    @Override
    public void save(FieldTypeEntity fieldTypeEntity) {

        final ValidationResult result = new ValidationResult();
        final FieldTypeValidationContext validationContext = validationContextFactory.create();

        for (FieldTypeValidator validator : validators) {
            result.merge(validator.validate(validationContext, fieldTypeEntity));
        }
        if (!result.isValid()) {
            throw new ValidationException(result);
        }

        versionedRepository.save(fieldTypeEntity);
    }

    @Override
    public Optional<FieldTypeEntity> findBaseType(String text) {
        return repository.findBaseType(text);
    }

}
