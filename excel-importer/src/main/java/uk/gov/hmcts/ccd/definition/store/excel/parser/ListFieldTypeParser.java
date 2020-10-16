package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.ReferenceUtils;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Parses Field types defined as part of tab `FixedLists`.
 * Because the spreadsheet doesn't specify if a list is intended to be used as a fixed list type or radio list type or
 * multi-select list type, at the moment both version of the type will be created.
 */
public class ListFieldTypeParser {

    private static final Logger logger = LoggerFactory.getLogger(ListFieldTypeParser.class);

    private static final String FIXED_LIST_TYPE = "FixedList";
    private static final String FIXED_RADIO_LIST_TYPE = "FixedRadioList";
    private static final String MULTI_LIST_TYPE = "MultiSelectList";
    public static final String NO_BASE_TYPE_FOUND = "No base type found for: ";
    private final ParseContext parseContext;
    private final FieldTypeEntity fixedListBaseType;
    private final FieldTypeEntity fixedRadioListBaseType;
    private final FieldTypeEntity multiListBaseType;
    private final SpreadsheetValidator spreadsheetValidator;

    public ListFieldTypeParser(ParseContext parseContext, final SpreadsheetValidator spreadsheetValidator) {
        this.parseContext = parseContext;
        fixedListBaseType = parseContext.getBaseType(FIXED_LIST_TYPE).orElseThrow(() ->
            new InvalidImportException(NO_BASE_TYPE_FOUND + FIXED_LIST_TYPE));
        fixedRadioListBaseType = parseContext.getBaseType(FIXED_RADIO_LIST_TYPE).orElseThrow(() ->
            new InvalidImportException(NO_BASE_TYPE_FOUND + FIXED_RADIO_LIST_TYPE));
        multiListBaseType = parseContext.getBaseType(MULTI_LIST_TYPE).orElseThrow(() ->
            new InvalidImportException(NO_BASE_TYPE_FOUND + MULTI_LIST_TYPE));
        this.spreadsheetValidator = spreadsheetValidator;

    }

    /**
     * Extract list types from `FixedLists` tab.
     * Because the intent of the list is currently unknown, each list is extracted once as `FixedList` ,
     * once as 'FixedRadioList' and once as `MultiSelectList`.
     *
     * @param definitionSheets - definition sheet
     */
    public ParseResult<FieldTypeEntity> parse(Map<String, DefinitionSheet> definitionSheets) {
        logger.debug("List types parsing...");

        final Map<String, List<DefinitionDataItem>> fixedListsDataItems = definitionSheets
            .get(SheetName.FIXED_LISTS.getName()).groupDataItemsById();

        logger.debug("List types parsing: {} list types detected", fixedListsDataItems.size());

        // TODO Check for already existing types with same identity
        ParseResult<FieldTypeEntity> result = fixedListsDataItems
            .entrySet()
            .stream()
            .map(this::parseListType)
            .reduce(new ParseResult(), (res, listTypeParseResult) -> res.add(listTypeParseResult));

        logger.info("List types parsing: OK: {} types parsed", result.getAllResults().size());

        return result;
    }

    private ParseResult<FieldTypeEntity> parseListType(Map.Entry<String, List<DefinitionDataItem>> listDataItems) {
        final ParseResult<FieldTypeEntity> result = new ParseResult<>();

        final List<DefinitionDataItem> elements = listDataItems.getValue();

        // Add as FixedList
        final List<FieldTypeListItemEntity> fixedListItems = elements.stream()
            .map(this::parseListItem)
            .collect(toList());

        final FieldTypeEntity fixedListType = new FieldTypeEntity();
        fixedListType.setBaseFieldType(fixedListBaseType);
        String fixedListReference = ReferenceUtils.listReference(FIXED_LIST_TYPE, listDataItems.getKey());
        spreadsheetValidator.validate(
            SheetName.FIXED_LISTS.getName(), "ID", fixedListReference, FIXED_RADIO_LIST_TYPE);
        fixedListType.setReference(fixedListReference);
        fixedListType.setJurisdiction(parseContext.getJurisdiction());
        fixedListType.addListItems(fixedListItems);
        parseContext.addToAllTypes(fixedListType);
        result.addNew(fixedListType);

        // Add as FixedRadioList
        final List<FieldTypeListItemEntity> fixedRadioListItems = elements.stream()
            .map(this::parseListItem)
            .collect(toList());

        final FieldTypeEntity fixedRadioListType = new FieldTypeEntity();
        fixedRadioListType.setBaseFieldType(fixedRadioListBaseType);
        String fixedRadioListReference = ReferenceUtils.listReference(FIXED_RADIO_LIST_TYPE, listDataItems.getKey());
        spreadsheetValidator.validate(SheetName.FIXED_LISTS.getName(), "ID",
            fixedRadioListReference, FIXED_RADIO_LIST_TYPE);
        fixedRadioListType.setReference(fixedRadioListReference);
        fixedRadioListType.setJurisdiction(parseContext.getJurisdiction());
        fixedRadioListType.addListItems(fixedRadioListItems);
        parseContext.addToAllTypes(fixedRadioListType);
        result.addNew(fixedRadioListType);

        // Add as MultiSelectList
        final List<FieldTypeListItemEntity> multiListItems = elements.stream()
            .map(this::parseListItem)
            .collect(toList());

        final FieldTypeEntity multiListType = new FieldTypeEntity();
        multiListType.setBaseFieldType(multiListBaseType);
        String multiListTypeReference = ReferenceUtils.listReference(MULTI_LIST_TYPE, listDataItems.getKey());
        spreadsheetValidator.validate(SheetName.FIXED_LISTS.getName(), "ID",
            multiListTypeReference, MULTI_LIST_TYPE);
        multiListType.setReference(multiListTypeReference);
        multiListType.setJurisdiction(parseContext.getJurisdiction());
        multiListType.addListItems(multiListItems);
        parseContext.addToAllTypes(multiListType);
        result.addNew(multiListType);

        return result;
    }

    private FieldTypeListItemEntity parseListItem(DefinitionDataItem element) {
        final FieldTypeListItemEntity listItem = new FieldTypeListItemEntity();
        listItem.setValue(element.getString(ColumnName.LIST_ELEMENT_CODE));
        listItem.setLabel(element.getString(ColumnName.LIST_ELEMENT));
        listItem.setOrder(element.getInteger(ColumnName.DISPLAY_ORDER));
        return listItem;
    }

}
