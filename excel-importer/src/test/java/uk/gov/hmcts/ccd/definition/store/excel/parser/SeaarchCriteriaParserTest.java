package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeaarchCriteriaParserTest extends ParserTestBase {

    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String CASE_TYPE_ID_2 = "TestCaseTypeID_2";

    private static final String OTHER_CASE_REFERENCE = "OtherCaseReference";

    private SearchCriteriaParser parser;

    @BeforeEach
    public void setUp() {
        init();
        parseContext = mock(ParseContext.class);
        parser = new SearchCriteriaParser();

        definitionSheets.put(SheetName.SEARCH_CRITERIA.getName(), definitionSheet);
        CaseTypeEntity caseTypeEntity1 = mock(CaseTypeEntity.class);
        when(caseTypeEntity1.getReference()).thenReturn(CASE_TYPE_ID_1);
        CaseTypeEntity caseTypeEntity2 = mock(CaseTypeEntity.class);
        when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_ID_2);
        Set<CaseTypeEntity> caseTypes = Sets.newHashSet(caseTypeEntity1, caseTypeEntity2);
        given(parseContext.getCaseTypes()).willReturn(caseTypes);
    }

    @Test
    void shouldParseValidSearchCriteriaEntities() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            OTHER_CASE_REFERENCE));
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_2,
            OTHER_CASE_REFERENCE));
        List<SearchCriteriaEntity> entityList = parser.parse(definitionSheets, parseContext);
        assertEquals(2, entityList.size());
    }

    @Test
    void shouldThrowExceptionWhenCaseTypeNotFound() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            OTHER_CASE_REFERENCE));
        definitionSheet.addDataItem(buildDefinitionDataItem("InvalidCaseTypeId3",
            OTHER_CASE_REFERENCE));
        Assertions.assertThrows(ValidationException.class, () -> parser.parse(definitionSheets, parseContext));
    }

    private DefinitionDataItem buildDefinitionDataItem(String caseTypeId,
                                                       String otherCaseReference) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.SEARCH_CRITERIA.toString());
        item.addAttribute(ColumnName.OTHER_CASE_REFERENCE.toString(), otherCaseReference);
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), caseTypeId);
        return item;
    }
}
