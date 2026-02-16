package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SearchPartyParserTest extends ParserTestBase {

    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String CASE_TYPE_ID_2 = "TestCaseTypeID_2";

    private static final String SEARCH_PARTY_NAME_1 = "Test Name 1";
    private static final String SEARCH_PARTY_NAME_2 = "Test Name 2";
    private static final String SEARCH_PARTY_EMAIL_ADDRESS = "email@mail.com";
    private static final String SEARCH_PARTY_ADDRESS_LINE_1 = "TestAddressLine1";
    private static final String SEARCH_PARTY_POST_CODE = "W23 3UJ";
    private static final String SEARCH_PARTY_DOB = "10/10/1920";
    private static final String SEARCH_PARTY_DOD = "10/10/1920";

    private static final String SEARCH_PARTY_COLLECTION_FIELD_NAME = "Name";

    private SearchPartyParser parser;

    @BeforeEach
    void setUp() {
        init();
        parseContext = mock(ParseContext.class);
        parser = new SearchPartyParser();

        definitionSheets.put(SheetName.SEARCH_PARTY.getName(), definitionSheet);
        CaseTypeEntity caseTypeEntity1 = mock(CaseTypeEntity.class);
        lenient().when(caseTypeEntity1.getReference()).thenReturn(CASE_TYPE_ID_1);
        CaseTypeEntity caseTypeEntity2 = mock(CaseTypeEntity.class);
        lenient().when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_ID_2);
        Set<CaseTypeEntity> caseTypes = Sets.newHashSet(caseTypeEntity1, caseTypeEntity2);
        lenient().when(parseContext.getCaseTypes()).thenReturn(caseTypes);
    }

    @Test
    void shouldSuccessfullyCreateSearchPartyEntity() {
        SearchPartyEntity searchPartyEntity = parser.createSearchPartyEntity(parseContext,
            buildSearchPartyDefinitionDataItem(CASE_TYPE_ID_1,
            SEARCH_PARTY_NAME_1));
        assertEquals(CASE_TYPE_ID_1, searchPartyEntity.getCaseType().getReference());
        assertEquals(SEARCH_PARTY_NAME_1, searchPartyEntity.getSearchPartyName());
        assertEquals(SEARCH_PARTY_EMAIL_ADDRESS, searchPartyEntity.getSearchPartyEmailAddress());
        assertEquals(SEARCH_PARTY_ADDRESS_LINE_1, searchPartyEntity.getSearchPartyAddressLine1());
        assertEquals(SEARCH_PARTY_POST_CODE, searchPartyEntity.getSearchPartyPostCode());
        assertEquals(SEARCH_PARTY_DOB, searchPartyEntity.getSearchPartyDob());
        assertEquals(SEARCH_PARTY_DOD, searchPartyEntity.getSearchPartyDod());
        assertEquals(SEARCH_PARTY_COLLECTION_FIELD_NAME, searchPartyEntity.getSearchPartyCollectionFieldName());
    }

    @Test
    void shouldFailToValidateCaseTypeIdWhenCreatingSearchPartyEntity() {
        Assertions.assertThrows(InvalidImportException.class, () -> parser.createSearchPartyEntity(parseContext,
            buildSearchPartyDefinitionDataItem("InvalidCaseTypeId3",
            SEARCH_PARTY_NAME_1)));
    }

    @Test
    void shouldParseValidSearchPartyEntities() {
        definitionSheet.addDataItem(buildSearchPartyDefinitionDataItem(CASE_TYPE_ID_1,
            SEARCH_PARTY_NAME_1));
        definitionSheet.addDataItem(buildSearchPartyDefinitionDataItem(CASE_TYPE_ID_2,
            SEARCH_PARTY_NAME_2));
        List<SearchPartyEntity> entityList = parser.parse(definitionSheets, parseContext);
        assertEquals(2, entityList.size());
    }

    @Test
    void shouldThrowExceptionWhenCaseTypeNotFound() {
        definitionSheet.addDataItem(buildSearchPartyDefinitionDataItem(CASE_TYPE_ID_1,
            SEARCH_PARTY_NAME_1));
        definitionSheet.addDataItem(buildSearchPartyDefinitionDataItem("InvalidCaseTypeId3",
            SEARCH_PARTY_NAME_2));
        Assertions.assertThrows(ValidationException.class, () -> parser.parse(definitionSheets, parseContext));
    }

    private DefinitionDataItem buildSearchPartyDefinitionDataItem(String caseTypeId,
                                                                  String searchPartyName) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.SEARCH_PARTY.toString());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), caseTypeId);
        item.addAttribute(ColumnName.SEARCH_PARTY_NAME.toString(), searchPartyName);
        item.addAttribute(ColumnName.SEARCH_PARTY_EMAIL_ADDRESS.toString(), SEARCH_PARTY_EMAIL_ADDRESS);
        item.addAttribute(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1.toString(), SEARCH_PARTY_ADDRESS_LINE_1);
        item.addAttribute(ColumnName.SEARCH_PARTY_POST_CODE.toString(), SEARCH_PARTY_POST_CODE);
        item.addAttribute(ColumnName.SEARCH_PARTY_DOB.toString(), SEARCH_PARTY_DOB);
        item.addAttribute(ColumnName.SEARCH_PARTY_DOD.toString(), SEARCH_PARTY_DOD);
        item.addAttribute(ColumnName.SEARCH_PARTY_COLLECTION_FIELD_NAME.toString(), SEARCH_PARTY_COLLECTION_FIELD_NAME);
        return item;
    }
}
