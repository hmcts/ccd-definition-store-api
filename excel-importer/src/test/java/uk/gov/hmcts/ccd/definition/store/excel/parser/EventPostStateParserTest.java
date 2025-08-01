package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventPostStateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EventPostStateParserTest extends ParserTestBase {

    private static final String STATE_DEFAULT = "DefaultPostState";

    private static final String VALID_ENABLING_CONDITION =
        "ApprovalRequired(FieldA!=\"\" AND FieldB=\"I'm innocent\"):1";

    private static final String VALID_MULTI_STATE_ENABLING_CONDITION =
        "ApprovalRequired(FieldA!=\"\" AND FieldB=\"I'm innocent\"):1;"
        + "ScheduleForHearing(FieldC=\"*\" AND FieldD=\"Plea Entered\"):2";

    private static final String NO_PRIORITY_ENABLING_CONDITION =
        "ApprovalRequired(FieldA!=\"\" AND FieldB=\"I'm innocent\")";

    private static final String IN_VALID_POST_STATE_CONDITION =
        "ApprovalRequired(FieldA!=\"\" AND FieldB\"I'm innocent\"):3";

    private EventPostStateParser postStateParser;

    private DefinitionSheet caseEventToFieldsSheet;

    @BeforeEach
    void setup() {
        init();
        parseContext = mock(ParseContext.class);
        postStateParser = new EventPostStateParser(parseContext, CASE_TYPE_UNDER_TEST);

        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);

        definitionSheets.put(SheetName.CASE_EVENT.getName(), definitionSheet);
        caseEventToFieldsSheet = new DefinitionSheet();
        definitionSheets.put(SheetName.CASE_EVENT_TO_FIELDS.getName(), caseEventToFieldsSheet);
        lenient().when(parseContext.getStateForCaseType(anyString(), anyString())).thenReturn(new StateEntity());
    }

    @Test
    void shouldParseEventPostStateConditionWithWildCard() {
        final List<EventPostStateEntity> eventEntities = postStateParser.parse("*");
        assertThat(eventEntities.size(), is(1));
        assertThat(eventEntities.get(0).getPostStateReference(), is("*"));
        assertThat(eventEntities.get(0).getEnablingCondition(), is(nullValue()));
        assertThat(eventEntities.get(0).getPriority(), is(99));
    }

    @Test
    void shouldParseEventPostStateConditionWithEmptyValue() {
        final List<EventPostStateEntity> eventEntities = postStateParser.parse("");
        assertThat(eventEntities.size(), is(0));
    }

    @Test
    void shouldParseEventPostStateConditionWithDefaultState() {
        final List<EventPostStateEntity> eventEntities = postStateParser.parse(STATE_DEFAULT);
        assertThat(eventEntities.size(), is(1));
        assertThat(eventEntities.get(0).getPostStateReference(), is(STATE_DEFAULT));
        assertThat(eventEntities.get(0).getEnablingCondition(), is(nullValue()));
        assertThat(eventEntities.get(0).getPriority(), is(99));
    }

    @Test
    void shouldParseEventPostStateConditionWithValidConditions() {
        final List<EventPostStateEntity> eventEntities = postStateParser.parse(VALID_ENABLING_CONDITION);
        assertThat(eventEntities.size(), is(1));
        assertThat(eventEntities.get(0).getPostStateReference(), is("ApprovalRequired"));
        assertThat(eventEntities.get(0).getEnablingCondition(), is("FieldA!=\"\" AND FieldB=\"I'm innocent\""));
        assertThat(eventEntities.get(0).getPriority(), is(1));
    }

    @Test
    void shouldParseEventPostStateConditionWithMultipleStates() {
        final List<EventPostStateEntity> eventEntities = postStateParser.parse(VALID_MULTI_STATE_ENABLING_CONDITION);
        assertThat(eventEntities.size(), is(2));
    }

    @Test
    void shouldParseEventPostStateConditionWithNoPriority() {
        SpreadsheetParsingException thrown = assertThrows(SpreadsheetParsingException.class,
            () -> postStateParser.parse(NO_PRIORITY_ENABLING_CONDITION));

        assertThat(thrown.getMessage(), is("Invalid Post State "
            + "ApprovalRequired(FieldA!=\"\" AND FieldB=\"I'm innocent\")"));
    }

    @Test
    void shouldParseEventPostStateConditionWithInvalidShowCondition() {
        SpreadsheetParsingException thrown = assertThrows(SpreadsheetParsingException.class,
            () -> postStateParser.parse(IN_VALID_POST_STATE_CONDITION));

        assertThat(thrown.getMessage(), is("Invalid Post State Condition "
            + "ApprovalRequired(FieldA!=\"\" AND FieldB\"I'm innocent\")"));
    }

}
