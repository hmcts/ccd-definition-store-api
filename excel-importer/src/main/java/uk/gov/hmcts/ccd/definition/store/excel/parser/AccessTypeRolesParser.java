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

import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

@Component
public class AccessTypeRolesParser {

    private final AccessTypeRolesValidator accessTypeRolesValidator;

    @Autowired
    public AccessTypeRolesParser(AccessTypeRolesValidator accessTypeRolesValidator) {
        this.accessTypeRolesValidator = accessTypeRolesValidator;
    }

    public List<AccessTypeRoleEntity> parse(final Map<String, DefinitionSheet> definitionSheets,
                                            final ParseContext parseContext,
                                            final List<AccessTypeEntity> accessTypeEntities,
                                            final List<RoleToAccessProfilesEntity> accessProfileEntities) {

        ValidationResult validationResult = new ValidationResult();
        try {

            final List<DefinitionDataItem> accessTypeRolesItems = definitionSheets
                .get(SheetName.ACCESS_TYPE_ROLE.getName())
                .getDataItems();

            final List<AccessTypeRoleEntity> accessTypeRoleEntities = accessTypeRolesItems
                .stream().map(accessTypeRolesEntity ->
                    createAccessTypeRoleEntity(parseContext, accessTypeRolesEntity)
                ).collect(Collectors.toList());

            validationResult.merge(accessTypeRolesValidator
                .validate(parseContext, accessTypeEntities, accessTypeRoleEntities, accessProfileEntities));

            if (!validationResult.isValid()) {
                throw new InvalidImportException();
            }

            return accessTypeRoleEntities;

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
                    caseType, ColumnName.CASE_TYPE_ID, SheetName.ACCESS_TYPE_ROLE);
                throw new InvalidImportException(message);

            });
        accessTypeRoleEntity.setCaseType(toCaseTypeLiteEntity(caseTypeEntity));

        accessTypeRoleEntity.setLiveFrom(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM));
        accessTypeRoleEntity.setLiveTo(definitionDataItem.getLocalDate(ColumnName.LIVE_TO));
        accessTypeRoleEntity.setCaseType(toCaseTypeLiteEntity(caseTypeEntity));
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
