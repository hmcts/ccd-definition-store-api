package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;

@Component
public class CaseFieldEntityComplexFieldACLValidatorImpl implements CaseFieldEntityValidator {

    private static final Logger LOG = LoggerFactory.getLogger(CaseFieldEntityComplexFieldsValidatorImpl.class);

    @Override
    public ValidationResult validate(final CaseFieldEntity caseField,
                                     final CaseFieldEntityValidationContext caseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

        for (ComplexFieldACLEntity entity : caseField.getComplexFieldACLEntities()) {
            validateUserRole(caseField, caseFieldEntityValidationContext, validationResult, entity);
            validateCRUDAgainstCaseFieldParent(caseField, caseFieldEntityValidationContext, validationResult, entity);
            validateCRUDComplexParent(caseField, caseFieldEntityValidationContext, validationResult, entity);
        }
        return validationResult;
    }

    private void validateCRUDComplexParent(CaseFieldEntity caseField, CaseFieldEntityValidationContext caseFieldEntityValidationContext,
                                           ValidationResult validationResult, ComplexFieldACLEntity parentComplexFieldACLEntity) {
        String parentUserRole = parentComplexFieldACLEntity.getUserRole() != null ? parentComplexFieldACLEntity.getUserRole().getReference() : "";
        caseField.getComplexFieldACLEntities()
            .stream()
            .anyMatch(child -> {
                boolean match = (child.getUserRole() != null && child.getUserRole().getReference().equalsIgnoreCase(parentUserRole))
                    && isAChild(parentComplexFieldACLEntity.getListElementCode(), child.getListElementCode())
                    && parentComplexFieldACLEntity.hasLowerAccessThan(child);
                if (match) {
                    validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                        String.format("List element code '%s' has higher access than its parent '%s'", child.getListElementCode(), parentComplexFieldACLEntity.getListElementCode()),
                        child, new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
                    LOG.info("List element code '{}' has higher access than its parent '{}'", child.getListElementCode(), parentComplexFieldACLEntity.getListElementCode());
                }
                return match;
            });
        List<String> parentCodes = CaseFieldEntityUtil.parseParentCodes(parentComplexFieldACLEntity.getListElementCode());
        final List<String> missingCodes = parentCodes
            .stream()
            .filter(parentCode -> isMissingInComplexACLs(caseField.getComplexFieldACLEntities(), parentUserRole, parentCode))
            .collect(Collectors.toList());
        for (String code : missingCodes) {
            validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                String.format("Parent list element code '%s' is missing for list element code '%s'", code, parentComplexFieldACLEntity.getListElementCode()),
                parentComplexFieldACLEntity, new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
            LOG.info("Parent list element code '{}' is missing for list element code '{}'", code, parentComplexFieldACLEntity.getListElementCode());
        }
    }

    private boolean isAChild(String parentCode, String childCode) {
        return childCode.startsWith(parentCode + ".") && !childCode.equalsIgnoreCase(parentCode);
    }

    private boolean isMissingInComplexACLs(List<ComplexFieldACLEntity> complexFieldACLEntities, String userRole, String parentCode) {
        return complexFieldACLEntities.stream()
            .noneMatch(entity -> (entity.getUserRole() != null && entity.getUserRole().getReference().equalsIgnoreCase(userRole))
                && parentCode.equals(entity.getListElementCode())
            );
    }

    private void validateCRUDAgainstCaseFieldParent(CaseFieldEntity caseField, CaseFieldEntityValidationContext caseFieldEntityValidationContext,
                                                    ValidationResult validationResult, ComplexFieldACLEntity entity) {
        String userRole = entity.getUserRole() != null ? entity.getUserRole().getReference() : "";
        final Optional<CaseFieldACLEntity> caseFieldACLByRole = caseField.getCaseFieldACLByRole(userRole);
        if (caseFieldACLByRole.isPresent()) {
            if (caseFieldACLByRole.get().hasLowerAccessThan(entity)) {
                validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                    String.format("List element code '%s' has higher access than case field '%s'", entity.getListElementCode(), caseField.getReference()),
                    entity, new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
                LOG.info("List element code '{}' has higher access than case field '{}'", entity.getListElementCode(), caseField.getReference());
            }
        } else {
            validationResult.addError(new CaseFieldEntityComplexACLValidationError(
                String.format("Parent case field '%s' doesn't have any ACL defined for List element code '%s'", entity.getListElementCode(), caseField.getReference()),
                entity, new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
            LOG.info("Parent case field '{}' doesn't have any ACL defined for List element code '{}'", entity.getListElementCode(), caseField.getReference());
        }
    }

    private void validateUserRole(CaseFieldEntity caseField, CaseFieldEntityValidationContext caseFieldEntityValidationContext,
                                  ValidationResult validationResult, ComplexFieldACLEntity entity) {
        if (null == entity.getUserRole()) {
            validationResult.addError(new CaseFieldEntityInvalidUserRoleValidationError(entity,
                new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
        }
    }
}
