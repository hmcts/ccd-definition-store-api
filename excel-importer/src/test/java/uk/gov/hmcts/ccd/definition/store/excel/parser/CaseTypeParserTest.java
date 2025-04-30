package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CaseTypeParserTest extends ParserTestBase {

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
    public void setup() {

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

    @Test
    public void shouldParseCaseTypeEntity_whenDataIsGood() {
        final DefinitionDataItem caseTypeItem = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        caseTypeItem.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        caseTypeItem.addAttribute(ColumnName.NAME.toString(), "Test Address Book Case");

        definitionSheet.addDataItem(caseTypeItem);
        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);

        final ParseResult<CaseTypeEntity> parseResult = caseTypeParser.parseAll(definitionSheets);
        assertThat(parseResult.getAllResults().size(), is(1));

        final CaseTypeEntity caseTypeEntity = parseResult.getAllResults().get(0);

        InOrder inOrder = inOrder(caseFieldParser,
                stateParser,
                metadataCaseFieldParser,
                eventParser,
                searchAliasFieldParser,
                authorisationCaseTypeParser);

        assertAll(
                () -> assertThat(caseTypeEntity.getId(), is(nullValue())),
                () -> assertThat(caseTypeEntity.getJurisdiction(), is(jurisdiction)),
                () -> assertThat(caseTypeEntity.getName(), is("Test Address Book Case")),
                () -> assertThat(caseTypeEntity.getReference(), is(CASE_TYPE_UNDER_TEST)),
                () -> inOrder.verify(caseFieldParser).parseAll(definitionSheets, caseTypeEntity),
                () -> inOrder.verify(stateParser).parseAll(definitionSheets, caseTypeEntity),
                () -> inOrder.verify(metadataCaseFieldParser).parseAll(caseTypeEntity),
                () -> inOrder.verify(eventParser).parseAll(definitionSheets, caseTypeEntity),
                () -> inOrder.verify(searchAliasFieldParser).parseAll(definitionSheets, caseTypeEntity),
                () -> inOrder.verify(authorisationCaseTypeParser).parseAll(definitionSheets, caseTypeEntity)
        );
    }

    @Test
    public void shouldParseCaseTypeEntityAndWebhook_whenDataIsGood() {
        final DefinitionDataItem caseTypeItem = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        caseTypeItem.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        caseTypeItem.addAttribute(ColumnName.NAME.toString(), "Test Address Book Case");
        caseTypeItem.addAttribute(ColumnName.PRINTABLE_DOCUMENTS_URL.toString(), "n g i t b");

        definitionSheet.addDataItem(caseTypeItem);
        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);

        final ParseResult<CaseTypeEntity> parseResult = caseTypeParser.parseAll(definitionSheets);
        assertThat(parseResult.getAllResults().size(), is(1));

        final CaseTypeEntity caseTypeEntity = parseResult.getAllResults().get(0);

        assertThat(caseTypeEntity.getId(), is(nullValue()));
        assertThat(caseTypeEntity.getJurisdiction(), is(jurisdiction));
        assertThat(caseTypeEntity.getName(), is("Test Address Book Case"));
        assertThat(caseTypeEntity.getReference(), is(CASE_TYPE_UNDER_TEST));

        final WebhookEntity printWebhook = caseTypeEntity.getPrintWebhook();
        assertThat(printWebhook.getUrl(), is("n g i t b"));
        assertThat(printWebhook.getTimeouts().size(), is(0));
    }

    @Test
    public void shouldParseGetCaseWebhook_whenDataIsGood() {
        final DefinitionDataItem caseTypeItem = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        caseTypeItem.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        caseTypeItem.addAttribute(ColumnName.NAME.toString(), "Test Address Book Case");
        caseTypeItem.addAttribute(ColumnName.CALLBACK_GET_CASE_URL.toString(), "Get Case Test Url");
        caseTypeItem.addAttribute(ColumnName.RETRIES_GET_CASE_URL.toString(), "6,8,7,5");

        definitionSheet.addDataItem(caseTypeItem);
        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);

        final ParseResult<CaseTypeEntity> parseResult = caseTypeParser.parseAll(definitionSheets);
        assertThat(parseResult.getAllResults().size(), is(1));

        final CaseTypeEntity caseTypeEntity = parseResult.getAllResults().get(0);

        assertThat(caseTypeEntity.getId(), is(nullValue()));
        assertThat(caseTypeEntity.getJurisdiction(), is(jurisdiction));
        assertThat(caseTypeEntity.getName(), is("Test Address Book Case"));
        assertThat(caseTypeEntity.getReference(), is(CASE_TYPE_UNDER_TEST));

        final WebhookEntity getCaseWebhook = caseTypeEntity.getGetCaseWebhook();
        assertThat(getCaseWebhook.getUrl(), is("Get Case Test Url"));
        assertThat(getCaseWebhook.getTimeouts().size(), is(4));
    }

    @Test
    public void shouldFail_whenDuplicateCaseTypeId() {
        final DefinitionDataItem caseTypeItem = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        caseTypeItem.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        caseTypeItem.addAttribute(ColumnName.NAME.toString(), "Test Address Book Case");

        final DefinitionDataItem duplicate = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        duplicate.addAttribute(ColumnName.ID.toString(), CASE_TYPE_UNDER_TEST);
        duplicate.addAttribute(ColumnName.NAME.toString(), "Duplicate");

        definitionSheet.addDataItem(caseTypeItem);
        definitionSheet.addDataItem(duplicate);

        definitionSheets.put(SheetName.CASE_TYPE.getName(), definitionSheet);
        SpreadsheetParsingException ex = assertThrows(SpreadsheetParsingException.class, () -> {
            final ParseResult<CaseTypeEntity> parseResult = caseTypeParser.parseAll(definitionSheets);
            assertThat(parseResult.getAllResults().size(), is(1));
            parseResult.getAllResults().get(0);
        });
        
        assertThat(ex.getMessage(), is("Multiple case type definitions for ID: Some Case Type"));
    }
}
