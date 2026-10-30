package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CaseTypeParserTest extends ParserTestBase {

    private CaseTypeParser caseTypeParser;

    @Mock
    private StateParser stateParser;

    @Mock
    private EventParser eventParser;

    @Mock
    private CaseFieldParser caseFieldParser;

    @Mock
    private JurisdictionEntity jurisdiction;

    @Mock
    private AuthorisationCaseTypeParser authorisationCaseTypeParser;

    @Mock
    private AuthorisationCaseFieldParser authorisationCaseFieldParser;

    @Mock
    private AuthorisationComplexTypeParser authorisationComplexTypeParser;

    @Mock
    private AuthorisationCaseEventParser authorisationCaseEventParser;

    @Mock
    private AuthorisationCaseStateParser authorisationCaseStateParser;

    @Mock
    private MetadataCaseFieldParser metadataCaseFieldParser;

    @Mock
    private CaseRoleParser caseRoleParser;

    @Mock
    private SearchAliasFieldParser searchAliasFieldParser;

    @BeforeEach
    void setup() {

        init();

        parseContext = mock(ParseContext.class);
        caseTypeParser = new CaseTypeParser(
            parseContext,
            caseFieldParser,
            stateParser,
            eventParser,
            authorisationCaseTypeParser,
            authorisationCaseFieldParser,
            authorisationComplexTypeParser,
            authorisationCaseEventParser,
            authorisationCaseStateParser,
            metadataCaseFieldParser,
            caseRoleParser,
            searchAliasFieldParser);
        lenient().doReturn(jurisdiction).when(parseContext).getJurisdiction();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void shouldDefaultCaseTypesOrderToNull(String value) {
        addCaseTypeWithOrder(CASE_TYPE_UNDER_TEST, value);

        assertThat(caseTypeParser.parseAll(definitionSheets).getAllResults())
            .singleElement()
            .satisfies(entity -> assertThat(entity.getCaseTypesOrder()).isNull());
    }

    @Test
    void shouldDefaultMissingCaseTypesOrderColumnToNull() {
        DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        item.addAttribute(ColumnName.ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.NAME, "Test Address Book Case");
        definitionSheet.addDataItem(item);
        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);

        assertThat(caseTypeParser.parseAll(definitionSheets).getAllResults())
            .singleElement()
            .satisfies(entity -> assertThat(entity.getCaseTypesOrder()).isNull());
    }

    @ParameterizedTest
    @CsvSource({"1, 1", "42, 42", "99, 99", "' 42 ', 42", "42.0, 42"})
    void shouldParseWholeNumberCaseTypesOrder(String value, int expected) {
        addCaseTypeWithOrder(CASE_TYPE_UNDER_TEST, value);

        assertThat(caseTypeParser.parseAll(definitionSheets).getAllResults())
            .singleElement()
            .satisfies(entity -> assertThat(entity.getCaseTypesOrder()).isEqualTo(expected));
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "-1", "0", "100", "1.5", "2147483648", "NaN", "Infinity"})
    void shouldRejectInvalidCaseTypesOrder(String value) {
        addCaseTypeWithOrder(CASE_TYPE_UNDER_TEST, value);

        assertThatThrownBy(() -> caseTypeParser.parseAll(definitionSheets))
            .isInstanceOf(MapperException.class)
            .hasMessageContaining(value)
            .hasMessageContaining("CaseTypesOrder")
            .hasMessageContaining("CaseType")
            .hasMessageContaining(CASE_TYPE_UNDER_TEST);
    }

    @ParameterizedTest
    @CsvSource({"7, 42", "42, 42"})
    void shouldParseIndependentCaseTypesOrders(int first, int second) {
        addCaseTypeWithOrder("First", Integer.toString(first));
        addCaseTypeWithOrder("Second", Integer.toString(second));

        assertThat(caseTypeParser.parseAll(definitionSheets).getAllResults())
            .extracting(CaseTypeEntity::getReference, CaseTypeEntity::getCaseTypesOrder)
            .containsExactlyInAnyOrder(tuple("First", first), tuple("Second", second));
    }

    private void addCaseTypeWithOrder(String id, String value) {
        DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        item.addAttribute(ColumnName.ID, id);
        item.addAttribute(ColumnName.NAME, "Test Address Book Case");
        item.addAttribute(ColumnName.CASE_TYPES_ORDER, value);
        definitionSheet.addDataItem(item);
        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);
    }

    @Test
    void shouldParseCaseTypeEntity_whenDataIsGood() {
        final DefinitionDataItem caseTypeItem = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        caseTypeItem.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        caseTypeItem.addAttribute(ColumnName.NAME.toString(), "Test Address Book Case");

        definitionSheet.addDataItem(caseTypeItem);
        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);

        final ParseResult<CaseTypeEntity> parseResult = caseTypeParser.parseAll(definitionSheets);
        assertThat(parseResult.getAllResults()).hasSize(1);

        final CaseTypeEntity caseTypeEntity = parseResult.getAllResults().getFirst();

        InOrder inOrder = inOrder(caseFieldParser,
            stateParser,
            metadataCaseFieldParser,
            eventParser,
            searchAliasFieldParser,
            authorisationCaseTypeParser);

        assertThat(caseTypeEntity.getId()).isNull();
        assertThat(caseTypeEntity.getJurisdiction()).isEqualTo(jurisdiction);
        assertThat(caseTypeEntity.getName()).isEqualTo("Test Address Book Case");
        assertThat(caseTypeEntity.getReference()).isEqualTo(CASE_TYPE_UNDER_TEST);
        inOrder.verify(caseFieldParser).parseAll(definitionSheets, caseTypeEntity);
        inOrder.verify(stateParser).parseAll(definitionSheets, caseTypeEntity);
        inOrder.verify(metadataCaseFieldParser).parseAll(caseTypeEntity);
        inOrder.verify(eventParser).parseAll(definitionSheets, caseTypeEntity);
        inOrder.verify(searchAliasFieldParser).parseAll(definitionSheets, caseTypeEntity);
        inOrder.verify(authorisationCaseTypeParser).parseAll(definitionSheets, caseTypeEntity);
    }

    @Test
    void shouldParseCaseTypeEntityAndWebhook_whenDataIsGood() {
        final DefinitionDataItem caseTypeItem = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        caseTypeItem.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        caseTypeItem.addAttribute(ColumnName.NAME.toString(), "Test Address Book Case");
        caseTypeItem.addAttribute(ColumnName.PRINTABLE_DOCUMENTS_URL.toString(), "n g i t b");

        definitionSheet.addDataItem(caseTypeItem);
        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);

        final ParseResult<CaseTypeEntity> parseResult = caseTypeParser.parseAll(definitionSheets);
        assertThat(parseResult.getAllResults()).hasSize(1);

        final CaseTypeEntity caseTypeEntity = parseResult.getAllResults().getFirst();

        assertThat(caseTypeEntity.getId()).isNull();
        assertThat(caseTypeEntity.getJurisdiction()).isEqualTo(jurisdiction);
        assertThat(caseTypeEntity.getName()).isEqualTo("Test Address Book Case");
        assertThat(caseTypeEntity.getReference()).isEqualTo(CASE_TYPE_UNDER_TEST);

        final WebhookEntity printWebhook = caseTypeEntity.getPrintWebhook();
        assertThat(printWebhook.getUrl()).isEqualTo("n g i t b");
        assertThat(printWebhook.getTimeouts()).isEmpty();
    }

    @Test
    void shouldParseGetCaseWebhook_whenDataIsGood() {
        final DefinitionDataItem caseTypeItem = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        caseTypeItem.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        caseTypeItem.addAttribute(ColumnName.NAME.toString(), "Test Address Book Case");
        caseTypeItem.addAttribute(ColumnName.CALLBACK_GET_CASE_URL.toString(), "Get Case Test Url");
        caseTypeItem.addAttribute(ColumnName.RETRIES_GET_CASE_URL.toString(), "6,8,7,5");

        definitionSheet.addDataItem(caseTypeItem);
        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);

        final ParseResult<CaseTypeEntity> parseResult = caseTypeParser.parseAll(definitionSheets);
        assertThat(parseResult.getAllResults()).hasSize(1);

        final CaseTypeEntity caseTypeEntity = parseResult.getAllResults().getFirst();

        assertThat(caseTypeEntity.getId()).isNull();
        assertThat(caseTypeEntity.getJurisdiction()).isEqualTo(jurisdiction);
        assertThat(caseTypeEntity.getName()).isEqualTo("Test Address Book Case");
        assertThat(caseTypeEntity.getReference()).isEqualTo(CASE_TYPE_UNDER_TEST);

        final WebhookEntity getCaseWebhook = caseTypeEntity.getGetCaseWebhook();
        assertThat(getCaseWebhook.getUrl()).isEqualTo("Get Case Test Url");
        assertThat(getCaseWebhook.getTimeouts()).hasSize(4);
    }

    @Test
    void shouldFail_whenDuplicateCaseTypeId() {
        final DefinitionDataItem caseTypeItem = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        caseTypeItem.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        caseTypeItem.addAttribute(ColumnName.NAME.toString(), "Test Address Book Case");

        final DefinitionDataItem duplicate = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        duplicate.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        duplicate.addAttribute(ColumnName.NAME.toString(), "Duplicate");

        definitionSheet.addDataItem(caseTypeItem);
        definitionSheet.addDataItem(duplicate);

        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);
        assertThatThrownBy(() -> caseTypeParser.parseAll(definitionSheets))
            .isInstanceOf(MapperException.class)
            .hasMessage("Multiple case type definitions for ID: Some Case Type");
    }
}
