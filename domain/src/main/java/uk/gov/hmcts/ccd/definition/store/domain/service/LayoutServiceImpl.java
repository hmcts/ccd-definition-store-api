package uk.gov.hmcts.ccd.definition.store.domain.service;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutEntityValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutValidator;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.GenericLayoutRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LayoutServiceImpl implements LayoutService {
    private final GenericLayoutRepository genericRepository;
    private final List<GenericLayoutValidator> genericLayoutValidators;
    private final DisplayGroupRepository displayGroupRepository;
    private final List<DisplayGroupValidator> displayGroupValidators;

    @Autowired
    public LayoutServiceImpl(GenericLayoutRepository genericRepository,
                             List<GenericLayoutValidator> genericLayoutValidators,
                             DisplayGroupRepository displayGroupRepository,
                             List<DisplayGroupValidator> displayGroupValidators) {
        this.genericRepository = genericRepository;
        this.genericLayoutValidators = genericLayoutValidators;
        this.displayGroupRepository = displayGroupRepository;
        this.displayGroupValidators = displayGroupValidators;
    }

    @Override
    public void createGenerics(List<GenericLayoutEntity> genericLayouts) {
        validate(genericLayouts);
        genericRepository.saveAll(genericLayouts);
    }

    @Override
    public void createDisplayGroups(List<DisplayGroupEntity> displayGroups) {
        ValidationResult result = new ValidationResult();
        for (DisplayGroupEntity displayGroup : displayGroups) {
            for (DisplayGroupValidator validator : displayGroupValidators) {
                result.merge(validator.validate(displayGroup, displayGroups));
            }
        }

        if (!result.isValid()) {
            throw new ValidationException(result);
        }
        displayGroupRepository.saveAll(displayGroups);
    }

    private void validate(List<GenericLayoutEntity> genericLayouts) {
        ValidationResult result = new ValidationResult();
        for (GenericLayoutEntity genericLayoutEntity : genericLayouts) {
            for (GenericLayoutValidator validator : genericLayoutValidators) {
                result.merge(validator.validate(genericLayoutEntity));
            }
        }

        for (GenericLayoutEntity genericLayoutEntity : genericLayouts) {
            genericLayouts.stream()
                .filter(e -> e != genericLayoutEntity)
                .filter(e -> e.getCaseField().getReference().equals(genericLayoutEntity.getCaseField().getReference()))
                .filter(e -> e.getCaseType().getReference().equals(genericLayoutEntity.getCaseType().getReference()))
                .filter(e -> StringUtils.equals(e.getCaseFieldElementPath(), genericLayoutEntity.getCaseFieldElementPath()))
                .findFirst().ifPresent(e -> result.addError(error(e)));
        }

        if (!result.isValid()) {
            throw new ValidationException(result);
        }
    }

    private GenericLayoutEntityValidatorImpl.ValidationError error(GenericLayoutEntity entity) {
        return new GenericLayoutEntityValidatorImpl.ValidationError(
            String.format("Following have to be unique: [CaseTypeID '%s', CaseFieldID '%s', ListElementCode '%s'] with label '%s'",
                entity.getCaseType().getReference(),
                entity.getCaseField().getReference(),
                entity.getCaseFieldElementPath(),
                entity.getLabel()
            ), entity);
    }
}
