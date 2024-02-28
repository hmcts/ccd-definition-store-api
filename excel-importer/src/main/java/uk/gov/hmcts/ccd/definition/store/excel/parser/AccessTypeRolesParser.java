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
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;

import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

@Component
public class AccessTypeRolesParser {

    private final AccessTypeRolesValidator accessTypeRolesValidator;

    @Autowired
    public AccessTypeRolesParser(AccessTypeRolesValidator accessTypeRolesValidator) {
        this.accessTypeRolesValidator = accessTypeRolesValidator;
    }

    public List<AccessTypeRolesEntity> parse(final Map<String, DefinitionSheet> definitionSheets,
                                             final ParseContext parseContext,
                                             final List<RoleToAccessProfilesEntity> accessProfileEntities) {
        ValidationResult validationResult = new ValidationResult();
        try {

            final List<DefinitionDataItem> accessTypeRolesItems = definitionSheets
                .get(SheetName.ACCESS_TYPE_ROLES.getName())
                .getDataItems();
            final List<AccessTypeRolesEntity> accessTypeRolesEntities = accessTypeRolesItems
                .stream().map(accessTypeRolesEntity ->
                        createRoleToAccessProfileEntity(parseContext, accessTypeRolesEntity)
                ).collect(Collectors.toList());

            validationResult.merge(accessTypeRolesValidator
                .validate(parseContext, accessTypeRolesEntities, accessProfileEntities));

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

    private AccessTypeRolesEntity createRoleToAccessProfileEntity(final ParseContext parseContext,
                                                                  final DefinitionDataItem definitionDataItem) {
        AccessTypeRolesEntity accessTypeRolesEntity = new AccessTypeRolesEntity();

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
        accessTypeRolesEntity.setCaseType(toCaseTypeLiteEntity(caseTypeEntity));

        accessTypeRolesEntity.setLiveFrom(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM));
        accessTypeRolesEntity.setLiveTo(definitionDataItem.getLocalDate(ColumnName.LIVE_TO));
        accessTypeRolesEntity.setAccessTypeId(definitionDataItem.getString(ColumnName.ACCESS_TYPE_ID));
        accessTypeRolesEntity.setOrganisationProfileId(definitionDataItem.getString(
            ColumnName.ORGANISATION_PROFILE_ID));
        accessTypeRolesEntity.setAccessMandatory(
            definitionDataItem.getBooleanOrDefault(ColumnName.ACCESS_MANDATORY, false));
        accessTypeRolesEntity.setAccessDefault(
            definitionDataItem.getBooleanOrDefault(ColumnName.ACCESS_DEFAULT, false));
        accessTypeRolesEntity.setDisplay(definitionDataItem.getBooleanOrDefault(ColumnName.DISPLAY, false));
        accessTypeRolesEntity.setDescription(definitionDataItem.getString(ColumnName.DESCRIPTION));
        accessTypeRolesEntity.setHint(definitionDataItem.getString(ColumnName.HINT_TEXT));
        accessTypeRolesEntity.setDisplayOrder(definitionDataItem.getInteger(ColumnName.DISPLAY_ORDER));
        accessTypeRolesEntity.setOrganisationalRoleName(
            definitionDataItem.getString(ColumnName.ORGANISATION_ROLE_NAME));
        accessTypeRolesEntity.setGroupRoleName(definitionDataItem.getString(ColumnName.GROUP_ROLE_NAME));
        accessTypeRolesEntity.setCaseAssignedRoleField(
            definitionDataItem.getString(ColumnName.CASE_ASSIGNED_ROLE_FIELD));
        accessTypeRolesEntity.setGroupAccessEnabled(
            definitionDataItem.getBooleanOrDefault(ColumnName.GROUP_ACCESS_ENABLED, false));
        accessTypeRolesEntity.setCaseAccessGroupIdTemplate(
            definitionDataItem.getString(ColumnName.CASE_GROUP_ID_TEMPLATE));

        return accessTypeRolesEntity;
    }
}
