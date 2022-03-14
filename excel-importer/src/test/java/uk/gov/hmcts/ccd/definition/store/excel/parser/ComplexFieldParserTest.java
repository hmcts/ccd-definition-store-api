package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;

public class ComplexFieldParserTest extends ParserTestBase {

    private ComplexFieldTypeParser complexFieldTypeParser;

    @BeforeEach
    public void setup(){
        parseContext = mock(ParseContext.class);
        FieldTypeParser fieldsTypeParser = mock(FieldTypeParser.class);
        ShowConditionParser showConditionParser = mock(ShowConditionParser.class);
        EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        HiddenFieldsValidator hiddenFieldsValidator = new HiddenFieldsValidator();
        Executor executor = mock(Executor.class);



        complexFieldTypeParser = new ComplexFieldTypeParser(parseContext, fieldsTypeParser,
            showConditionParser, entityToDefinitionDataItemRegistry, hiddenFieldsValidator, executor);
    }

}
