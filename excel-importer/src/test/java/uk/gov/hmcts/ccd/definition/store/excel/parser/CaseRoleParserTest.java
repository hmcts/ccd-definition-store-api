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
    private static final String CASE_ROLE_ID = "Some case role Id";
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
    @DisplayName("parse all state entities")
    void testParseAll() {
        definitionSheets.put(CASE_ROLE.getName(), buildCaseRoleSheet());
        final Collection<CaseRoleEntity> caseRoleEntities = underTest.parseAll(definitionSheets, caseTypeEntity);
        assertAll(() -> assertThat(caseRoleEntities, hasSize(1)),
                  () -> {
                      final CaseRoleEntity s = new ArrayList<>(caseRoleEntities).get(0);
                      assertThat(s.getReference(), is(CASE_ROLE_ID));
                      assertThat(s.getCaseType(), is(nullValue()));
                      assertThat(s.getDescription(), is(DESCRIPTION));
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
        return sheet;
    }
}
