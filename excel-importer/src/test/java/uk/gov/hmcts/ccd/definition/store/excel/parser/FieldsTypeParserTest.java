package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class FieldsTypeParserTest extends ParserTestBase {

    private FieldsTypeParser fieldsTypeParser;

    @Mock
    private ListFieldTypeParser fixedListParser;

    @Mock
    private ComplexFieldTypeParser complexParser;

    @Mock
    private CaseFieldTypeParser fieldParser;

    @Before
    public void setup() {

        init();

        parseContext = mock(ParseContext.class);
        fieldsTypeParser = new FieldsTypeParser(fixedListParser, complexParser, fieldParser);
    }

    @Test
    public void shouldParseEmpty() {

        final ParseResult<FieldTypeEntity> parseResult = new ParseResult();

        given(fixedListParser.parse(definitionSheets)).willReturn(parseResult);
        given(complexParser.parse(definitionSheets)).willReturn(parseResult);
        given(fieldParser.parse(definitionSheets)).willReturn(parseResult);

        final ParseResult<FieldTypeEntity> finalResult = fieldsTypeParser.parseAll(definitionSheets);
        assertThat(finalResult.getAllResults().size(), is(0));

    }
}
