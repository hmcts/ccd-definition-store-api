package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class FieldTypeParserTest {

    @Mock
    private ParseContext parseContext;

    @InjectMocks
    private FieldTypeParser classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void shouldReturnFieldTypeEntityWithUniqueReferenceAndCollectionBaseType_whenFieldTypeAttributeIsCollection() {

        FieldTypeEntity collectionBaseType = new FieldTypeEntity();
        String collectionFieldType = "Collection";
        when(parseContext.getType(eq(collectionFieldType))).thenReturn(Optional.of(collectionBaseType));
        String fieldTypeParameter = "FieldTypeParameter";
        FieldTypeEntity parameterFieldType = new FieldTypeEntity();
        when(parseContext.getType(eq(fieldTypeParameter))).thenReturn(Optional.of(parameterFieldType));

        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.CASE_FIELD.toString());
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE.toString(), collectionFieldType);
        definitionDataItem.addAttribute(ColumnName.FIELD_TYPE_PARAMETER.toString(), fieldTypeParameter);

        String fieldId = "FieldId";

        ParseResult.Entry<FieldTypeEntity> result = classUnderTest.parse(fieldId, definitionDataItem);

        assertNotNull(result.getValue());

        FieldTypeEntity parsedEntity = result.getValue();

        assertTrue(
            String.format(
                "Should be a reference matching %s-<SOME_GUID> but was %s", fieldId, parsedEntity.getReference()),
            Pattern.compile(
                String.format("(%s)-[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}", fieldId))
                .matcher(parsedEntity.getReference()).matches()
        );
        assertEquals(parsedEntity.getBaseFieldType(), collectionBaseType);
        assertEquals(parameterFieldType, parsedEntity.getCollectionFieldType());

        assertNull(parsedEntity.getRegularExpression());
        assertNull(parsedEntity.getMinimum());
        assertNull(parsedEntity.getMaximum());

    }

}
