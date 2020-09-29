package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NoCConfigEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class NoCConfigParserTest extends ParserTestBase {

    private static final String CASE_TYPE_ID_1 = "FT_MasterCaseType_1";
    private static final String CASE_TYPE_ID_2 = "FT_MasterCaseType_2";
    private static final String CASE_TYPE_ID_3 = "FT_MasterCaseType_3";
    private static final String CASE_TYPE_ID_4 = "FT_MasterCaseType_4";

    private NoCConfigParser configParser;

    @BeforeEach
    void setUp() {
        init();
        parseContext = mock(ParseContext.class);
        configParser = new NoCConfigParser(parseContext);
        definitionSheets.put(SheetName.NOC_CONFIG.getName(), definitionSheet);
        Set<CaseTypeEntity> caseTypeEntities = caseTypeEntities();
        given(parseContext.getCaseTypes()).willReturn(caseTypeEntities);
    }

    private Set<CaseTypeEntity> caseTypeEntities() {
        CaseTypeEntity caseTypeEntity1 = mock(CaseTypeEntity.class);
        when(caseTypeEntity1.getReference()).thenReturn(CASE_TYPE_ID_1);
        CaseTypeEntity caseTypeEntity2 = mock(CaseTypeEntity.class);
        when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_ID_2);

        return Sets.newHashSet(caseTypeEntity1, caseTypeEntity2);
    }

    @Test
    @DisplayName("Should parse the noc config content for the two case types with single entry each")
    void shouldParse_whenOnlyOneNocConfigPerCaseType() {
        definitionSheet.addDataItem(buildDefinitionDataItem(
            CASE_TYPE_ID_1, false, false));
        definitionSheet.addDataItem(buildDefinitionDataItem(
            CASE_TYPE_ID_2, true, false));
        Map<String, List<NoCConfigEntity>> nocConfigs = configParser.parse(definitionSheets);
        Assertions.assertNotNull(nocConfigs);
        Assertions.assertEquals(2, nocConfigs.size());
    }

    @Test
    @DisplayName("Should parse the noc config content for the case type with single entry")
    void shouldParse_whenOnlyOneNocConfigForCaseType() {
        definitionSheet.addDataItem(buildDefinitionDataItem(
            CASE_TYPE_ID_1, false, false));
        Map<String, List<NoCConfigEntity>> nocConfigs = configParser.parse(definitionSheets);
        Assertions.assertNotNull(nocConfigs);
        Assertions.assertEquals(2, nocConfigs.size());
        Assertions.assertEquals(0, nocConfigs.get(CASE_TYPE_ID_2).size());
    }

    @Test
    @DisplayName("Should throw exception when noc config tab has single unknown case type")
    void shouldThrow_Exception_For_Non_Matching_CaseTypeIds() {
        definitionSheet.addDataItem(buildDefinitionDataItem(
            CASE_TYPE_ID_3, false, false));
        MapperException thrown = assertThrows(MapperException.class, () -> configParser.parse(definitionSheets));
        assertEquals("Unknown Case Type(s) 'FT_MasterCaseType_3' in worksheet 'NoticeOfChangeConfig'",
            thrown.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when noc config tab has multiple unknown case types")
    void shouldThrow_Exception_For_Non_Matching_Multiple_CaseTypeIds() {
        definitionSheet.addDataItem(buildDefinitionDataItem(
            CASE_TYPE_ID_3, false, false));
        definitionSheet.addDataItem(buildDefinitionDataItem(
            CASE_TYPE_ID_4, false, false));
        MapperException thrown = assertThrows(MapperException.class, () -> configParser.parse(definitionSheets));
        assertEquals("Unknown Case Type(s) 'FT_MasterCaseType_3,FT_MasterCaseType_4' "
            + "in worksheet 'NoticeOfChangeConfig'", thrown.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid noc config values")
    void shouldThrow_Exception_For_Invalid_Noc_ConfigValues() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_2,
            "invalidReason",
            "invalidNocAction"));
        ValidationException thrown = assertThrows(
            ValidationException.class, () -> configParser.parse(definitionSheets));
        assertNotNull(thrown);
        assertNotNull(thrown.getValidationResult());
        List<ValidationError> validationErrors = thrown.getValidationResult().getValidationErrors();
        assertEquals(2, validationErrors.size());
    }

    private DefinitionDataItem buildDefinitionDataItem(String caseTypeId,
                                                       boolean reasonsRequired,
                                                       boolean nocActionInterpretationRequired) {
        return buildDefinitionDataItem(
            caseTypeId, String.valueOf(reasonsRequired), String.valueOf(nocActionInterpretationRequired));
    }

    private DefinitionDataItem buildDefinitionDataItem(String caseTypeId,
                                                       String reasonsRequired,
                                                       String nocActionInterpretationRequired) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.NOC_CONFIG.toString());
        item.addAttribute(ColumnName.REASON_REQUIRED.toString(), reasonsRequired);
        item.addAttribute(ColumnName.NOC_ACTION_INTERPRETATION_REQUIRED.toString(), nocActionInterpretationRequired);
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), caseTypeId);
        return item;
    }
}
