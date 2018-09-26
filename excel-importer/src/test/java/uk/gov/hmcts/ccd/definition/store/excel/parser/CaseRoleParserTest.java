package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_ROLE;

@DisplayName("Case Role Parser Test")
class CaseRoleParserTest {

    private static final String CASE_TYPE_ID = "Some case type";
    private static final String CASE_ROLE_ID = "[Some case role Id]";
    private static final String DESCRIPTION = "Some description";

    private CaseRoleParser underTest;
    private Map<String, DefinitionSheet> definitionSheets;
    private CaseTypeEntity caseTypeEntity;
    private ParseContext context;

    @BeforeEach
    void init() {
        caseTypeEntity = buildCaseTypeEntity();

        context = new ParseContext();
        context.registerCaseType(caseTypeEntity);

        definitionSheets = new HashMap<>();

        underTest = new CaseRoleParser(context);
    }

    @Test
    @DisplayName("should be OK - parse valid case roles entities")
    void testParseAll() {
        definitionSheets.put(CASE_ROLE.getName(), buildCaseRoleSheet());
        final Collection<CaseRoleEntity> caseRoleEntities = underTest.parseAll(definitionSheets, caseTypeEntity);
        assertAll(() -> assertThat(caseRoleEntities, hasSize(2)),
            () -> {
                final CaseRoleEntity s = new ArrayList<>(caseRoleEntities).get(0);
                assertThat(s.getReference(), is(CASE_ROLE_ID));
                assertThat(s.getCaseType(), is(nullValue()));
                assertThat(s.getDescription(), is(DESCRIPTION));
                assertThat(context.getCaseRoleForCaseType(CASE_TYPE_ID, CASE_ROLE_ID), is(s));
            },
            () -> {
                final CaseRoleEntity s = new ArrayList<>(caseRoleEntities).get(1);
                assertThat(s.getReference(), is(CASE_ROLE_ID + "_2"));
                assertThat(s.getCaseType(), is(nullValue()));
                assertThat(s.getDescription(), is(DESCRIPTION + "_2"));
                assertThat(context.getCaseRoleForCaseType(CASE_TYPE_ID, CASE_ROLE_ID + "_2"), is(s));
            });
    }

    @Test
    @DisplayName("should be OK - no CaseRoles worksheet")
    void testParseNoWorksheet() {
        final Collection<CaseRoleEntity> caseRoleEntities = underTest.parseAll(definitionSheets, caseTypeEntity);
        assertThat(caseRoleEntities, hasSize(0));
    }

    @Test
    @DisplayName("should be OK - no CaseRoles in the CaseRoles worksheet")
    void testParseNoCaseRoleInWorksheet() {
        definitionSheets.put(CASE_ROLE.getName(), new DefinitionSheet());
        final Collection<CaseRoleEntity> caseRoleEntities = underTest.parseAll(definitionSheets, caseTypeEntity);
        assertThat(caseRoleEntities, hasSize(0));
    }

    @Test
    @DisplayName("should be OK - case role description is empty")
    void testParseNoDescriptionForCaseRole() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(CASE_ROLE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.ID, CASE_ROLE_ID);
        sheet.addDataItem(item);
        definitionSheets.put(CASE_ROLE.getName(), sheet);
        final Collection<CaseRoleEntity> caseRoleEntities = underTest.parseAll(definitionSheets, caseTypeEntity);
        assertAll(() -> assertThat(caseRoleEntities, hasSize(1)),
            () -> {
                final CaseRoleEntity s = new ArrayList<>(caseRoleEntities).get(0);
                assertThat(s.getReference(), is(CASE_ROLE_ID));
                assertThat(s.getCaseType(), is(nullValue()));
                assertThat(s.getDescription(), is(nullValue()));
                assertThat(context.getCaseRoleForCaseType(CASE_TYPE_ID, CASE_ROLE_ID), is(s));
            });
    }

    private CaseTypeEntity buildCaseTypeEntity() {
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_ID);
        return caseType;
    }

    private DefinitionSheet buildCaseRoleSheet() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(CASE_ROLE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.ID, CASE_ROLE_ID);
        item.addAttribute(ColumnName.DESCRIPTION, DESCRIPTION);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(CASE_ROLE.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item2.addAttribute(ColumnName.ID, CASE_ROLE_ID + "_2");
        item2.addAttribute(ColumnName.DESCRIPTION, DESCRIPTION + "_2");
        sheet.addDataItem(item2);
        return sheet;
    }
}
