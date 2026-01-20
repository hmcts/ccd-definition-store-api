package uk.gov.hmcts.ccd.definition.store.excel.parser;

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
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

@Component
public class ShellMappingParser {

    public List<ShellMappingEntity> parse(final Map<String, DefinitionSheet> definitionSheets,
                                          final ParseContext parseContext) {
        ValidationResult validationResult = new ValidationResult();
        try {

            final List<DefinitionDataItem> accessTypeRolesItems = definitionSheets
                .get(SheetName.SHELL_MAPPING.getName())
                .getDataItems();

            final List<ShellMappingEntity> shellMappingEntities = accessTypeRolesItems
                .stream().map(accessTypeEntity ->
                    createShellMappingEntity(parseContext, accessTypeEntity)
                ).toList();

            if (!validationResult.isValid()) {
                throw new InvalidImportException();
            }

            return shellMappingEntities;

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

    private ShellMappingEntity createShellMappingEntity(final ParseContext parseContext,
                                                    final DefinitionDataItem definitionDataItem) {
        ShellMappingEntity shellMappingEntity = new ShellMappingEntity();

        final String shellCaseType = definitionDataItem.getString(ColumnName.SHELL_CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(shellCaseType))
            .findAny()
            .orElseThrow(() -> new InvalidImportException(
                String.format("Case Type not found %s in column '%s' in the sheet '%s'",
                shellCaseType, ColumnName.SHELL_CASE_TYPE_ID, SheetName.SHELL_MAPPING)));
        shellMappingEntity.setShellCaseTypeId(toCaseTypeLiteEntity(caseTypeEntity));

        shellMappingEntity.setLiveFrom(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM));
        shellMappingEntity.setLiveTo(definitionDataItem.getLocalDate(ColumnName.LIVE_TO));
        String shellCaseFieldName = definitionDataItem.getString(ColumnName.SHELL_CASE_FIELD_NAME);

        CaseFieldEntity shellCaseFieldEntity = caseTypeEntity.getCaseFields()
            .stream()
            .filter(entity -> entity.getReference().equals(shellCaseFieldName))
            .findAny()
            .orElseThrow(() -> new InvalidImportException(
                String.format("Shell Case field not found %s in column '%s' in the sheet '%s'",
                shellCaseFieldName, ColumnName.SHELL_CASE_FIELD_NAME, SheetName.SHELL_MAPPING)));
        shellMappingEntity.setShellCaseFieldName(shellCaseFieldEntity);


        final String originatingCaseType = definitionDataItem.getString(ColumnName.ORIGINATING_CASE_TYPE_ID);
        CaseTypeEntity originatingCaseTypeEntity = parseContext.getCaseTypes()
            .stream()
            .filter(entity -> entity.getReference().equals(originatingCaseType))
            .findAny()
            .orElseThrow(() -> new InvalidImportException(
                String.format("Originating Case Type not found %s in column '%s' in the sheet '%s'",
                originatingCaseType, ColumnName.ORIGINATING_CASE_TYPE_ID, SheetName.SHELL_MAPPING)));
        shellMappingEntity.setOriginatingCaseTypeId(toCaseTypeLiteEntity(originatingCaseTypeEntity));

        String originatingCaseFieldName = definitionDataItem.getString(ColumnName.SHELL_CASE_FIELD_NAME);

        CaseFieldEntity originatingCaseFieldEntity = originatingCaseTypeEntity.getCaseFields()
            .stream()
            .filter(entity -> entity.getReference().equals(originatingCaseFieldName))
            .findAny()
            .orElseThrow(() -> new InvalidImportException(
                String.format("Originating Case field not found %s in column '%s' in the sheet '%s'",
                originatingCaseFieldName, ColumnName.ORIGINATING_CASE_FIELD_NAME, SheetName.SHELL_MAPPING)));
        shellMappingEntity.setOriginatingCaseFieldName(originatingCaseFieldEntity);

        return shellMappingEntity;
    }

}
