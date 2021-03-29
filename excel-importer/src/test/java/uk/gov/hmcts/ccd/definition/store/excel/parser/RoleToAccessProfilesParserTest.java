package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class RoleToAccessProfilesParserTest extends ParserTestBase {

    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String CASE_TYPE_ID_2 = "TestCaseTypeID_2";

    private RoleToAccessProfilesParser parser;

    @BeforeEach
    void setUp() {
        init();
        parseContext = mock(ParseContext.class);
        parser = new RoleToAccessProfilesParser();

        definitionSheets.put(SheetName.ROLE_TO_ACCESS_PROFILES.getName(), definitionSheet);
        CaseTypeEntity caseTypeEntity1 = mock(CaseTypeEntity.class);
        when(caseTypeEntity1.getReference()).thenReturn(CASE_TYPE_ID_1);
        CaseTypeEntity caseTypeEntity2 = mock(CaseTypeEntity.class);
        when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_ID_2);
        Set<CaseTypeEntity> caseTypes = Sets.newHashSet(caseTypeEntity1, caseTypeEntity2);
        given(parseContext.getCaseTypes()).willReturn(caseTypes);
        UserRoleEntity userRoleEntity = mock(UserRoleEntity.class);
        when(parseContext.getRole(anyString(), anyString())).thenReturn(Optional.of(userRoleEntity));
    }

    @Test
    void shouldParseValidRoleToAccessProfileEntities() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            "Test Role 1", "judge", "Y", "N"));
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_2,
            "Test Role 2", "solicitor", "Y", "N"));
        List<RoleToAccessProfilesEntity> entityList = parser.parse(definitionSheets, parseContext);
        assertEquals(2, entityList.size());
    }

    @Test
    void shouldThrowExceptionWhenInvalidReadOnlyValue() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            "Test Role 1", "judge", "Y", "N"));
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_2,
            "Test Role 2", "solicitor", "TT", "N"));
        Assertions.assertThrows(MapperException.class, () -> parser.parse(definitionSheets, parseContext));
    }

    @Test
    void shouldThrowExceptionWhenInvalidDisabledValue() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            "Test Role 1", "judge", "Y", "N"));
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_2,
            "Test Role 2", "solicitor", "Y", "TT"));
        Assertions.assertThrows(MapperException.class, () -> parser.parse(definitionSheets, parseContext));
    }

    @Test
    void shouldThrowExceptionWhenCaseTypeNotFound() {
        definitionSheet.addDataItem(buildDefinitionDataItem(CASE_TYPE_ID_1,
            "Test Role 1", "judge", "Y", "N"));
        definitionSheet.addDataItem(buildDefinitionDataItem("InvalidCaseTypeId3",
            "Test Role 2", "solicitor", "Y", "TT"));
        Assertions.assertThrows(ValidationException.class, () -> parser.parse(definitionSheets, parseContext));
    }

    private DefinitionDataItem buildDefinitionDataItem(String caseTypeId,
                                                       String roleName, String accessProfiles,
                                                       String readOnly, String disabled) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.ROLE_TO_ACCESS_PROFILES.toString());
        item.addAttribute(ColumnName.ROLE_NAME.toString(), roleName);
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), caseTypeId);
        item.addAttribute(ColumnName.ACCESS_PROFILES.toString(), accessProfiles);
        item.addAttribute(ColumnName.READ_ONLY.toString(), readOnly);
        item.addAttribute(ColumnName.DISABLED.toString(), disabled);
        item.addAttribute(ColumnName.AUTHORISATION.toString(), "Authorised");
        return item;
    }
}
