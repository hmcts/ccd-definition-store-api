package uk.gov.hmcts.ccd.definition.store.excel.validation;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.excel.common.TestLoggerUtils;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.definition.store.excel.common.TestLoggerUtils.assertLogged;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;

@DisplayName("DotNotationValidator")
@ExtendWith(MockitoExtension.class)
class DotNotationValidatorTest {

    private static final SheetName SHEET_NAME = SheetName.SEARCH_PARTY;
    private static final ColumnName COLUMN_NAME = ColumnName.SEARCH_PARTY_NAME;
    private static final String CASE_TYPE = "TestCaseType";

    private ParseContext parseContext;
    private ListAppender<ILoggingEvent> listAppender;

    @InjectMocks
    private DotNotationValidator dotNotationValidator;

    @BeforeEach
    void setUp() {
        parseContext = buildParseContext();
        listAppender = TestLoggerUtils.setupLogger();
    }

    @AfterEach
    void teardown() {
        TestLoggerUtils.teardownLogger();
    }

    @ParameterizedTest(name = "should validate expression if field exists in ParseContext - #{index} - `{0}`")
    @ValueSource(strings = {
        "SimpleText",
        "TopLevel.Text",
        "TopLevel.Child.Text",
        "TopLevel.Child.Grandchild.Text"
    })
    void shouldValidateExpressionIfFieldExistsInParseContext(String expression) {

        // GIVEN
        String topLevelField = expression.split(Pattern.quote(DotNotationValidator.DOT_SEPARATOR))[0];

        // WHEN
        dotNotationValidator.validate(
            this.parseContext,
            SHEET_NAME,
            COLUMN_NAME,
            CASE_TYPE,
            expression
        );

        // THEN
        // NB: only the top-level field is searched for: its descendants are loaded from the same response.
        verify(parseContext).getCaseFieldType(CASE_TYPE, topLevelField);
    }

    @ParameterizedTest(name = "should validate and load FieldType if field exists in ParseContext - #{index} - `{0}`")
    @ArgumentsSource(ValidExpressionWithFieldTypeArgumentsProvider.class)
    void shouldValidateAndLoadFieldTypeIfFieldExistsInParseContext(String expression, String expectedFieldTypeRef) {

        // GIVEN

        // WHEN
        FieldTypeEntity fieldTypeEntity = dotNotationValidator.validateAndLoadFieldType(
            this.parseContext,
            SHEET_NAME,
            COLUMN_NAME,
            CASE_TYPE,
            expression
        );

        // THEN
        assertNotNull(fieldTypeEntity);
        assertEquals(expectedFieldTypeRef, fieldTypeEntity.getReference());
    }

    @ParameterizedTest(name = "should validate expression if field exists in ComplexType - #{index} - `{0}`")
    @ValueSource(strings = {
        "Text",
        "Child.Text",
        "Child.Grandchild.Text"
    })
    void shouldValidateExpressionIfFieldExistsInComplexType(String expression) {

        // GIVEN
        FieldTypeEntity complexField = getTestComplexType();

        // WHEN
        dotNotationValidator.validate(
            complexField,
            SHEET_NAME,
            COLUMN_NAME,
            expression
        );

        // THEN
        // NB: just looking for no exception: so just verify premise that we started with a complex type
        assertTrue(complexField.isComplexFieldType());
    }

    @ParameterizedTest(name = "throws exception if expression field does not exist in ParseContext - #{index} - `{0}`")
    @ArgumentsSource(BadExpressionsForValidationFromParseContextArgumentsProvider.class)
    void throwsExceptionWhenExpressionFieldDoesNotExistInParseContext(String expression, String badField) {

        // GIVEN
        String expectedFullErrorMessage = getExpectedErrorMessage(expression);

        // WHEN & THEN
        InvalidImportException invalidImportException = assertThrows(InvalidImportException.class,
            () -> dotNotationValidator.validate(
                parseContext,
                SHEET_NAME,
                COLUMN_NAME,
                CASE_TYPE,
                expression
            )
        );

        assertEquals(expectedFullErrorMessage, invalidImportException.getMessage());

        // verify issue logged identifying the problematic field ....
        assertLogged(listAppender, getExpectedErrorMessage(badField));
        // ...  and also identifying full expression (if different)
        assertLogged(listAppender, expectedFullErrorMessage);
    }

