package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Component
public class AccessTypeRolesValidator {

    List<String> roles;

    public ValidationResult validate(final ParseContext parseContext,
                                     final List<AccessTypeEntity> accessTypeEntities,
                                     final List<AccessTypeRoleEntity> accessTypeRolesEntities,
                                     final List<RoleToAccessProfilesEntity> accessProfileEntities) {

        ValidationResult validationResult = new ValidationResult();

        roles = accessProfileEntities.stream()
            .map(RoleToAccessProfilesEntity::getRoleName).toList();

        accessTypeRolesEntities
            .forEach(entity -> {
                validateRequiredFields(validationResult, entity, accessTypeEntities, accessTypeRolesEntities);
                validateRoleName(validationResult, entity);
                validateCaseAccessGroupIDTemplate(parseContext, validationResult, entity);
            });

        return validationResult;
    }

    private void validateRequiredFields(ValidationResult validationResult, AccessTypeRoleEntity entity,
                                        List<AccessTypeEntity> accessTypeEntities,
                                        List<AccessTypeRoleEntity> accessTypeRolesEntities) {

        if (!StringUtils.hasLength(entity.getAccessTypeId())) {
            validationResult.addError(new ValidationError(
                String.format("Access Type ID should not be null or empty in column '%s' in the sheet '%s'",
                    ColumnName.ACCESS_TYPE_ID, SheetName.ACCESS_TYPE_ROLE)) {
            });
        }

        if (!StringUtils.hasLength(entity.getOrganisationProfileId())) {
            validationResult.addError(new ValidationError(
                String.format("Organisation Profile ID should not be null or empty in column '%s' "
                    + "in the sheet '%s'", ColumnName.ORGANISATION_PROFILE_ID, SheetName.ACCESS_TYPE_ROLE)) {
            });
        }

        if (StringUtils.hasLength(entity.getAccessTypeId()) && StringUtils.hasLength(entity.getOrganisationProfileId())) {
            validateAccessType(validationResult, accessTypeEntities, accessTypeRolesEntities);
        }
    }

    private void validateAccessType(ValidationResult validationResult, List<AccessTypeEntity> accessTypeEntities,
                                    List<AccessTypeRoleEntity> accessTypeRolesEntities) {


        Map<Object, List<AccessTypeEntity>> uniqueRecords = accessTypeEntities.stream()
            .collect(groupingBy(accessTypeEntity -> new AccessTypeEntity.uniqueIdentifier(
                accessTypeEntity.getCaseTypeId().getReference(),
                accessTypeEntity.getCaseTypeId().getJurisdiction().getReference(),
                accessTypeEntity.getAccessTypeId(),
                accessTypeEntity.getOrganisationProfileId()
            )));



        /*find out if it existsss
        if (!roles.contains(columnValue)) {
            validationResult.addError(new ValidationError(
                String.format("'%s' in column '%s' in the sheet '%s' is not a listed '%s' in the sheet '%s'",
                    columnValue, columnName,
                    SheetName.ACCESS_TYPE_ROLE, ColumnName.ROLE_NAME, SheetName.ROLE_TO_ACCESS_PROFILES)) {
            });
        }*/

    }

    private void validateRoleName(ValidationResult validationResult, AccessTypeRoleEntity entity) {

        if (!StringUtils.hasLength(entity.getOrganisationalRoleName())
            && !StringUtils.hasLength(entity.getGroupRoleName())) {
            validationResult.addError(new ValidationError(
                String.format("Either '%s' or '%s' must be set in the sheet '%s'",
                    ColumnName.ORGANISATION_ROLE_NAME, ColumnName.GROUP_ROLE_NAME, SheetName.ACCESS_TYPE_ROLE)) {
            });
        }

        if (StringUtils.hasLength(entity.getGroupRoleName())) {

            validateAgainstRoleName(validationResult, entity.getGroupRoleName(), ColumnName.GROUP_ROLE_NAME);

            if (!entity.getGroupAccessEnabled()) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must be enabled if '%s' is set in the sheet '%s'",
                        ColumnName.GROUP_ACCESS_ENABLED, ColumnName.GROUP_ROLE_NAME, SheetName.ACCESS_TYPE_ROLE)) {
                });
            }

            validateCaseAssignedRoleField(validationResult, entity);
        }

        if (StringUtils.hasLength(entity.getOrganisationalRoleName())) {
            validateAgainstRoleName(validationResult, entity.getOrganisationalRoleName(),
                ColumnName.ORGANISATION_ROLE_NAME);
        }
    }

    private void validateCaseAccessGroupIDTemplate(ParseContext parseContext, ValidationResult validationResult,
                                                   AccessTypeRoleEntity entity) {

        if (!StringUtils.hasLength(entity.getCaseAccessGroupIdTemplate())
            && StringUtils.hasLength(entity.getGroupRoleName())) {
            validationResult.addError(new ValidationError(
                String.format("'%s' must be set if '%s' is not null in the sheet '%s'",
                    ColumnName.CASE_GROUP_ID_TEMPLATE, ColumnName.GROUP_ROLE_NAME, SheetName.ACCESS_TYPE_ROLE)) {
            });
        } else if (StringUtils.hasLength(entity.getCaseAccessGroupIdTemplate())) {

            String jurisdiction = parseContext.getJurisdiction().getReference();

            if (!entity.getCaseAccessGroupIdTemplate().startsWith(jurisdiction + ":")) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must start with '%s' (Service Name) in column '%s' in the sheet '%s'",
                        entity.getCaseAccessGroupIdTemplate(), jurisdiction, ColumnName.CASE_GROUP_ID_TEMPLATE,
                        SheetName.ACCESS_TYPE_ROLE)) {
                });
            }

            if (!entity.getCaseAccessGroupIdTemplate().endsWith(":$ORGID$")) {
                validationResult.addError(new ValidationError(
                    String.format("'%s' must end with $ORGID$ column '%s' in the sheet '%s'",
                        entity.getCaseAccessGroupIdTemplate(), ColumnName.CASE_GROUP_ID_TEMPLATE,
                        SheetName.ACCESS_TYPE_ROLE)) {
                });
            }
        }
    }

    private boolean alreadyReportedError(ValidationResult validationResult, String errorMessage) {
        return validationResult.getValidationErrors().stream()
            .map(ValidationError::getDefaultMessage).anyMatch(errorMessage::contains);
    }

    private void validateCaseAssignedRoleField(ValidationResult validationResult,
                                               AccessTypeRoleEntity entity) {

        if (!StringUtils.hasLength(entity.getCaseAssignedRoleField())) {
            validationResult.addError(new ValidationError(
                String.format("'%s' must be set if '%s' is not null in the sheet '%s'",
                    ColumnName.CASE_ASSIGNED_ROLE_FIELD, ColumnName.GROUP_ROLE_NAME, SheetName.ACCESS_TYPE_ROLE)) {
            });
        } else {
            validateAgainstRoleName(validationResult, entity.getCaseAssignedRoleField(),
                ColumnName.CASE_ASSIGNED_ROLE_FIELD);
        }
    }

    private void validateAgainstRoleName(ValidationResult validationResult, String columnValue, ColumnName columnName) {
        if (!roles.contains(columnValue)) {
            validationResult.addError(new ValidationError(
                String.format("'%s' in column '%s' in the sheet '%s' is not a listed '%s' in the sheet '%s'",
                    columnValue, columnName,
                    SheetName.ACCESS_TYPE_ROLE, ColumnName.ROLE_NAME, SheetName.ROLE_TO_ACCESS_PROFILES)) {
            });
        }
    }
}
