package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.FieldTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.CaseFieldComplexFieldEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;

import static uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.CaseFieldComplexFieldEntityValidator.ValidationContext;

@Component
public class CaseFieldEntityComplexFieldsValidatorImpl implements CaseFieldEntityValidator {

    private List<CaseFieldComplexFieldEntityValidator> complexFieldEntityValidators;
    private List<FieldTypeEntity> preDefinedComplexTypes;

    @Autowired
    public CaseFieldEntityComplexFieldsValidatorImpl(
        List<CaseFieldComplexFieldEntityValidator> complexFieldEntityValidators, FieldTypeService fieldTypeService) {
        this.complexFieldEntityValidators = complexFieldEntityValidators;
        this.preDefinedComplexTypes = fieldTypeService.getPredefinedComplexTypes();
    }

    @Override
    public ValidationResult validate(CaseFieldEntity caseField,
                                     CaseFieldEntityValidationContext caseFieldEntityValidationContext) {

        ValidationResult validationResult = new ValidationResult();

        for (CaseFieldComplexFieldEntityValidator complexFieldEntityValidator : this.complexFieldEntityValidators) {
            for (ComplexFieldEntity complexFieldEntity : caseField.getFieldType().getComplexFields()) {
                validationResult.merge(
                    complexFieldEntityValidator.validate(
                        complexFieldEntity,
                        new ValidationContext(caseFieldEntityValidationContext, caseField, this.preDefinedComplexTypes)
                    )
                );
            }
        }

        return validationResult;
    }
}