    @ParameterizedTest(
        name = "throws exception if expression field does not exist and loading FieldType - #{index} - `{0}`"
    )
    @ArgumentsSource(BadExpressionsForValidationFromParseContextArgumentsProvider.class)
    void throwsExceptionWhenExpressionFieldDoesNotExistAndLoadingFieldType(String expression, String badField) {

        // GIVEN
        String expectedFullErrorMessage = getExpectedErrorMessage(expression);

        // WHEN & THEN
        InvalidImportException invalidImportException = assertThrows(InvalidImportException.class,
            () -> dotNotationValidator.validateAndLoadFieldType(
                parseContext,
                SHEET_NAME,
                COLUMN_NAME,
                CASE_TYPE,
                expression
            )
        );

        assertEquals(expectedFullErrorMessage, invalidImportException.getMessage());

        // verify issue logged identifying the problematic field ....
        assertLogged(listAppender, getExpectedErrorMessage(badField));
        // ...  and also identifying full expression (if different)
        assertLogged(listAppender, expectedFullErrorMessage);
    }

    @ParameterizedTest(name = "throws exception if expression field does not exist in ComplexType - #{index} - `{0}`")
    @ArgumentsSource(BadExpressionsForValidationFromComplexTypeArgumentsProvider.class)
    void throwsExceptionWhenExpressionFieldDoesNotExistInComplexType(String expression, String badField) {

        // GIVEN
        FieldTypeEntity complexField = getTestComplexType();
        String expectedFullErrorMessage = getExpectedErrorMessage(expression);

        // WHEN & THEN
        InvalidImportException invalidImportException = assertThrows(InvalidImportException.class,
            () -> dotNotationValidator.validate(
                complexField,
                SHEET_NAME,
                COLUMN_NAME,
                expression
            )
        );

        assertEquals(expectedFullErrorMessage, invalidImportException.getMessage());

        // verify issue logged identifying the problematic field ....
        assertLogged(listAppender, getExpectedErrorMessage(badField));
        // ...  and also identifying full expression (if different)
        assertLogged(listAppender, expectedFullErrorMessage);
    }

    private static String getExpectedErrorMessage(String expression) {
        return String.format(DotNotationValidator.ERROR_MESSAGE, SHEET_NAME, expression, COLUMN_NAME);
    }

    private FieldTypeEntity getTestComplexType() {
        // load test complex type (i.e. `TopLevel`) from test parseContext
        return this.parseContext.getCaseFieldType(CASE_TYPE, "TopLevel");
    }

    private static class ValidExpressionWithFieldTypeArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                // Arguments :: expression, expectedFieldTypeReference

                // NB: see references in `buildParseContext`

