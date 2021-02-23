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
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;

public class RoleToAccessProfilesParser {

    public List<RoleToAccessProfilesEntity> parse(Map<String, DefinitionSheet> definitionSheets,
                                                  ParseContext parseContext) {
        try {
            final List<DefinitionDataItem> roleToAccessProfiles = definitionSheets
                .get(SheetName.ROLE_TO_ACCESS_PROFILES.getName()).getDataItems();
            return roleToAccessProfiles
                .stream()
                .map(questionItem ->
                    createRoleToAccessProfileEntity(parseContext, questionItem)).collect(Collectors.toList());
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

    public RoleToAccessProfilesEntity createRoleToAccessProfileEntity(ParseContext parseContext,
                                                                      DefinitionDataItem definitionDataItem) {
        RoleToAccessProfilesEntity roleToAccessProfilesEntity = new RoleToAccessProfilesEntity();

        final String caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(caseType))
            .findAny()
            .orElseThrow(() -> {
                String message = String.format("Case Type not found %s in column '%s' in the sheet '%s'",
                    caseType, ColumnName.CASE_TYPE_ID, SheetName.ROLE_TO_ACCESS_PROFILES);
                throw new InvalidImportException(message);

            });
        roleToAccessProfilesEntity.setCaseType(caseTypeEntity);

        roleToAccessProfilesEntity.setRoleName(definitionDataItem.getString(ColumnName.ROLE_NAME));
        roleToAccessProfilesEntity.setLiveFrom(definitionDataItem.getDate(ColumnName.LIVE_FROM));
        roleToAccessProfilesEntity.setLiveTo(definitionDataItem.getDate(ColumnName.LIVE_TO));
        roleToAccessProfilesEntity.setReadOnly(definitionDataItem.getBooleanOrDefault(ColumnName.READ_ONLY, false));
        roleToAccessProfilesEntity.setDisabled(definitionDataItem.getBooleanOrDefault(ColumnName.DISABLED, false));
        String accessProfiles = definitionDataItem.getString(ColumnName.ACCESS_PROFILES);
        roleToAccessProfilesEntity.setAccessProfiles(accessProfiles);
        roleToAccessProfilesEntity.setAuthorisation(definitionDataItem.getString(ColumnName.AUTHORISATION));

        return roleToAccessProfilesEntity;
    }
}
