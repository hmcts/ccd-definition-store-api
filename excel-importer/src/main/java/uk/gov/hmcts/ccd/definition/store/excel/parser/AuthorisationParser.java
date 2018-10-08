package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

interface AuthorisationParser {

    default void parseUserRole(final Authorisation entity,
                               final DefinitionDataItem definition,
                               final ParseContext parseContext) {
        final String userRole = definition.getString(ColumnName.USER_ROLE);

        final Optional<UserRoleEntity> userRoleEntity = parseContext.getUserRole(userRole);
        if (userRoleEntity.isPresent()) {
            entity.setUserRole(userRoleEntity.get());
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

    default void validateCaseTypes(Map<String, DefinitionSheet> definitionSheets, Map<String, List<DefinitionDataItem>> dataItemMap) {
        final Map<String, List<DefinitionDataItem>> caseTypeItems = definitionSheets.get(CASE_TYPE.getName()).groupDataItemsById();
        final Optional<String> unknownCaseType = dataItemMap.keySet()
            .stream()
            .filter(typeName -> !caseTypeItems.containsKey(typeName))
            .findFirst();
        if (unknownCaseType.isPresent()) {
            throw new MapperException(String.format("Unknown Case Type '%s' in worksheet '%s'", unknownCaseType.get(), getSheetName()));
        }
    }

    default DefinitionSheet getDefinitionSheet(Map<String, DefinitionSheet> definitionSheets) {
        DefinitionSheet definitionSheet = definitionSheets.get(getSheetName());
        if (definitionSheet == null) {
            throw new MapperException(
                String.format("A definition must contain a %s sheet", getSheetName())
            );
        }
        return definitionSheet;
    }

    String getSheetName();
}
