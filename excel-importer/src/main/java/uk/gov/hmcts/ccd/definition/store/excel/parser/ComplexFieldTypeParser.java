package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.field.FieldShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.SecurityClassificationColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;

/**
 * Parses Field types defined as part of tab `ComplexTypes`.
 * This includes Complex types themselves and the custom simple types they use.
 */
public class ComplexFieldTypeParser implements FieldShowConditionParser {
    private static final String INVALID_ORDER_COLUMN = "ComplexField with reference='%s' has incorrect order for nested fields. Order has to be incremental and start from 1";
    private static final Logger logger = LoggerFactory.getLogger(ComplexFieldTypeParser.class);

    private ParseContext parseContext;
    private ShowConditionParser showConditionParser;

    private final FieldTypeEntity complexBaseType;
    private final FieldTypeParser fieldTypeParser;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    public ComplexFieldTypeParser(ParseContext parseContext,
                                  FieldTypeParser fieldTypeParser,
                                  ShowConditionParser showConditionParser,
                                  EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry
    ) {
        this.parseContext = parseContext;
        this.showConditionParser = showConditionParser;
        this.fieldTypeParser = fieldTypeParser;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;

        complexBaseType = parseContext.getBaseType(BASE_COMPLEX).orElseThrow(() -> new SpreadsheetParsingException("No base type found for Complex field: " + BASE_COMPLEX));
    }

    /**
     * Parse all complex types defined as part of `ComplexTypes` tab.
     *
     * @return List of new types to be created.
     */
    public ParseResult<FieldTypeEntity> parse(Map<String, DefinitionSheet> definitionSheets) {
        logger.debug("Complex types parsing...");

        final Map<String, List<DefinitionDataItem>> complexTypesItems = definitionSheets.get(SheetName.COMPLEX_TYPES.getName()).groupDataItemsById();

        logger.debug("Complex types parsing: {} complex types detected", complexTypesItems.size());

        // TODO Check for already existing types with same identity
        ParseResult<FieldTypeEntity> result = complexTypesItems.entrySet().stream().map(this::parseComplexType).reduce(new ParseResult(), (res, complexTypeParseResult) -> res.add(complexTypeParseResult));

        logger.info("Complex types parsing: OK: {} types parsed, including {} complex", result.getAllResults().size(), complexTypesItems.size());

        return result;
    }

    private ParseResult<FieldTypeEntity> parseComplexType(Entry<String, List<DefinitionDataItem>> complexTypeItems) {
        logger.debug("Complex types parsing: parsing '{}'", complexTypeItems.getKey());


        final ParseResult<FieldTypeEntity> result = new ParseResult<>();

        final List<DefinitionDataItem> items = complexTypeItems.getValue();

        final List<ComplexFieldEntity> complexFields = items.stream()
                                                               .map(item -> parseComplexField(item, items.size(), result))
                                                               .collect(toList());

        final FieldTypeEntity complexType = new FieldTypeEntity();
        complexType.setReference(complexTypeItems.getKey());
        complexType.setBaseFieldType(complexBaseType);
        complexType.setJurisdiction(parseContext.getJurisdiction());
        complexType.addComplexFields(complexFields);

        result.addNew(complexType);
        parseContext.addToAllTypes(complexType);

        return result;
    }

    private ComplexFieldEntity parseComplexField(DefinitionDataItem definitionDataItem, int itemsCount, ParseResult<FieldTypeEntity> result) {
        final ComplexFieldEntity complexField = new ComplexFieldEntity();

        final String fieldId = definitionDataItem.getString(ColumnName.LIST_ELEMENT_CODE);

        final ParseResult.Entry<FieldTypeEntity> resultEntry = fieldTypeParser.parse(fieldId, definitionDataItem);

        complexField.setReference(fieldId);
        complexField.setFieldType(resultEntry.getValue());

        SecurityClassificationColumn securityClassificationColumn = definitionDataItem.getSecurityClassification();
        complexField.setSecurityClassification(securityClassificationColumn.getSecurityClassification());

        complexField.setLabel(definitionDataItem.getString(ColumnName.ELEMENT_LABEL));
        complexField.setHidden(definitionDataItem.getBoolean(ColumnName.DEFAULT_HIDDEN));
        complexField.setHint(definitionDataItem.getString(ColumnName.HINT_TEXT));
        Integer order = definitionDataItem.getInteger(ColumnName.DISPLAY_ORDER);
        if (isOrderNotWithinItemsCount(itemsCount, order)) {
            throw new MapperException(String.format(INVALID_ORDER_COLUMN, fieldId));
        }
        complexField.setOrder(order);
        complexField.setShowCondition(parseShowCondition(definitionDataItem.getString(ColumnName.FIELD_SHOW_CONDITION)));
        complexField.setDisplayContextParameter(definitionDataItem.getString(ColumnName.DISPLAY_CONTEXT_PARAMETER));

        this.entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(complexField, definitionDataItem);

        result.add(resultEntry);

        return complexField;
    }

    private boolean isOrderNotWithinItemsCount(int itemsCount, Integer order) {
        return order != null && order > itemsCount;
    }

    @Override
    public ShowConditionParser getShowConditionParser() {
        return showConditionParser;
    }
}
