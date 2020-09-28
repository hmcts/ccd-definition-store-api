package uk.gov.hmcts.ccd.definition.store.excel.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
public class SearchAliasFieldParser {

    private static final String NESTED_FIELD_SEPARATOR_REGEX = "\\.";
    private static final String COLLECTION_FIELD_VALUE = "value";

    private final ParseContext parseContext;

    public SearchAliasFieldParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    public List<SearchAliasFieldEntity> parseAll(Map<String, DefinitionSheet> definitionSheets,
                                                 CaseTypeEntity caseType) {
        Map<String, List<DefinitionDataItem>> searchAliasFieldsByCaseTypes = ofNullable(definitionSheets
            .get(SheetName.SEARCH_ALIAS.getName()))
            .map(DefinitionSheet::groupDataItemsByCaseType)
            .orElse(Collections.emptyMap());

        log.debug("Parsing search alias case fields for case type {}...", caseType.getReference());

        List<SearchAliasFieldEntity> searchAliasFields = ofNullable(searchAliasFieldsByCaseTypes
            .get(caseType.getReference()))
            .map(dataItems -> dataItems
                .stream()
                .map(dataItem -> parseSearchAliasField(dataItem, caseType))
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());

        log.info("Parsed {} search alias fields for case type {}", searchAliasFields.size(), caseType.getReference());

        return searchAliasFields;
    }

    private SearchAliasFieldEntity parseSearchAliasField(DefinitionDataItem dataItem, CaseTypeEntity caseType) {
        SearchAliasFieldEntity searchAliasField = new SearchAliasFieldEntity();

        searchAliasField.setCaseType(caseType);
        searchAliasField.setReference(dataItem.getString(ColumnName.SEARCH_ALIAS_ID));
        String caseFieldPath = dataItem.getString(ColumnName.CASE_FIELD_ID);
        searchAliasField.setCaseFieldPath(caseFieldPath);
        searchAliasField.setFieldType(deriveCaseFieldType(caseFieldPath, caseType));

        return searchAliasField;
    }

    private FieldTypeEntity deriveCaseFieldType(String caseFieldPath, CaseTypeEntity caseType) {
        if (StringUtils.isEmpty(caseFieldPath)) {
            return null;
        }
        List<String> fieldsInPath = parseCaseFieldPath(caseFieldPath);
        CaseFieldEntity caseField = parseContext.getCaseFieldForCaseType(
            caseType.getReference(), fieldsInPath.remove(0));
        if (caseField.isComplexFieldType()) {
            return deriveComplexFieldType(caseField.getFieldType().getReference(), fieldsInPath);
        } else if (caseField.isCollectionFieldType()) {
            return deriveCollectionFieldType(caseField, fieldsInPath);
        } else {
            return caseField.getBaseType();
        }
    }

    private FieldTypeEntity deriveCollectionFieldType(CaseFieldEntity caseField, List<String> fieldsInPath) {
        removeCollectionValuePlaceholder(fieldsInPath);
        if (caseField.isCollectionOfComplex()) {
            return deriveComplexFieldType(
                caseField.getFieldType().getCollectionFieldType().getReference(), fieldsInPath);
        } else {
            return parseContext.getBaseType(caseField.getFieldType().getCollectionFieldType().getReference())
                .orElse(null);
        }
    }

    private void removeCollectionValuePlaceholder(List<String> fieldsInPath) {
        if (!fieldsInPath.isEmpty() && fieldsInPath.get(0).equalsIgnoreCase(COLLECTION_FIELD_VALUE)) {
            fieldsInPath.remove(0);
        } else {
            throw new MapperException(String.format("%s: A collection case field ID must be suffixed with '.%s'",
                SheetName.SEARCH_ALIAS.getName(), COLLECTION_FIELD_VALUE));
        }
    }

    /**
     * Recursive method to get field type of the leaf field in an object notation e.g. Company.BusinessAddress.Postcode
     */
    private FieldTypeEntity deriveComplexFieldType(String complexFieldType, List<String> fields) {
        if (fields.isEmpty()) {
            return null;
        }
        String field = fields.remove(0);

        return parseContext.getType(complexFieldType)
            .map(complexType -> complexType.getComplexFields()
                .stream()
                .filter(complexField -> complexField.getReference().equalsIgnoreCase(field))
                .findFirst().map(complexField -> {
                    if (complexField.isComplexFieldType()) {
                        return deriveComplexFieldType(complexField.getFieldType().getReference(), fields);
                    }
                    return complexField.getBaseType();
                }).orElse(null))
            .orElse(null);
    }

    /**
     * e.g. input Company.BusinessAddress.Postcode would produce output ["Company", "BusinessAddress", "Postcode"]
     */
    private List<String> parseCaseFieldPath(String caseFieldPath) {
        return new LinkedList<>(Arrays.asList(caseFieldPath.split(NESTED_FIELD_SEPARATOR_REGEX)));
    }

}
