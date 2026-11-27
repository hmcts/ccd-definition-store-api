package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DecentralisedEventParserTest extends ParserTestBase {

    private final Map<String, List<String>> whitelists = new HashMap<>();
    private final EntityToDefinitionDataItemRegistry registry = mock(EntityToDefinitionDataItemRegistry.class);
    private EventParser parser;

    @BeforeEach
    void setUp() {
        init();
        parseContext = mock(ParseContext.class);
        caseType = new CaseTypeEntity();
        definitionSheets.put(SheetName.CASE_EVENT.getName(), definitionSheet);
        definitionSheets.put(SheetName.CASE_EVENT_TO_FIELDS.getName(), new DefinitionSheet());
        parser = new EventParser(parseContext, mock(EventCaseFieldParser.class),
            mock(EventCaseFieldComplexTypeParser.class), registry, new ShowConditionParser(), true, whitelists);
    }

    @ParameterizedTest
    @CsvSource({"PCS, pcs", "PcS, pcs", "OtherCaseType, othercasetype", "CIVIL, civil"})
    void shouldImportWhitelistedEventUsingLowercaseCaseType(String caseTypeId, String configKey) {
        whitelists.put(configKey, List.of("ext:allowed"));
        DefinitionDataItem row = addEvent(caseTypeId, "ext:allowed");

        Collection<EventEntity> events = parser.parseAll(definitionSheets, caseType);

        assertEquals(1, events.size());
        EventEntity event = events.iterator().next();
        assertEquals("ext:allowed", event.getReference());
        verify(registry).addDefinitionDataItemForEntity(event, row);
        verify(parseContext).registerEvent(caseTypeId, event);
    }

    @ParameterizedTest
    @CsvSource({
        "PCS, ext:denied",
        "OTHER, ext:allowed",
        "PCS, ext:Allowed",
        "EMPTY, ext:allowed",
        "PCS, ext:"
    })
    void shouldRejectUnlistedEventOrUnconfiguredCaseType(String caseTypeId, String eventId) {
        whitelists.put("pcs", List.of("ext:allowed"));
        whitelists.put("empty", List.of());
        addEvent(caseTypeId, eventId);

        SpreadsheetParsingException exception = assertThrows(SpreadsheetParsingException.class,
            () -> parser.parseAll(definitionSheets, caseType));

        assertEquals(String.format("Decentralised event '%s' is not whitelisted for case type '%s'",
            eventId, caseTypeId), exception.getMessage());
        verify(registry, never()).addDefinitionDataItemForEntity(any(), any());
        verify(parseContext, never()).registerEvent(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"create", "EXT:allowed", "prefix-ext:allowed", "external"})
    void shouldKeepExistingBehaviourForNonDecentralisedEvents(String eventId) {
        addEvent("PCS", eventId);

        Collection<EventEntity> events = parser.parseAll(definitionSheets, caseType);

        assertEquals(1, events.size());
        assertEquals(eventId, events.iterator().next().getReference());
    }

    private DefinitionDataItem addEvent(String caseTypeId, String eventId) {
        caseType.setReference(caseTypeId);
        DefinitionDataItem row = new DefinitionDataItem(SheetName.CASE_EVENT.getName());
        row.addAttribute(ColumnName.CASE_TYPE_ID, caseTypeId);
        row.addAttribute(ColumnName.ID, eventId);
        row.addAttribute(ColumnName.NAME, "Event name");
        definitionSheet.addDataItem(row);
        return row;
    }
}
