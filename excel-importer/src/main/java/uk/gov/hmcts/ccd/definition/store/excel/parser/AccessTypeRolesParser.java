package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

public class AccessTypeRolesParser {

    public List<AccessTypeRolesEntity> parse(final Map<String, DefinitionSheet> definitionSheets,
                                             final ParseContext parseContext) {
        try {
            final List<DefinitionDataItem> roleToAccessProfiles = definitionSheets
                .get(SheetName.ACCESS_TYPE_ROLES.getName())
                .getDataItems();
            return roleToAccessProfiles.stream()
                .map(roleToAccessProfile -> createRoleToAccessProfileEntity(parseContext, roleToAccessProfile))
                .collect(Collectors.toUnmodifiableList());
        } catch (InvalidImportException invalidImportException) {
            ValidationResult validationResult = new ValidationResult();
            validationResult.addError(new ValidationError(invalidImportException.getMessage()) {
                @Override
                public String toString() {
                    return getDefaultMessage();
                }
            });
            throw new ValidationException(validationResult);
        }
    }

    private AccessTypeRolesEntity createRoleToAccessProfileEntity(final ParseContext parseContext,
                                                                       final DefinitionDataItem definitionDataItem) {
        AccessTypeRolesEntity accessTypeRolesEntity = new AccessTypeRolesEntity();

        final String caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
        final CaseTypeEntity caseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(caseType))
            .findAny()
            .orElseThrow(() -> {
                String message = String.format("Case Type not found %s in column '%s' in the sheet '%s'",
                    caseType, ColumnName.CASE_TYPE_ID, SheetName.ROLE_TO_ACCESS_PROFILES);
                throw new InvalidImportException(message);

            });

        accessTypeRolesEntity.setLiveFrom(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM));
        accessTypeRolesEntity.setLiveTo(definitionDataItem.getLocalDate(ColumnName.LIVE_TO));
        accessTypeRolesEntity.setCaseTypeId(caseTypeEntity);
        accessTypeRolesEntity.setAccessTypeId(definitionDataItem.getString(ColumnName.ACCESS_TYPE_ID));
        accessTypeRolesEntity.setOrganisationProfileId(definitionDataItem.getString(ColumnName.ORGANISATION_PROFILE_ID));
        accessTypeRolesEntity.setAccessMandatory(definitionDataItem.getBoolean(ColumnName.ACCESS_MANDATORY));
        accessTypeRolesEntity.setAccessDefault(definitionDataItem.getBoolean(ColumnName.ACCESS_DEFAULT));
        accessTypeRolesEntity.setDisplay(definitionDataItem.getBoolean(ColumnName.DISPLAY));
        accessTypeRolesEntity.setDescription(definitionDataItem.getString(ColumnName.DESCRIPTION));
        accessTypeRolesEntity.setHint(definitionDataItem.getString(ColumnName.HINT_TEXT));
        accessTypeRolesEntity.setDisplayOrder(definitionDataItem.getInteger(ColumnName.DISPLAY_ORDER));
        accessTypeRolesEntity.setOrganisationalRoleName(definitionDataItem.getString(ColumnName.ORGANISATION_ROLE_NAME));
        accessTypeRolesEntity.setGroupRoleName(definitionDataItem.getString(ColumnName.GROUP_ROLE_NAME));
        accessTypeRolesEntity.setOrganisationPolicyField(definitionDataItem.getString(ColumnName.ORGANISATION_POLICY_FIELD));
        accessTypeRolesEntity.setGroupAccessEnabled(definitionDataItem.getBoolean(ColumnName.GROUP_ROLE_NAME));
        accessTypeRolesEntity.setCaseAccessGroupIdTemplate(definitionDataItem.getString(ColumnName.CASE_GROUP_ID_TEMPLATE));

        return accessTypeRolesEntity;
    }
}
