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
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfileEntity;

public class RoleToAccessProfilesParser {

    public List<RoleToAccessProfileEntity> parse(Map<String, DefinitionSheet> definitionSheets,
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

    public RoleToAccessProfileEntity createRoleToAccessProfileEntity(ParseContext parseContext,
                                                                     DefinitionDataItem definitionDataItem) {
        RoleToAccessProfileEntity roleToAccessProfileEntity = new RoleToAccessProfileEntity();

        final String caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(caseType))
            .findAny()
            .orElseThrow(() -> new InvalidImportException("Case Type not found " + caseType));
        roleToAccessProfileEntity.setCaseType(caseTypeEntity);

        roleToAccessProfileEntity.setRoleName(definitionDataItem.getString(ColumnName.ROLE_NAME));
        roleToAccessProfileEntity.setLiveFrom(definitionDataItem.getDate(ColumnName.LIVE_FROM));
        roleToAccessProfileEntity.setLiveTo(definitionDataItem.getDate(ColumnName.LIVE_TO));
        roleToAccessProfileEntity.setReadOnly(definitionDataItem.getBooleanOrDefault(ColumnName.READ_ONLY, false));
        roleToAccessProfileEntity.setDisabled(definitionDataItem.getBooleanOrDefault(ColumnName.DISABLED, false));
        String accessProfiles = definitionDataItem.getString(ColumnName.ACCESS_PROFILES);
        roleToAccessProfileEntity.setAccessProfiles(accessProfiles);
        roleToAccessProfileEntity.setAuthorisation(definitionDataItem.getString(ColumnName.AUTHORISATION));

        return roleToAccessProfileEntity;
    }
}