                Arguments.of("SimpleText", "Text"),
                Arguments.of("TopLevel", "TopLevelField"),
                Arguments.of("TopLevel.Child", "ChildField"),
                Arguments.of("TopLevel.Child.Grandchild", "GrandchildField")
            );
        }
    }

    private static class BadExpressionsForValidationFromParseContextArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                // Arguments :: expression, badField

                // blank fields
                Arguments.of("", ""), // assume match always required as calling code can choose not to validate blanks
                Arguments.of(".Text", ""),
                Arguments.of("TopLevel..Text", ""),
                Arguments.of("TopLevel.Child..Text", ""),

                // blank complex field properties
                Arguments.of("TopLevel.", ""),
                Arguments.of("TopLevel.Child.", ""),
                Arguments.of("TopLevel.Child.Grandchild.", ""),
                Arguments.of("TopLevel.Child.Grandchild.Text.", ""),

                // bad fields (i.e. reference does not exist)
                Arguments.of("BadSimpleText", "BadSimpleText"),
                Arguments.of("BadTopLevel.Text", "BadTopLevel"),
                Arguments.of("TopLevel.BadChild.Text", "BadChild"),
                Arguments.of("TopLevel.Child.BadGrandchild.Text", "BadGrandchild"),

                // bad complex field properties (i.e. reference does not exist)
                Arguments.of("TopLevel.BadProperty", "BadProperty"),
                Arguments.of("TopLevel.Child.BadProperty", "BadProperty"),
                Arguments.of("TopLevel.Child.Grandchild.BadProperty", "BadProperty"),
                Arguments.of("TopLevel.Child.Grandchild.Text.BadProperty", "BadProperty"),

                // bad spaces - before
                Arguments.of(" ", " "),
                Arguments.of(" SimpleText", " SimpleText"),
                Arguments.of(" TopLevel.Text", " TopLevel"),
                Arguments.of("TopLevel. Child.Text", " Child"),
                Arguments.of("TopLevel.Child. Grandchild.Text", " Grandchild"),
                Arguments.of("TopLevel.Child.Grandchild. Text", " Text"),

                // bad spaces - after
                Arguments.of("SimpleText ", "SimpleText "),
                Arguments.of("TopLevel .Text", "TopLevel "),
                Arguments.of("TopLevel.Child .Text", "Child "),
                Arguments.of("TopLevel.Child.Grandchild .Text", "Grandchild "),
                Arguments.of("TopLevel.Child.Grandchild.Text ", "Text ")
            );
        }
    }

    private static class BadExpressionsForValidationFromComplexTypeArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                // Arguments :: expression, badField

                // NB: same as `BadExpressionsForValidationFromParseContextArgumentsProvider' but with TopLevel removed

                // blank fields
                Arguments.of("", ""), // assume match always required as calling code can choose not to validate blanks
                Arguments.of(".Text", ""),
                Arguments.of("Child..Text", ""),

                // blank complex field properties
                Arguments.of(".", ""),
                Arguments.of("Child.", ""),
                Arguments.of("Child.Grandchild.", ""),
                Arguments.of("Child.Grandchild.Text.", ""),

                // bad fields (i.e. reference does not exist)
                Arguments.of("BadChild.Text", "BadChild"),
                Arguments.of("Child.BadGrandchild.Text", "BadGrandchild"),

                // bad complex field properties (i.e. reference does not exist)
                Arguments.of("BadProperty", "BadProperty"),
                Arguments.of("Child.BadProperty", "BadProperty"),
                Arguments.of("Child.Grandchild.BadProperty", "BadProperty"),
                Arguments.of("Child.Grandchild.Text.BadProperty", "BadProperty"),

                // bad spaces - before
                Arguments.of(" ", " "),
                Arguments.of(" Child.Text", " Child"),
                Arguments.of("Child. Grandchild.Text", " Grandchild"),
                Arguments.of("Child.Grandchild. Text", " Text"),

                // bad spaces - after
                Arguments.of("Child .Text", "Child "),
                Arguments.of("Child.Grandchild .Text", "Grandchild "),
                Arguments.of("Child.Grandchild.Text ", "Text ")
            );
        }
    }

    private ParseContext buildParseContext() {
        ParseContext parseContext = spy(new ParseContext());

        // register a test case type
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE);
        parseContext.registerCaseType(caseTypeEntity);

        // register basic text field type
        FieldTypeEntity textFieldType = new FieldTypeEntity();
        textFieldType.setReference(BASE_TEXT);
        parseContext.addToAllTypes(textFieldType);

        // register complex fields
        // :: i.e. TopLevel.Child.Grandchild
        FieldTypeEntity baseComplexType = buildBaseComplexType();

        // :: register grandchild complex type
        FieldTypeEntity grandchildFieldType = new FieldTypeEntity();
        grandchildFieldType.setReference("GrandchildField");
        grandchildFieldType.setBaseFieldType(baseComplexType);
        parseContext.addToAllTypes(grandchildFieldType);
        // :: Grandchild -> text
        ComplexFieldEntity grandchildTextField = new ComplexFieldEntity();
        grandchildTextField.setReference("Text");
        grandchildTextField.setFieldType(textFieldType);
        // :: Grandchild :: add all fields for type
        grandchildFieldType.addComplexFields(List.of(grandchildTextField));

        // :: register child complex type
        FieldTypeEntity childFieldType = new FieldTypeEntity();
        childFieldType.setReference("ChildField");
        childFieldType.setBaseFieldType(baseComplexType);
        parseContext.addToAllTypes(childFieldType);
        // :: child -> text
        ComplexFieldEntity childTextField = new ComplexFieldEntity();
        childTextField.setReference("Text");
        childTextField.setFieldType(textFieldType);
        // :: child -> Grandchild
        ComplexFieldEntity grandchildField = new ComplexFieldEntity();
        grandchildField.setReference("Grandchild");
        grandchildField.setFieldType(grandchildFieldType);
        // :: child :: add all fields for type
        childFieldType.addComplexFields(List.of(childTextField, grandchildField));

        // :: register top level complex type
        FieldTypeEntity topLevelFieldType = new FieldTypeEntity();
        topLevelFieldType.setReference("TopLevelField");
        topLevelFieldType.setBaseFieldType(baseComplexType);
        parseContext.addToAllTypes(topLevelFieldType);
        // :: top level -> text
        ComplexFieldEntity topLevelTextField = new ComplexFieldEntity();
        topLevelTextField.setReference("Text");
        topLevelTextField.setFieldType(textFieldType);
        // :: top level -> child
        ComplexFieldEntity childField = new ComplexFieldEntity();
        childField.setReference("Child");
        childField.setFieldType(childFieldType);
        // :: top level :: add all fields for type
        topLevelFieldType.addComplexFields(List.of(topLevelTextField, childField));

        // register all fields that belong to case type (i.e. top level fields)
        parseContext.registerCaseFieldType(CASE_TYPE, "SimpleText", textFieldType);
        parseContext.registerCaseFieldType(CASE_TYPE, "TopLevel", topLevelFieldType);

        return parseContext;
    }

    private FieldTypeEntity buildBaseComplexType() {

        FieldTypeEntity baseComplexType = new FieldTypeEntity();
        baseComplexType.setReference(BASE_COMPLEX);

        return baseComplexType;
    }
}
