package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CaseFieldEntityComplexFieldACLValidatorImpl implements CaseFieldEntityValidator {

    private static final Logger LOG = LoggerFactory.getLogger(CaseFieldEntityComplexFieldsValidatorImpl.class);

    @Override
    public ValidationResult validate(final CaseFieldEntity caseField,
                                     final CaseFieldEntityValidationContext caseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

        for (ComplexFieldACLEntity entity : caseField.getComplexFieldACLEntities()) {
            validateAccessProfile(caseField, caseFieldEntityValidationContext, validationResult, entity);
            validateCrudAgainstCaseFieldParent(caseField, caseFieldEntityValidationContext, validationResult, entity);
            validateCrudComplexParent(caseField, caseFieldEntityValidationContext, validationResult, entity);
            validatePredefinedComplexTypes(caseField, caseFieldEntityValidationContext, validationResult, entity);
        }
        return validationResult;
    }

    private void validatePredefinedComplexTypes(CaseFieldEntity caseField,
                                                CaseFieldEntityValidationContext caseFieldEntityValidationContext,
                                                ValidationResult validationResult, ComplexFieldACLEntity entity) {
        final String code = entity.getListElementCode();
        if (caseField.isPredefinedComplexType()) {
            validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                String.format("CaseField '%s' is a predefined complex type and list element code '%s' is not allowed",
                    caseField.getReference(), code), entity, new AuthorisationCaseFieldValidationContext(
                        caseField, caseFieldEntityValidationContext)));
            LOG.info("CaseField '{}' is a predefined complex type parent and list element code {} is not allowed",
                caseField.getReference(), code);
        }
        if (code.contains(".")) {
            final Optional<FieldEntity> optionalParent = caseField.findNestedElementByPath(
                code.substring(0, code.lastIndexOf('.')));
            if (optionalParent.isPresent()) {
                FieldEntity parent = optionalParent.get();
                if (parent.isPredefinedComplexType()) {
                    validationResult.addError(new CaseFieldEntityComplexACLValidationError(String.format(
                        "List element code '%s' refers to a predefined complex type parent '%s' and is not allowed",
                            code, parent.getFieldType().getReference()),
                        entity, new AuthorisationCaseFieldValidationContext(
                            caseField, caseFieldEntityValidationContext)));
                    LOG.info("List element code '{}' refers to a predefined complex type parent {} and is not allowed",
                        code, parent.getFieldType().getReference());
                }
            } else {
                validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                    String.format("List element code '%s' is not a valid reference...", code),
                    entity, new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
                LOG.info("List element code '{}' is not a valid reference...", code);
            }
        }
    }

    private void validateCrudComplexParent(CaseFieldEntity caseField,
                                           CaseFieldEntityValidationContext caseFieldEntityValidationContext,
                                           ValidationResult validationResult,
                                           ComplexFieldACLEntity parentComplexFieldACLEntity) {
        String parentAccessProfile = parentComplexFieldACLEntity.getAccessProfile() != null
            ? parentComplexFieldACLEntity.getAccessProfile().getReference() : "";
        
        for (ComplexFieldACLEntity child : caseField.getComplexFieldACLEntities()) {
            boolean match = (child.getAccessProfile() != null
                && child.getAccessProfile().getReference().equalsIgnoreCase(parentAccessProfile))
                && isAChild(parentComplexFieldACLEntity.getListElementCode(), child.getListElementCode())
                && parentComplexFieldACLEntity.hasLowerAccessThan(child);
            if (match) {
                validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                    String.format("List element code '%s' has higher access than its parent '%s'",
                        child.getListElementCode(), parentComplexFieldACLEntity.getListElementCode()),
                    child, new AuthorisationCaseFieldValidationContext(
                        caseField, caseFieldEntityValidationContext)));
                LOG.info("List element code '{}' has higher access than its parent '{}'",
                    child.getListElementCode(), parentComplexFieldACLEntity.getListElementCode());
                return;
            }
        }
        List<String> parentCodes = CaseFieldEntityUtil.parseParentCodes(
            parentComplexFieldACLEntity.getListElementCode());
        final List<String> missingCodes = parentCodes
            .stream()
            .filter(parentCode -> isMissingInComplexACLs(
                caseField.getComplexFieldACLEntities(), parentAccessProfile, parentCode))
            .collect(Collectors.toList());
        for (String code : missingCodes) {
            validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                String.format("Parent list element code '%s' is missing for list element code '%s'", code,
                    parentComplexFieldACLEntity.getListElementCode()),
                parentComplexFieldACLEntity, new AuthorisationCaseFieldValidationContext(
                    caseField, caseFieldEntityValidationContext)));
            LOG.info("Parent list element code '{}' is missing for list element code '{}'", code,
                parentComplexFieldACLEntity.getListElementCode());
        }
    }

    private boolean isAChild(String parentCode, String childCode) {
        return childCode.startsWith(parentCode + ".") && !childCode.equalsIgnoreCase(parentCode);
    }

    private boolean isMissingInComplexACLs(List<ComplexFieldACLEntity> complexFieldACLEntities,
                                           String accessProfile, String parentCode) {
        return complexFieldACLEntities.stream()
            .noneMatch(entity -> (entity.getAccessProfile() != null
                && entity.getAccessProfile().getReference().equalsIgnoreCase(accessProfile))
                && parentCode.equals(entity.getListElementCode())
            );
    }

    private void validateCrudAgainstCaseFieldParent(CaseFieldEntity caseField,
                                                    CaseFieldEntityValidationContext caseFieldEntityValidationContext,
                                                    ValidationResult validationResult, ComplexFieldACLEntity entity) {
        String accessProfile = entity.getAccessProfile() != null ? entity.getAccessProfile().getReference() : "";
        final Optional<CaseFieldACLEntity> caseFieldACLByAccessProfile
                = caseField.getCaseFieldACLByAccessProfile(accessProfile);
        if (caseFieldACLByAccessProfile.isPresent()) {
            if (caseFieldACLByAccessProfile.get().hasLowerAccessThan(entity)) {
                validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                    String.format("List element code '%s' has higher access than case field '%s'",
                        entity.getListElementCode(), caseField.getReference()),
                    entity, new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
                LOG.info("List element code '{}' has higher access than case field '{}'", entity.getListElementCode(),
                    caseField.getReference());
            }
        } else {
            validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                String.format("Parent case field '%s' doesn't have any ACL defined for List element code '%s'",
                    entity.getListElementCode(), caseField.getReference()),
                entity, new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
            LOG.info("Parent case field '{}' doesn't have any ACL defined for List element code '{}'",
                entity.getListElementCode(), caseField.getReference());
        }
    }

    private void validateAccessProfile(CaseFieldEntity caseField,
                                       CaseFieldEntityValidationContext caseFieldEntityValidationContext,
                                       ValidationResult validationResult, ComplexFieldACLEntity entity) {
        if (null == entity.getAccessProfile()) {
            validationResult.addError(new CaseFieldEntityInvalidAccessProfileValidationError(entity,
                new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
        }
    }
}
