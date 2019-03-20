package uk.gov.hmcts.ccd.definition.store.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutValidator;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.GenericLayoutRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.write.repository.DefinitionWriteRepository;

@Component
public class LayoutServiceImpl implements LayoutService {
    private final List<GenericLayoutValidator> genericLayoutValidators;
    private final List<DisplayGroupValidator> displayGroupValidators;
    private DefinitionWriteRepository definitionWriteRepository;

    @Autowired
    public LayoutServiceImpl(List<GenericLayoutValidator> genericLayoutValidators,
                             List<DisplayGroupValidator> displayGroupValidators,
                             DefinitionWriteRepository definitionWriteRepository) {
        this.genericLayoutValidators = genericLayoutValidators;
        this.displayGroupValidators = displayGroupValidators;
        this.definitionWriteRepository = definitionWriteRepository;
    }

    @Override
    public void createGenerics(List<GenericLayoutEntity> genericLayouts) {
        ValidationResult result = new ValidationResult();
        for (GenericLayoutEntity genericLayoutEntity : genericLayouts) {
            for (GenericLayoutValidator validator : genericLayoutValidators) {
                result.merge(validator.validate(genericLayoutEntity));
            }
        }
        if (!result.isValid()) {
            throw new ValidationException(result);
        }
        definitionWriteRepository.save(genericLayouts);
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
        definitionWriteRepository.save(displayGroups);
    }
}
