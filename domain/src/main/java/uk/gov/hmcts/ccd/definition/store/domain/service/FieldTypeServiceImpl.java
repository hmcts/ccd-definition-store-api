package uk.gov.hmcts.ccd.definition.store.domain.service;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContextFactory;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidator;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class FieldTypeServiceImpl implements FieldTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(FieldTypeServiceImpl.class);

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
        Collection<FieldTypeEntity> uniqueFieldTypes = dedupeFieldTypes(fieldTypes);
        final FieldTypeValidationContext validationContext = validationContextFactory.create();

        ValidationResult result = new ValidationResult();

        for (FieldTypeEntity fieldTypeEntity : uniqueFieldTypes) {
            fieldTypeEntity.setJurisdiction(jurisdiction);

            for (FieldTypeValidator validator : validators) {
                result.merge(validator.validate(validationContext, fieldTypeEntity));
            }
        }

        if (!result.isValid()) {
            throw new ValidationException(result);
        }

        versionedRepository.saveAll(uniqueFieldTypes);
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

    private Collection<FieldTypeEntity> dedupeFieldTypes(Collection<FieldTypeEntity> fieldTypes) {
        Map<String, FieldTypeEntity> uniqueByReference = new LinkedHashMap<>();

        for (FieldTypeEntity fieldType : fieldTypes) {
            String reference = fieldType.getReference();
            FieldTypeEntity existing = uniqueByReference.get(reference);
            if (existing == null) {
                uniqueByReference.put(reference, fieldType);
                continue;
            }

            LOG.warn("Duplicate field type reference '{}' found in import; merging definitions.", reference);
            mergeFieldTypes(existing, fieldType);
        }

        return uniqueByReference.values();
    }

    private void mergeFieldTypes(FieldTypeEntity target, FieldTypeEntity incoming) {
        mergeScalarField("regular_expression", target.getRegularExpression(), incoming.getRegularExpression(),
            target::setRegularExpression);
        mergeScalarField("minimum", target.getMinimum(), incoming.getMinimum(), target::setMinimum);
        mergeScalarField("maximum", target.getMaximum(), incoming.getMaximum(), target::setMaximum);

        if (target.getBaseFieldType() == null && incoming.getBaseFieldType() != null) {
            target.setBaseFieldType(incoming.getBaseFieldType());
        } else if (target.getBaseFieldType() != null && incoming.getBaseFieldType() != null
            && !Objects.equals(target.getBaseFieldType().getReference(), incoming.getBaseFieldType().getReference())) {
            LOG.warn("Conflicting base field type for reference '{}': '{}' vs '{}'",
                target.getReference(),
                target.getBaseFieldType().getReference(),
                incoming.getBaseFieldType().getReference());
        }

        if (target.getCollectionFieldType() == null && incoming.getCollectionFieldType() != null) {
            target.setCollectionFieldType(incoming.getCollectionFieldType());
        } else if (target.getCollectionFieldType() != null && incoming.getCollectionFieldType() != null
            && !Objects.equals(target.getCollectionFieldType().getReference(),
            incoming.getCollectionFieldType().getReference())) {
            LOG.warn("Conflicting collection field type for reference '{}': '{}' vs '{}'",
                target.getReference(),
                target.getCollectionFieldType().getReference(),
                incoming.getCollectionFieldType().getReference());
        }

        if (target.getJurisdiction() == null && incoming.getJurisdiction() != null) {
            target.setJurisdiction(incoming.getJurisdiction());
        }

        mergeListItems(target, incoming);
        mergeComplexFields(target, incoming);
    }

    private void mergeScalarField(String fieldName, String currentValue, String incomingValue,
                                  java.util.function.Consumer<String> setter) {
        if (currentValue == null && incomingValue != null) {
            setter.accept(incomingValue);
        } else if (currentValue != null && incomingValue != null && !Objects.equals(currentValue, incomingValue)) {
            LOG.warn("Conflicting {} for reference: keeping '{}' and ignoring '{}'",
                fieldName, currentValue, incomingValue);
        }
    }

    private void mergeListItems(FieldTypeEntity target, FieldTypeEntity incoming) {
        if (incoming.getListItems().isEmpty()) {
            return;
        }

        Map<String, FieldTypeListItemEntity> existingByValue = new HashMap<>();
        for (FieldTypeListItemEntity item : target.getListItems()) {
            existingByValue.put(item.getValue(), item);
        }

        for (FieldTypeListItemEntity item : incoming.getListItems()) {
            FieldTypeListItemEntity existing = existingByValue.get(item.getValue());
            if (existing == null) {
                item.setFieldType(target);
                target.getListItems().add(item);
                existingByValue.put(item.getValue(), item);
                continue;
            }

            if (!Objects.equals(existing.getLabel(), item.getLabel())
                || !Objects.equals(existing.getOrder(), item.getOrder())) {
                LOG.warn("Conflicting list item '{}' for reference '{}': keeping existing item",
                    item.getValue(), target.getReference());
            }
        }
    }

    private void mergeComplexFields(FieldTypeEntity target, FieldTypeEntity incoming) {
        if (incoming.getComplexFields().isEmpty()) {
            return;
        }

        Map<String, ComplexFieldEntity> existingByReference = new HashMap<>();
        for (ComplexFieldEntity field : target.getComplexFields()) {
            existingByReference.put(field.getReference(), field);
        }

        for (ComplexFieldEntity field : incoming.getComplexFields()) {
            ComplexFieldEntity existing = existingByReference.get(field.getReference());
            if (existing == null) {
                field.setComplexFieldType(target);
                target.getComplexFields().add(field);
                existingByReference.put(field.getReference(), field);
                continue;
            }

            LOG.warn("Conflicting complex field '{}' for reference '{}': keeping existing field",
                field.getReference(), target.getReference());
        }
    }

}
