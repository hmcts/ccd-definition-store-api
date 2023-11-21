package uk.gov.hmcts.ccd.definition.store.excel.validation;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;

@Component
public class AccessTypeRolesValidator {

    public ValidationResult validate(final ParseContext parseContext,
                                     final List<AccessTypeRolesEntity> accessTypeRolesEntities) {

        ValidationResult validationResult = new ValidationResult();

        accessTypeRolesEntities.stream()
            .forEach(entity -> {
                validateRequiredFields(validationResult, entity, accessTypeRolesEntities);
                validateDisplay(validationResult, entity, accessTypeRolesEntities);
                validateRoleName(validationResult, entity);
                validateCaseAccessGroupIDTemplate(parseContext, validationResult, entity);
            });

        return validationResult;
    }

    private void validateRequiredFields(ValidationResult validationResult, AccessTypeRolesEntity entity,
                                        List<AccessTypeRolesEntity> accessTypeRolesEntities) {

        if (!StringUtils.hasLength(entity.getAccessTypeId())) {
            validationResult.addError(new ValidationError(
                String.format("Access Type ID should not be null or empty in column '%s' in the sheet '%s'",
                    ColumnName.ACCESS_TYPE_ID, SheetName.ACCESS_TYPE_ROLES)) {
            });
        } else {
            validateAccessTypeId(validationResult, accessTypeRolesEntities);
        }

        if (!StringUtils.hasLength(entity.getOrganisationProfileId())) {
            validationResult.addError(new ValidationError(
                String.format("Organisation Profile ID should not be null or empty in column '%s' "
                    + "in the sheet '%s'", ColumnName.ORGANISATION_PROFILE_ID, SheetName.ACCESS_TYPE_ROLES)) {
            });
        }
    }

    private void validateAccessTypeId(ValidationResult validationResult,
                                      List<AccessTypeRolesEntity> accessTypeRolesEntities) {

        Map<Triple<CaseTypeEntity, Integer, String>, List<AccessTypeRolesEntity>> accessTypeRolesAccessTypeId =
            accessTypeRolesEntities
                .stream()
                .collect(groupingBy(p ->
                    Triple.of(p.getCaseTypeId(),
                        p.getCaseTypeId().getJurisdiction().getId(),
                        p.getAccessTypeId())));

        accessTypeRolesAccessTypeId.keySet()
            .forEach(triple -> {
                if (accessTypeRolesAccessTypeId.get(triple).size() > 1) {
                    validationResult.addError(new ValidationError(
                        String.format("'%s' must be unique within the Jurisdiction in the sheet '%s'",
                            ColumnName.ACCESS_TYPE_ID, SheetName.ACCESS_TYPE_ROLES)) {
                    });
                }
            });

    }

