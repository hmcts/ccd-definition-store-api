package uk.gov.hmcts.ccd.definition.store.excel.util.mapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MapperUtil {

    public static final String COMPLEX = "Complex";
    public static final String FIXED_LIST = "FixedList";
    public static final String COLLECTION = "Collection";
    public static final String TEXT = "Text";
    public static final String MULTI_SELECT_LIST = "MultiSelectList";
    private static final String DATE = "Date";
    private static final String NO_COLUMN = "There's a missing value in column %s in the sheet %s";
    private static final String INCORRECT_INTEGER_LIST_FORMAT =
        "Found '%s' whilst expecting a comma separated list of numbers in the column '%s' of sheet '%s'";
    private static final String FIELD_SEPARATOR = ",";
    private static final String INVALID_VALUE_COLUMN = "Invalid value '%s' is found in column '%s' in the sheet '%s'";
    private static final String INVALID_OR_BLANK_COLUMN
        = "Couldn't find the column '%s' or invalid value in the sheet '%s'";

    private static final Logger LOG = LoggerFactory.getLogger(MapperUtil.class);

    private MapperUtil() {
        // This class is intended to provide static util methods
    }

    /**
     * Find the given DefinitionSheet in the given sheets list.
     *
     * @param sheets    - DefinitionSheets representing a Case Definition
     * @param sheetName - Name of the DefinitionSheet to be found
     * @return The DefinitionSheet matching the sheetName (null otherwise)
     */
    static DefinitionSheet findSheet(List<DefinitionSheet> sheets, SheetName sheetName) {
        for (DefinitionSheet sheet : sheets) {
            if (sheet.getName().equals(sheetName.getName())) {
                return sheet;
            }
        }
        return null;
    }

    /**
     * Find the String attribute in the given item.
     * Check if the attribute is required for the given sheet and column,
     * if so then throw an exception if the attribute doesn't exist
     *
     * @param definitionDataItem - item to find the attribute from
     * @param sheetName          - name of the sheet the attribute is in
     * @param columnName         - name of the column the attribute is in
     * @return The String attribute found (or null if it wasn't found)
     * @throws MapperException - thrown if the field is required, but the attribute wasn't found
     */
    static String getString(DefinitionDataItem definitionDataItem, SheetName sheetName, ColumnName columnName) {
        Object attribute = definitionDataItem.findAttribute(columnName.toString());
        if (attribute == null) {
            if (ColumnName.isRequired(sheetName, columnName)) {
                throw new MapperException(String.format(NO_COLUMN, columnName.toString(), sheetName.toString()));
            }
            return null;
        }
        return attribute.toString();
    }

    /**
     * Find the Integer attribute in the given item.
     * Check if the attribute is required for the given sheet and column,
     * if so then throw an exception if the attribute doesn't exist
     *
     * @param definitionDataItem - item to find the attribute from
     * @param sheetName          - name of the sheet the attribute is in
     * @param columnName         - name of the column the attribute is in
     * @return The Integer attribute found (or null if it wasn't found)
     * @throws MapperException - thrown if the field is required, but the attribute wasn't found
     */
    static Integer getInteger(DefinitionDataItem definitionDataItem, SheetName sheetName, ColumnName columnName) {
        Object attribute = definitionDataItem.findAttribute(columnName.toString());
        if (attribute == null) {
            if (ColumnName.isRequired(sheetName, columnName)) {
                throw new MapperException(String.format(NO_COLUMN, columnName.toString(), sheetName.toString()));
            }
            return null;
        }
        return ((Double) attribute).intValue();
    }

    /**
     * Find the BigDecimal attribute in the given item.
     * Check if the attribute is required for the given sheet and column,
     * if so then throw an exception if the attribute doesn't exist
     *
     * @param definitionDataItem - item to find the attribute from
     * @param sheetName          - name of the sheet the attribute is in
     * @param columnName         - name of the column the attribute is in
     * @return The BigDecimal attribute found (or null if it wasn't found)
     * @throws MapperException - thrown if the field is required, but the attribute wasn't found
     */
    static BigDecimal getBigDecimal(DefinitionDataItem definitionDataItem, SheetName sheetName, ColumnName columnName) {
        Object attribute = definitionDataItem.findAttribute(columnName.toString());
        if (attribute == null) {
            if (ColumnName.isRequired(sheetName, columnName)) {
                throw new MapperException(String.format(NO_COLUMN, columnName.toString(), sheetName.toString()));
            }
            return null;
        }
        return BigDecimal.valueOf((double) attribute);
    }

    /**
     * Find the Date attribute in the given item.
     * Check if the attribute is required for the given sheet and column,
     * if so then throw an exception if the attribute doesn't exist
     *
     * @param definitionDataItem - item to find the attribute from
     * @param sheetName          - name of the sheet the attribute is in
     * @param columnName         - name of the column the attribute is in
     * @return The Date attribute found (or null if it wasn't found)
     * @throws MapperException - thrown if the field is required, but the attribute wasn't found
     */
    static Date getDate(DefinitionDataItem definitionDataItem, SheetName sheetName, ColumnName columnName) {
        Object attribute = definitionDataItem.findAttribute(columnName.toString());
        if (attribute == null) {
            if (ColumnName.isRequired(sheetName, columnName)) {
                throw new MapperException(String.format(NO_COLUMN, columnName.toString(), sheetName.toString()));
            }
            return null;
        }
        return (Date) attribute;
    }

    /**
     * Find the Boolean attribute in the given item.
     * Check if the attribute is required for the given sheet and column,
     * if so then throw an exception if the attribute doesn't exist or if the field value cannot be interpreted
     *
     * @param definitionDataItem - item to find the attribute from
     * @param sheetName          - name of the sheet the attribute is in
     * @param columnName         - name of the column the attribute is in
     * @return The Boolean attribute found, "Yes" represents a true response and false otherwise.
     * @throws MapperException - thrown if the field is required, but the attribute wasn't found
     *                         or if the field value cannot be interpreted
     */
    static Boolean getBoolean(DefinitionDataItem definitionDataItem, SheetName sheetName, ColumnName columnName) {
        final Object obj = definitionDataItem.findAttribute(columnName.toString());
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        final String attribute = (String) obj;
        if (isBlank(attribute)) {
            if (ColumnName.isRequired(sheetName, columnName)) {
                throw new MapperException(String.format(INVALID_OR_BLANK_COLUMN, columnName, sheetName));
            }
            return null;
        }

        // returns true or false for the following definitions, or throws MapperException
        // Case Insensitive Yes = true
        // Case Insensitive No = false
        // Case Insensitive Y = true
        // Case Insensitive N = false
        // Case Insensitive True= true
        // Case Insensitive False= false
        // Case Insensitive T = true
        // Case Insensitive F = false
        switch (attribute.toLowerCase()) {
            case "yes":
                return true;
            case "y":
                return true;
            case "t":
                return true;
            case "true":
                return true;
            case "no":
                return false;
            case "n":
                return false;
            case "false":
                return false;
            case "f":
                return false;
            default:
                throw new MapperException(String.format(INVALID_VALUE_COLUMN, attribute, columnName, sheetName));
        }
    }

    /**
     * Find the Integer list attribute in the given item.
     * If there is an error in the attribute, then we return an empty list.
     *
     * @param definitionDataItem - definition data item
     * @param sheetName          - sheetName
     * @param columnName         - columnName
     * @return integer list
     * @throws MapperException - thrown if the field is required, but the attribute wasn't found or numbers in the
     *      list cannot be parsed
     */
    static List<Integer> getIntegerList(DefinitionDataItem definitionDataItem,
                                        SheetName sheetName,
                                        ColumnName columnName) {
        final String numbersString = getString(definitionDataItem, sheetName, columnName);
        if (isBlank(numbersString)) {
            if (ColumnName.isRequired(sheetName, columnName)) {
                throw new MapperException(String.format(NO_COLUMN, columnName, sheetName));
            }
            return Collections.emptyList();
        }
        try {
            return Arrays.asList(numbersString.split(FIELD_SEPARATOR))
                .stream()
                .map(StringUtils::trim)
                .map(attribute -> Math.round(Float.valueOf(attribute)))
                .collect(Collectors.toList());
        } catch (NumberFormatException ex) {
            LOG.error("Found '{}' whilst expecting a comma separated list of numbers in Sheet {}, column name {}",
                numbersString, sheetName, columnName, ex);
            throw new MapperException(String.format(INCORRECT_INTEGER_LIST_FORMAT,
                numbersString, columnName, sheetName), ex);
        }
    }

    /**
     * Create a FieldType object for the given DefinitionDataItem in the given Sheet.
     *
     * @param sheetName        - name of the given Sheet
     * @param dataItem         - given DefinitionDataItem
     * @param fixedListsByCode - all Fixed List Item's mapped by their Id
     * @param complexTypeIds   - all ComplexTypeId's in the current Case Definition
     * @return created FieldType
     */
    static FieldType createFieldType(SheetName sheetName,
                                     DefinitionDataItem dataItem,
                                     Map<String, List<FixedListItem>> fixedListsByCode,
                                     Set<String> complexTypeIds) {
        FieldType fieldType = new FieldType();
        String id = getString(dataItem, sheetName, ColumnName.FIELD_TYPE);
        fieldType.setId(id);
        fieldType.setType(complexTypeIds.contains(id) ? COMPLEX : id);
        // Set Max and Min values, these will be numeric unless the field is a Date field
        if (!DATE.equals(fieldType.getType())) {
            fieldType.setMin(getString(dataItem, sheetName, ColumnName.MIN));
            fieldType.setMax(getString(dataItem, sheetName, ColumnName.MAX));
        } else {
            Date minDate = getDate(dataItem, sheetName, ColumnName.MIN);
            if (minDate != null) {
                fieldType.setMin(String.valueOf(minDate.getTime()));
            }
            Date maxDate = getDate(dataItem, sheetName, ColumnName.MAX);
            if (maxDate != null) {
                fieldType.setMax(String.valueOf(maxDate.getTime()));
            }
        }
        fieldType.setRegularExpression(getString(dataItem, sheetName, ColumnName.REGULAR_EXPRESSION));

        final String fieldTypeParameter = getString(dataItem, sheetName, ColumnName.FIELD_TYPE_PARAMETER);

        if ((FIXED_LIST.equals(id) || MULTI_SELECT_LIST.equals(id))
            && fixedListsByCode.get(fieldTypeParameter) != null) {
            fieldType.setFixedListItems(fixedListsByCode.get(fieldTypeParameter));
        } else if (COLLECTION.equals(id)) {
            fieldType.setId(fieldTypeParameter);
        }
        return fieldType;
    }

    static FieldType createCollectionFieldType(FieldType fieldType, Map<String, List<CaseField>> complexTypesById) {

        final FieldType itemType = new FieldType();

        if (complexTypesById.containsKey(fieldType.getId())) {
            itemType.setId(fieldType.getId());
            itemType.setType(COMPLEX);
            itemType.setComplexFields(complexTypesById.get(fieldType.getId()));
        } else {
            itemType.setId(fieldType.getId());
            itemType.setType(fieldType.getId());
        }

        return itemType;
    }

    /**
     * Create all the CaseField objects for Complex Types from the Case Definition and map them in lists
     * by their Field Type Id.
     *
     * @param sheets - DefinitionSheets representing a Case Definition
     * @return Map of CaseField's by CaseTypeId
     * @throws MapperException - thrown if the mapping fails when validating the imported schema
     */
    static Map<String, List<CaseField>> createComplexTypes(List<DefinitionSheet> sheets) {
        DefinitionSheet complexTypesSheet = findSheet(sheets, SheetName.COMPLEX_TYPES);
        // If there is no Case Field sheet then fail the import
        if (complexTypesSheet == null) {
            throw new MapperException("A definition must contain a Complex Types worksheet");
        }
        Map<String, List<FixedListItem>> fixedListsByCode = createFixedListFields(sheets);
        Map<String, List<CaseField>> complexTypeFieldsById = new HashMap<>();
        Set<String> complexTypeIds = findComplexTypes(sheets);
        for (DefinitionDataItem complexTypeItem : complexTypesSheet.getDataItems()) {
            CaseField caseField = new CaseField();
            caseField.setId(getString(complexTypeItem, SheetName.COMPLEX_TYPES, ColumnName.LIST_ELEMENT_CODE));
            caseField.setLabel(getString(complexTypeItem, SheetName.COMPLEX_TYPES, ColumnName.ELEMENT_LABEL));
            caseField.setHintText(getString(complexTypeItem, SheetName.COMPLEX_TYPES, ColumnName.HINT_TEXT));
            caseField.setFieldType(createFieldType(
                SheetName.COMPLEX_TYPES, complexTypeItem, fixedListsByCode, complexTypeIds));
            caseField.setHidden(getBoolean(complexTypeItem, SheetName.COMPLEX_TYPES, ColumnName.DEFAULT_HIDDEN));
            caseField.setSecurityClassification(getString(
                complexTypeItem, SheetName.COMPLEX_TYPES, ColumnName.SECURITY_CLASSIFICATION));
            String complexTypeId = getString(complexTypeItem, SheetName.COMPLEX_TYPES, ColumnName.ID);
            complexTypeFieldsById.computeIfAbsent(complexTypeId, k -> new ArrayList<>());
            complexTypeFieldsById.get(complexTypeId).add(caseField);
        }

        return complexTypeFieldsById;
    }

    /**
     * Recursive function to populate the tree of Complex Case Fields.
     *
     * @param fieldTypeId      - Id of FieldType being populated
     * @param complexTypesById - All Complex Case Fields by their Case Type Id
     */
    static void populateComplexCaseFields(String fieldTypeId, Map<String, List<CaseField>> complexTypesById) {
        List<CaseField> complexFields = complexTypesById.get(fieldTypeId);
        for (CaseField complexField : complexFields) {
            // If the Field Type is COMPLEX with no ComplexTypeFields, then the tree hasn't been mapped yet
            if (complexField.getFieldType().getType().equals(COMPLEX)
                && complexField.getFieldType().getComplexFields().isEmpty()) {
                // Make recursive call to map populate and Complex Fields of this Field Type,
                // then set them onto the Field
                populateComplexCaseFields(complexField.getFieldType().getId(), complexTypesById);
                complexField.getFieldType().setComplexFields(complexTypesById.get(complexField.getFieldType().getId()));
            }
        }
    }

    static void populateCollectionCaseFields(FieldType fieldType, Map<String, List<CaseField>> complexTypesById) {
        switch (fieldType.getType()) {
            case COLLECTION:
                fieldType.setCollectionFieldType(createCollectionFieldType(fieldType, complexTypesById));
                populateCollectionCaseFields(fieldType.getCollectionFieldType(), complexTypesById);
                break;
            case COMPLEX:
                if (null != fieldType.getComplexFields()) {
                    for (CaseField caseField : fieldType.getComplexFields()) {
                        populateCollectionCaseFields(caseField.getFieldType(), complexTypesById);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Create a List of FixedListItem entries mapped by Fixed List Code.
     *
     * @param sheets Case Definition sheets
     * @return Map of FixedListItem's
     */
    static Map<String, List<FixedListItem>> createFixedListFields(List<DefinitionSheet> sheets) {
        DefinitionSheet fixedListSheet = findSheet(sheets, SheetName.FIXED_LISTS);
        // If there is no Fixed List sheet then fail the import
        if (fixedListSheet == null) {
            throw new MapperException("A definition must contain a Fixed List worksheet");
        }
        // Create a List of FixedListsField's mapped by their FixedListCode
        Map<String, List<FixedListItem>> fixedListsByCode = new HashMap<>();
        for (DefinitionDataItem fixedListItem : fixedListSheet.getDataItems()) {
            FixedListItem fixedListField = new FixedListItem();
            fixedListField.setCode(getString(fixedListItem, SheetName.FIXED_LISTS, ColumnName.LIST_ELEMENT_CODE));
            fixedListField.setLabel(getString(fixedListItem, SheetName.FIXED_LISTS, ColumnName.LIST_ELEMENT));

            // Add fixed list field to map
            String fixedListCode = getString(fixedListItem, SheetName.FIXED_LISTS, ColumnName.ID);
            fixedListsByCode.computeIfAbsent(fixedListCode, k -> new ArrayList<>());
            fixedListsByCode.get(fixedListCode).add(fixedListField);
        }

        return fixedListsByCode;
    }

    /**
     * Create a Set of all names of all Complex Field Types.
     *
     * @param sheets Case Definition Sheets
     * @return Set containing names of all Complex Types
     */
    static Set<String> findComplexTypes(List<DefinitionSheet> sheets) {
        DefinitionSheet complexTypesSheet = findSheet(sheets, SheetName.COMPLEX_TYPES);
        // If there is no Case Field sheet then fail the import
        if (complexTypesSheet == null) {
            throw new MapperException("A definition must contain a Complex Types worksheet");
        }
        Set<String> complexTypes = new HashSet<>();
        for (DefinitionDataItem complexTypeItem : complexTypesSheet.getDataItems()) {
            complexTypes.add(getString(complexTypeItem, SheetName.COMPLEX_TYPES, ColumnName.ID));
        }
        return complexTypes;
    }
}
