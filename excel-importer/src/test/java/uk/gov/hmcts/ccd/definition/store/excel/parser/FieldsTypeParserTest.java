package uk.gov.hmcts.ccd.definition.store.excel.parser;


import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FieldsTypeParserTest extends ParserTestBase {

    private FieldsTypeParser fieldsTypeParser;

    @Mock
    private ListFieldTypeParser fixedListParser;

    @Mock
    private ComplexFieldTypeParser complexParser;

    @Mock
    private CaseFieldTypeParser fieldParser;

    @BeforeEach
    void setup() {

        init();

        parseContext = mock(ParseContext.class);
        fieldsTypeParser = new FieldsTypeParser(fixedListParser, complexParser, fieldParser);
    }

    @Test
    void shouldParseEmpty() {

        final ParseResult<FieldTypeEntity> parseResult = new ParseResult();

        given(fixedListParser.parse(definitionSheets)).willReturn(parseResult);
        given(complexParser.parse(definitionSheets)).willReturn(parseResult);
        given(fieldParser.parse(definitionSheets)).willReturn(parseResult);

        final ParseResult<FieldTypeEntity> finalResult = fieldsTypeParser.parseAll(definitionSheets);
        assertThat(finalResult.getAllResults().size(), is(0));

    }
}
