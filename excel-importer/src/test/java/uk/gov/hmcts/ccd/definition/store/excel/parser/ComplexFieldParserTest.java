package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComplexFieldParserTest extends ParserTestBase {

    private static final String COMPLEX_ID = "Address";
    private static final String COMPLEX_LIST_ELEMENT_CODE = "AddressLine1";
    private static final String COMPLEX_FIELD_TYPE = "Text";
    private static final String COMPLEX_ELEMENT_LABEL = "Address Line 1";
    private static final String COMPLEX_SECURITY_CLASSIFICATION = "Public";
    private static final String COMPLEX_HINT = "Hint";
    private static final String COMPLEX_SHOW_CONDITION = "SHOW CONDITION";
    private static final String COMPLEX_DISPLAY_CONTEXT_PARAMETER = "Display Context Parameter";
    private static final String COMPLEX_CATEGORY_ID = "Category ID";
    private static final Integer COMPLEX_DISPLAY_ORDER = 1;
    private static final Boolean COMPLEX_SEARCHABLE = true;
    private static final Boolean COMPLEX_RETAIN_HIDDEN_VALUE = true;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private ComplexFieldTypeParser complexFieldTypeParser;

    private FieldTypeEntity complexBaseType;

    private JurisdictionEntity jurisdiction;

    @Mock
    private HiddenFieldsValidator hiddenFieldsValidator;

    @Mock
    private FieldTypeParser fieldTypeParser;


    @BeforeEach
    public void setup() throws InvalidShowConditionException {
        init();
        parseContext = new ParseContext();
        jurisdiction = new JurisdictionEntity();
        List<FieldTypeEntity> complexBaseTypes = new ArrayList<>();
        complexBaseType = new FieldTypeEntity();
        complexBaseType.setReference("Complex");
        complexBaseTypes.add(complexBaseType);
        parseContext.addBaseTypes(complexBaseTypes);
        parseContext.setJurisdiction(jurisdiction);
        definitionSheets.put(SheetName.COMPLEX_TYPES.getName(), definitionSheet);

        ShowConditionParser showConditionParser = new ShowConditionParser();
        EntityToDefinitionDataItemRegistry
            entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        definitionSheet.addDataItem(buildDefinitionDataItem());

        complexFieldTypeParser = new ComplexFieldTypeParser(parseContext, fieldTypeParser,
            showConditionParser, entityToDefinitionDataItemRegistry, hiddenFieldsValidator, executor);
    }

    @Test
    public void testComplexParser() {
        FieldTypeEntity fieldTypeEntity2 = new FieldTypeEntity();
        final ParseResult.Entry<FieldTypeEntity> resultEntry = ParseResult.Entry.createNew(fieldTypeEntity2);

        when(hiddenFieldsValidator
            .parseComplexTypesHiddenFields(definitionSheet.getDataItems().get(0), definitionSheets)).thenReturn(true);
        when(fieldTypeParser
            .parse(COMPLEX_LIST_ELEMENT_CODE, definitionSheet.getDataItems().get(0))).thenReturn(resultEntry);

        ParseResult<FieldTypeEntity> parseResult = complexFieldTypeParser.parse(definitionSheets);

        FieldTypeEntity complexType =
            parseResult.getAllResults().get(1);

        assertEquals(COMPLEX_ID, complexType.getReference());
        assertEquals(complexBaseType, complexType.getBaseFieldType());
        assertEquals(jurisdiction, complexType.getJurisdiction());

        ComplexFieldEntity complexFieldEntity =
            parseResult.getAllResults().get(1).getComplexFields().iterator().next();
        assertEquals(COMPLEX_LIST_ELEMENT_CODE, complexFieldEntity.getReference());
        assertEquals(fieldTypeEntity2, complexFieldEntity.getFieldType());
        assertEquals(SecurityClassification.PUBLIC, complexFieldEntity.getSecurityClassification());
        assertEquals(COMPLEX_ELEMENT_LABEL, complexFieldEntity.getLabel());
        assertEquals(COMPLEX_HINT, complexFieldEntity.getHint());
        assertEquals(COMPLEX_DISPLAY_ORDER, complexFieldEntity.getOrder());
        assertEquals(COMPLEX_SHOW_CONDITION, complexFieldEntity.getShowCondition());
        assertEquals(COMPLEX_DISPLAY_CONTEXT_PARAMETER, complexFieldEntity.getDisplayContextParameter());
        assertTrue(complexFieldEntity.isSearchable());
        assertTrue(complexFieldEntity.getRetainHiddenValue());
        assertEquals(COMPLEX_CATEGORY_ID, complexFieldEntity.getCategoryId());


    }

    private DefinitionDataItem buildDefinitionDataItem() {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.COMPLEX_TYPES.toString());
        item.addAttribute(ColumnName.ID.toString(), COMPLEX_ID);
        item.addAttribute(ColumnName.LIST_ELEMENT_CODE.toString(), COMPLEX_LIST_ELEMENT_CODE);
        item.addAttribute(ColumnName.FIELD_TYPE.toString(), COMPLEX_FIELD_TYPE);
        item.addAttribute(ColumnName.ELEMENT_LABEL.toString(), COMPLEX_ELEMENT_LABEL);
        item.addAttribute(ColumnName.SECURITY_CLASSIFICATION.toString(), COMPLEX_SECURITY_CLASSIFICATION);
        item.addAttribute(ColumnName.HINT_TEXT.toString(), COMPLEX_HINT);
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), COMPLEX_SHOW_CONDITION);
        item.addAttribute(ColumnName.DISPLAY_ORDER, COMPLEX_DISPLAY_ORDER);
        item.addAttribute(ColumnName.DISPLAY_CONTEXT_PARAMETER, COMPLEX_DISPLAY_CONTEXT_PARAMETER);
        item.addAttribute(ColumnName.SEARCHABLE, COMPLEX_SEARCHABLE);
        item.addAttribute(ColumnName.RETAIN_HIDDEN_VALUE, COMPLEX_RETAIN_HIDDEN_VALUE);
        item.addAttribute(ColumnName.CATEGORY_ID, COMPLEX_CATEGORY_ID);
        return item;
    }

}
