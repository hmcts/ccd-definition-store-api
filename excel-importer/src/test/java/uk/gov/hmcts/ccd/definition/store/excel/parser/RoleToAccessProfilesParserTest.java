package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleToAccessProfilesParserTest extends ParserTestBase {

    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String CASE_TYPE_ID_2 = "TestCaseTypeID_2";

    private RoleToAccessProfilesParser underTest;

    @BeforeEach
    void setUp() {
        init();
        parseContext = mock(ParseContext.class);
        underTest = new RoleToAccessProfilesParser();

        definitionSheets.put(SheetName.ROLE_TO_ACCESS_PROFILES.getName(), definitionSheet);
        final CaseTypeEntity caseTypeEntity1 = mock(CaseTypeEntity.class);
        when(caseTypeEntity1.getReference()).thenReturn(CASE_TYPE_ID_1);
        final CaseTypeEntity caseTypeEntity2 = mock(CaseTypeEntity.class);
        when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_ID_2);
        final Set<CaseTypeEntity> caseTypes = Set.of(caseTypeEntity1, caseTypeEntity2);
        given(parseContext.getCaseTypes()).willReturn(caseTypes);
        final AccessProfileEntity accessProfileEntity = mock(AccessProfileEntity.class);
        when(parseContext.getAccessProfile(anyString(), anyString())).thenReturn(Optional.of(accessProfileEntity));
    }

    @Test
    void shouldParseValidRoleToAccessProfileEntities() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            "Test Role 1", "judge", "Y", "N"));
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_2,
            "Test Role 2", "solicitor", "Y", "N"));
        final List<RoleToAccessProfilesEntity> entityList = underTest.parse(definitionSheets, parseContext);

        assertEquals(2, entityList.size());
        entityList.forEach(entity -> {
            assertNotNull(entity.getRoleName());
            assertNotNull(entity.getAuthorisation());
            assertNotNull(entity.getCaseType());
            assertNotNull(entity.getAccessProfiles());
            assertNotNull(entity.getReadOnly());
            assertNotNull(entity.getDisabled());
            assertNotNull(entity.getCaseAccessCategories());
        });
    }

    @Test
    void shouldThrowExceptionWhenInvalidReadOnlyValue() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            "Test Role 1", "judge", "Y", "N"));
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_2,
            "Test Role 2", "solicitor", "TT", "N"));
        assertThrows(MapperException.class, () -> underTest.parse(definitionSheets, parseContext));
    }

    @Test
    void shouldThrowExceptionWhenInvalidDisabledValue() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            "Test Role 1", "judge", "Y", "N"));
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_2,
            "Test Role 2", "solicitor", "Y", "TT"));
        assertThrows(MapperException.class, () -> underTest.parse(definitionSheets, parseContext));
    }

    @Test
    void shouldThrowExceptionWhenCaseTypeNotFound() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            "Test Role 1", "judge", "Y", "N"));
        definitionSheet.addDataItem(buildDefinitionDataItem("InvalidCaseTypeId3",
            "Test Role 2", "solicitor", "Y", "TT"));
        assertThrows(ValidationException.class, () -> underTest.parse(definitionSheets, parseContext));
    }

    private DefinitionDataItem buildDefinitionDataItem(final String caseTypeId,
                                                       final String roleName,
                                                       final String accessProfiles,
                                                       final String readOnly,
                                                       final String disabled) {
        final String accessCategories = "";
        final String authorised = "Authorised";

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.ROLE_TO_ACCESS_PROFILES.toString());
        item.addAttribute(ColumnName.ROLE_NAME, roleName);
        item.addAttribute(ColumnName.CASE_TYPE_ID, caseTypeId);
        item.addAttribute(ColumnName.ACCESS_PROFILES, accessProfiles);
        item.addAttribute(ColumnName.READ_ONLY, readOnly);
        item.addAttribute(ColumnName.DISABLED, disabled);
        item.addAttribute(ColumnName.AUTHORISATION, authorised);
        item.addAttribute(ColumnName.CASE_ACCESS_CATEGORIES, accessCategories);

        return item;
    }
}
