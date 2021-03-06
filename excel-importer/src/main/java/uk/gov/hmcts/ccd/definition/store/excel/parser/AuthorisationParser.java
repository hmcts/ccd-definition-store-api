package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

interface AuthorisationParser {

    default void parseAccessProfile(final Authorisation entity,
                                    final DefinitionDataItem definition,
                                    final ParseContext parseContext) {
        final String accessProfile = definition.getString(ColumnName.ACCESS_PROFILE);
        final String caseType = definition.getString(ColumnName.CASE_TYPE_ID);

        entity.setAccessProfileId(accessProfile);
        Optional<AccessProfileEntity> accessProfileEntity = parseContext.getAccessProfile(caseType, accessProfile);
        if (accessProfileEntity.isPresent()) {
            entity.setAccessProfile(accessProfileEntity.get());
        } else {
            parseContext.addMissingAccessProfile(accessProfile);
        }
    }

    default void parseCrud(final Authorisation entity, final DefinitionDataItem definition) {
        final String crudString = definition.getString(ColumnName.CRUD);
        entity.setCrudAsString(crudString);

        final String crudUpperCase = StringUtils.defaultString(crudString).toUpperCase();
        entity.setCreate(crudUpperCase.contains("C"));
        entity.setRead(crudUpperCase.contains("R"));
        entity.setUpdate(crudUpperCase.contains("U"));
        entity.setDelete(crudUpperCase.contains("D"));
    }

    default void validateCaseTypes(Map<String, DefinitionSheet> definitionSheets,
                                   Map<String, List<DefinitionDataItem>> dataItemMap) {
        final Map<String, List<DefinitionDataItem>> caseTypeItems = definitionSheets
            .get(CASE_TYPE.getName()).groupDataItemsById();
        final Optional<String> unknownCaseType = dataItemMap.keySet()
            .stream()
            .filter(typeName -> !caseTypeItems.containsKey(typeName))
            .findFirst();
        if (unknownCaseType.isPresent()) {
            throw new MapperException(String.format(
                "Unknown Case Type '%s' in worksheet '%s'", unknownCaseType.get(), getSheetName()));
        }
    }

    default DefinitionSheet getDefinitionSheet(Map<String, DefinitionSheet> definitionSheets) {
        if (definitionSheets == null || definitionSheets.isEmpty()) {
            throw new MapperException("A definition must contain a sheet");
        }

        DefinitionSheet definitionSheet = definitionSheets.get(getSheetName());
        if (definitionSheet == null) {
            throw new MapperException(
                String.format("A definition must contain a '%s' sheet", getSheetName())
            );
        }
        return definitionSheet;
    }

    default void validateCaseFields(Map<String, DefinitionSheet> definitionSheets,
                                    DefinitionSheet definitionSheet,
                                    String caseTypeReference) {
        final Map<String, List<DefinitionDataItem>> casefieldsWithAuthorisationInfoThisCaseType
            = definitionSheet.getDataItems()
            .stream()
            .filter(definitionDataItem ->
                definitionDataItem.getString(ColumnName.CASE_TYPE_ID).equalsIgnoreCase(caseTypeReference))
            .collect(groupingBy(dataItem -> dataItem.getString(ColumnName.CASE_FIELD_ID)));

        final List<String> caseFieldItemsForThisCaseType = definitionSheets.get(CASE_FIELD.getName())
            .groupDataItemsByCaseType()
            .get(caseTypeReference)
            .stream()
            .map(definitionDataItem -> definitionDataItem.getString(ColumnName.ID)).collect(Collectors.toList());

        final Optional<String> unknownCaseFieldId = casefieldsWithAuthorisationInfoThisCaseType.keySet()
            .stream()
            .filter(typeName -> !caseFieldItemsForThisCaseType.contains(typeName))
            .findFirst();
        if (unknownCaseFieldId.isPresent()) {
            throw new MapperException(String.format("Unknown CaseField '%s' for CaseType '%s' in worksheet '%s'",
                unknownCaseFieldId.get(), caseTypeReference, getSheetName()));
        }
    }

    String getSheetName();
}