    private void validateDisplay(ValidationResult validationResult, AccessTypeRolesEntity entity,
                                 List<AccessTypeRolesEntity> accessTypeRolesEntities) {

        if (entity.getDisplay()) {
            if (!entity.getAccessMandatory() || !entity.getAccessDefault()) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' and '%s' must be set to true for '%s' to be used in the sheet '%s'",
                        ColumnName.ACCESS_MANDATORY, ColumnName.ACCESS_DEFAULT,
                        ColumnName.DISPLAY, SheetName.ACCESS_TYPE_ROLES)) {
                });
            }

            if (!StringUtils.hasLength(entity.getDescription())) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must be set for '%s' to be used in the sheet '%s'",
                        ColumnName.DESCRIPTION, ColumnName.DISPLAY, SheetName.ACCESS_TYPE_ROLES)) {
                });
            }

            if (!StringUtils.hasLength(entity.getHint())) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must be set for '%s' to be used in the sheet '%s'",
                        ColumnName.HINT_TEXT, ColumnName.DISPLAY, SheetName.ACCESS_TYPE_ROLES)) {
                });
            }

            if (entity.getDisplayOrder() == null) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' should not be null or empty for '%s' to be used in column '%s' "
                            + "in the sheet '%s'", ColumnName.DISPLAY_ORDER, ColumnName.DISPLAY,
                        ColumnName.DISPLAY_ORDER, SheetName.ACCESS_TYPE_ROLES)) {
                });

            } else if (entity.getDisplayOrder() < 1) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must be greater than 0 in column '%s' in the sheet '%s'",
                        ColumnName.DISPLAY_ORDER, ColumnName.DISPLAY_ORDER, SheetName.ACCESS_TYPE_ROLES)) {
                });
            } else {
                validateDisplayOrder(validationResult, accessTypeRolesEntities);
            }
        }
    }

    private void validateDisplayOrder(ValidationResult validationResult,
                                      List<AccessTypeRolesEntity> accessTypeRolesEntities) {
        Map<Triple<CaseTypeEntity, Integer, Integer>, List<AccessTypeRolesEntity>> accessTypeRolesDisplayOrder =
            accessTypeRolesEntities
                .stream()
                .collect(groupingBy(p ->
                    Triple.of(p.getCaseTypeId(),
                        p.getCaseTypeId().getJurisdiction().getId(),
                        p.getDisplayOrder())));

        accessTypeRolesDisplayOrder.keySet()
            .forEach(triple -> {
                if (accessTypeRolesDisplayOrder.get(triple).size() > 1) {
                    validationResult.addError(new ValidationError(
                        String.format("'%s' must be unique across all Case Types for "
                                + "a given Jurisdiction in the sheet '%s'",
                            ColumnName.DISPLAY_ORDER, SheetName.ACCESS_TYPE_ROLES)) {
                    });
                }
            });
    }

    private void validateRoleName(ValidationResult validationResult, AccessTypeRolesEntity entity) {

        if (!StringUtils.hasLength(entity.getOrganisationalRoleName())
            && !StringUtils.hasLength(entity.getGroupRoleName())) {
            validationResult.addError(new ValidationError(
                String.format("Either '%s' or '%s' must be set in the sheet '%s'",
                    ColumnName.ORGANISATION_ROLE_NAME, ColumnName.GROUP_ROLE_NAME, SheetName.ACCESS_TYPE_ROLES)) {
            });
        }

        if (StringUtils.hasLength(entity.getOrganisationalRoleName())
            && StringUtils.hasLength(entity.getGroupRoleName())) {
            validationResult.addError(new ValidationError(
                String.format("You cannot set both '%s' or '%s' in the sheet '%s'",
                    ColumnName.ORGANISATION_ROLE_NAME, ColumnName.GROUP_ROLE_NAME, SheetName.ACCESS_TYPE_ROLES)) {
            });
        }

        if (StringUtils.hasLength(entity.getGroupRoleName())) {
            validateOrgPolicyField(validationResult, entity);
        }
    }

    private void validateCaseAccessGroupIDTemplate(ParseContext parseContext, ValidationResult validationResult,
                                                   AccessTypeRolesEntity entity) {

        if (!StringUtils.hasLength(entity.getCaseAccessGroupIdTemplate())
            && StringUtils.hasLength(entity.getGroupRoleName())) {
            validationResult.addError(new ValidationError(
                String.format("'%s' must be set if '%s' is not null in the sheet '%s'",
                    ColumnName.CASE_GROUP_ID_TEMPLATE, ColumnName.GROUP_ROLE_NAME, SheetName.ACCESS_TYPE_ROLES)) {
            });
        } else if (StringUtils.hasLength(entity.getCaseAccessGroupIdTemplate())) {

            if (!entity.getCaseAccessGroupIdTemplate().startsWith(parseContext.getJurisdiction().getReference())) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must start with Service Name in column '%s' in the sheet '%s'",
                        entity.getCaseAccessGroupIdTemplate(), ColumnName.CASE_GROUP_ID_TEMPLATE,
                        SheetName.ACCESS_TYPE_ROLES)) {
                });
            }

            if (!entity.getCaseAccessGroupIdTemplate().endsWith(":$ORGID$")) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must end with $ORGID$ column '%s' in the sheet '%s'",
                        entity.getCaseAccessGroupIdTemplate(), ColumnName.CASE_GROUP_ID_TEMPLATE,
                        SheetName.ACCESS_TYPE_ROLES)) {
                });
            }
        }
    }

    private void validateOrgPolicyField(ValidationResult validationResult,
                                        AccessTypeRolesEntity entity) {

        if (!StringUtils.hasLength(entity.getOrganisationPolicyField())) {
            validationResult.addError(new ValidationError(
                String.format("'%s' must be set if '%s' is not null in the sheet '%s'",
                    ColumnName.ORGANISATION_POLICY_FIELD, ColumnName.GROUP_ROLE_NAME, SheetName.ACCESS_TYPE_ROLES)) {
            });
        } else {

            List<CaseFieldEntity> caseFieldEntities = entity.getCaseTypeId().getCaseFields()
                .stream()
                .filter(fieldType -> Objects.equals(fieldType.getReference(),
                    entity.getOrganisationPolicyField()))
                .toList();

            if (caseFieldEntities.size() != 1
                || !Objects.equals(caseFieldEntities.get(0).getFieldType().getReference(), "OrganisationPolicy")) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' is not a field with the type OrganisationPolicy "
                            + "in column '%s' in the sheet '%s'",
                        entity.getOrganisationPolicyField(), ColumnName.ORGANISATION_POLICY_FIELD,
                        SheetName.ACCESS_TYPE_ROLES)) {
                });
            }
        }
    }
}
