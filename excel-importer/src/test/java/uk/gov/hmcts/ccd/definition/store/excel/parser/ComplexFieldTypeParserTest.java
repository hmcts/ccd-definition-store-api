package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;

class ComplexFieldTypeParserTest extends ParserTestBase {

    @Mock
    private ParseContext parseContext;
    @Mock
    private ShowConditionParser showConditionParser;
    @Mock
    private FieldTypeEntity complexBaseType;
    @Mock
    private FieldTypeParser fieldTypeParser;
    @Mock
    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    private ComplexFieldTypeParser complexFieldTypeParser;

    @BeforeEach
    public void setup() throws InvalidShowConditionException {
        MockitoAnnotations.initMocks(this);
        init();
        parseContext = mock(ParseContext.class);
        DefinitionDataItem definitionDataItem = buildDefinitionDataItem();
        given(parseContext.getBaseType(BASE_COMPLEX)).willReturn(Optional.of(complexBaseType));

        ParseResult.Entry<FieldTypeEntity> resultEntry = mock(ParseResult.Entry.class);
        FieldTypeEntity fieldTypeEntity = mock(FieldTypeEntity.class);
        given(resultEntry.getValue()).willReturn(fieldTypeEntity);
        given(fieldTypeParser.parse("BirthCert", definitionDataItem)).willReturn(resultEntry);

        given(showConditionParser.parseShowCondition(anyString())).willReturn(new ShowCondition.Builder().build());

        definitionSheet.addDataItem(definitionDataItem);
        definitionSheets.put(SheetName.COMPLEX_TYPES.getName(), definitionSheet);
        complexFieldTypeParser = new ComplexFieldTypeParser(parseContext,
            fieldTypeParser, showConditionParser, entityToDefinitionDataItemRegistry);
    }

    @Test
    @DisplayName("Should parse the ComplexTypes definition sheet")
    public void shouldParseComplexTypesDefinitionSheet() {

        ParseResult<FieldTypeEntity> parsedResult = complexFieldTypeParser.parse(definitionSheets);

        FieldTypeEntity fieldTypeEntity = parsedResult.getNewResults().get(1);
        final ComplexFieldEntity complexFieldEntity = fieldTypeEntity.getComplexFields().get(0);
        assertThat(fieldTypeEntity.getReference(), is("Certificate"));
        assertThat(complexFieldEntity.getReference(), is("BirthCert"));
        assertThat(complexFieldEntity.getLabel(), is("Birth Certificate"));
        assertThat(complexFieldEntity.getHint(), is("Hint text"));
        assertNull(complexFieldEntity.getHidden());
        assertThat(complexFieldEntity.getSecurityClassification(), is(SecurityClassification.PUBLIC));
        assertThat(complexFieldEntity.getCategoryId(), is("someCategory"));
        assertThat(complexFieldEntity.getShowCondition(), is(""));
    }

    private DefinitionDataItem buildDefinitionDataItem() {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.COMPLEX_TYPES.toString());

        item.addAttribute(ColumnName.ID.toString(), "Certificate");
        item.addAttribute(ColumnName.LIST_ELEMENT_CODE.toString(), "BirthCert");
        item.addAttribute(ColumnName.FIELD_TYPE.toString(), "Document");
        item.addAttribute(ColumnName.CATEGORY_ID.toString(), "someCategory");
        item.addAttribute(ColumnName.FIELD_TYPE_PARAMETER.toString(), "");
        item.addAttribute(ColumnName.ELEMENT_LABEL.toString(), "Birth Certificate");
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "");
        item.addAttribute(ColumnName.REGULAR_EXPRESSION.toString(), "");
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint text");
        item.addAttribute(ColumnName.SECURITY_CLASSIFICATION.toString(), "Public");

        return item;
    }
}
