package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.AccessTypeRolesValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;

@Component
public class AccessTypeRolesParser {

    private final AccessTypeRolesValidator accessTypeRolesValidator;

    @Autowired
    public AccessTypeRolesParser(AccessTypeRolesValidator accessTypeRolesValidator) {
        this.accessTypeRolesValidator = accessTypeRolesValidator;
    }

    public List<AccessTypeEntity> parseAccessTypes(final Map<String, DefinitionSheet> definitionSheets,
                                            final ParseContext parseContext) {
        ValidationResult validationResult = new ValidationResult();
        try {

            final List<DefinitionDataItem> accessTypeRolesItems = definitionSheets
                .get(SheetName.ACCESS_TYPE_ROLES.getName())
                .getDataItems();

            final List<AccessTypeEntity> accessTypeEntities = accessTypeRolesItems
                .stream().map(accessTypeEntity ->
                    createAccessTypeEntity(parseContext, accessTypeEntity)
                ).collect(Collectors.toList());

            validationResult.merge(accessTypeRolesValidator
                .validateAccessTypes(accessTypeEntities));

            if (!validationResult.isValid()) {
                throw new InvalidImportException();
            }

            return accessTypeEntities;

        } catch (InvalidImportException invalidImportException) {
            String errorMessage = invalidImportException.getMessage();
            if (StringUtils.hasLength(errorMessage)) {
                validationResult.addError(new ValidationError(invalidImportException.getMessage()) {
                    @Override
                    public String toString() {
                        return getDefaultMessage();
                    }
                });
            }
            throw new ValidationException(validationResult);
        }
    }

    public List<AccessTypeRoleEntity> parseAccessTypeRoles(final Map<String, DefinitionSheet> definitionSheets,
                                        final ParseContext parseContext,
                                        final List<RoleToAccessProfilesEntity> accessProfileEntities) {
        ValidationResult validationResult = new ValidationResult();
        try {

            final List<DefinitionDataItem> accessTypeRolesItems = definitionSheets
                .get(SheetName.ACCESS_TYPE_ROLES.getName())
                .getDataItems();

            final List<AccessTypeRoleEntity> accessTypeRolesEntities = accessTypeRolesItems
                .stream().map(accessTypeRolesEntity ->
                    createAccessTypeRoleEntity(parseContext, accessTypeRolesEntity)
                ).collect(Collectors.toList());

            validationResult.merge(accessTypeRolesValidator
                .validateAccessTypeRoles(parseContext, accessTypeRolesEntities, accessProfileEntities));

            if (!validationResult.isValid()) {
                throw new InvalidImportException();
            }

            return accessTypeRolesEntities;

        } catch (InvalidImportException invalidImportException) {
            String errorMessage = invalidImportException.getMessage();
            if (StringUtils.hasLength(errorMessage)) {
                validationResult.addError(new ValidationError(invalidImportException.getMessage()) {
                    @Override
                    public String toString() {
                        return getDefaultMessage();
                    }
                });
            }
            throw new ValidationException(validationResult);
        }
    }

    private AccessTypeEntity createAccessTypeEntity(final ParseContext parseContext,
                                                    final DefinitionDataItem definitionDataItem) {
        AccessTypeEntity accessTypeEntity = new AccessTypeEntity();

        final String caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(caseType))
            .findAny()
            .orElseThrow(() -> {
                String message = String.format("Case Type not found %s in column '%s' in the sheet '%s'",
                    caseType, ColumnName.CASE_TYPE_ID, SheetName.ACCESS_TYPE_ROLES);
                throw new InvalidImportException(message);

            });
        accessTypeEntity.setCaseTypeId(caseTypeEntity);

        accessTypeEntity.setLiveFrom(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM));
        accessTypeEntity.setLiveTo(definitionDataItem.getLocalDate(ColumnName.LIVE_TO));
        accessTypeEntity.setCaseTypeId(caseTypeEntity);
        accessTypeEntity.setAccessTypeId(definitionDataItem.getString(ColumnName.ACCESS_TYPE_ID));
        accessTypeEntity.setOrganisationProfileId(definitionDataItem.getString(
            ColumnName.ORGANISATION_PROFILE_ID));
        accessTypeEntity.setAccessMandatory(
            definitionDataItem.getBooleanOrDefault(ColumnName.ACCESS_MANDATORY, false));
        accessTypeEntity.setAccessDefault(
            definitionDataItem.getBooleanOrDefault(ColumnName.ACCESS_DEFAULT, false));
        accessTypeEntity.setDisplay(definitionDataItem.getBooleanOrDefault(ColumnName.DISPLAY, false));
        accessTypeEntity.setDescription(definitionDataItem.getString(ColumnName.DESCRIPTION));
        accessTypeEntity.setHint(definitionDataItem.getString(ColumnName.HINT_TEXT));
        accessTypeEntity.setDisplayOrder(definitionDataItem.getInteger(ColumnName.DISPLAY_ORDER));

        return accessTypeEntity;
    }

    private AccessTypeRoleEntity createAccessTypeRoleEntity(final ParseContext parseContext,
                                                                 final DefinitionDataItem definitionDataItem) {
        AccessTypeRoleEntity accessTypeRoleEntity = new AccessTypeRoleEntity();

        final String caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(caseType))
            .findAny()
            .orElseThrow(() -> {
                String message = String.format("Case Type not found %s in column '%s' in the sheet '%s'",
                    caseType, ColumnName.CASE_TYPE_ID, SheetName.ACCESS_TYPE_ROLES);
                throw new InvalidImportException(message);

            });
        accessTypeRoleEntity.setCaseTypeId(caseTypeEntity);

        accessTypeRoleEntity.setLiveFrom(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM));
        accessTypeRoleEntity.setLiveTo(definitionDataItem.getLocalDate(ColumnName.LIVE_TO));
        accessTypeRoleEntity.setCaseTypeId(caseTypeEntity);
        accessTypeRoleEntity.setAccessTypeId(definitionDataItem.getString(ColumnName.ACCESS_TYPE_ID));
        accessTypeRoleEntity.setOrganisationProfileId(definitionDataItem.getString(
            ColumnName.ORGANISATION_PROFILE_ID));
        accessTypeRoleEntity.setOrganisationalRoleName(
            definitionDataItem.getString(ColumnName.ORGANISATION_ROLE_NAME));
        accessTypeRoleEntity.setGroupRoleName(definitionDataItem.getString(ColumnName.GROUP_ROLE_NAME));
        accessTypeRoleEntity.setCaseAssignedRoleField(
            definitionDataItem.getString(ColumnName.CASE_ASSIGNED_ROLE_FIELD));
        accessTypeRoleEntity.setGroupAccessEnabled(
            definitionDataItem.getBooleanOrDefault(ColumnName.GROUP_ACCESS_ENABLED, false));
        accessTypeRoleEntity.setCaseAccessGroupIdTemplate(
            definitionDataItem.getString(ColumnName.CASE_GROUP_ID_TEMPLATE));

        return accessTypeRoleEntity;
    }

}
