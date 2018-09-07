package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.STATE;

@DisplayName("State Parser Test")
class StateParserTest {

    private static final String CASE_TYPE_ID = "N>G>I>T>B.";
    private static final String STATE_ID = "how many more tests to get over 80%";
    private static final String TITLE_DISPLAY = "${lastName} ${[CASE_REFERENCE]}";
    private static final String EXPECTED_TITLE_DISPLAY = "${lastName} ${[CASE_REFERENCE]}";

    private StateParser underTest;
    private Map<String, DefinitionSheet> definitionSheets;
    private CaseTypeEntity caseTypeEntity;
    private ParseContext context;

    @BeforeEach
    void init() {
        caseTypeEntity = buildCaseTypeEntity();

        context = new ParseContext();
        context.registerCaseType(caseTypeEntity);

        definitionSheets = new HashMap<>();
        definitionSheets.put(STATE.getName(), buildStateSheet());

        underTest = new StateParser(context);
    }

    @Test
    @DisplayName("parse all state entities")
    void testParseAll() {
        final Collection<StateEntity> stateEntities = underTest.parseAll(definitionSheets, caseTypeEntity);
        assertAll(() -> assertThat(stateEntities, hasSize(1)),
                  () -> {
                      final StateEntity s = new ArrayList<>(stateEntities).get(0);
                      assertThat(s.getReference(), is(STATE_ID));
                      assertThat(s.getCaseType(), is(nullValue()));
                      assertThat(s.getTitleDisplay(), is(EXPECTED_TITLE_DISPLAY));
                      assertThat(context.getStateForCaseType(CASE_TYPE_ID, STATE_ID), is(s));
                  });
    }

    private CaseTypeEntity buildCaseTypeEntity() {
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_ID);
        return caseType;
    }

    private DefinitionSheet buildStateSheet() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(STATE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.ID, STATE_ID);
        item.addAttribute(ColumnName.TITLE_DISPLAY, TITLE_DISPLAY);
        sheet.addDataItem(item);
        return sheet;
    }
}
