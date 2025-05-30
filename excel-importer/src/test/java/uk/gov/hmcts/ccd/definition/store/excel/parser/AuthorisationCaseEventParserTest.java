package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseTypeParserTest.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_TYPE_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_EVENT_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

@DisplayName("Authorisation Case Event Parser Test")
class AuthorisationCaseEventParserTest {

    private static final String CASE_TYPE_INVALID = "Invalid Case Type";

    private static final String CASE_CRUD_1 = " CCCd  ";
    private static final String CASE_CRUD_2 = " X y  ";

    private AuthorisationCaseEventParser subject;
    private CaseTypeEntity caseType;
    private Map<String, DefinitionSheet> definitionSheets;
    private DefinitionSheet definitionSheet;

    private static final String TEST_ACCESS_PROFILE_FOUND = "CaseWorker 1";
    private static final String TEST_ACCESS_PROFILE_NOT_FOUND = "CaseWorker 2";
    private static final String TEST_CASE_ROLE_FOUND = "[CLAIMANT]";

    @Mock
    private AccessProfileEntity mockAccessProfileEntity;

    private CaseRoleEntity caseRoleEntity;

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    private EventEntity caseEvent;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        final ParseContext context = new ParseContext();

        given(mockAccessProfileEntity.getReference()).willReturn(TEST_ACCESS_PROFILE_FOUND);
        context.registerAccessProfiles(Collections.singletonList(mockAccessProfileEntity));

        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        subject = new AuthorisationCaseEventParser(context, entityToDefinitionDataItemRegistry);
        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);
        caseEvent = new EventEntity();
        caseEvent.setReference(CASE_EVENT_UNDER_TEST);

        definitionSheets = new HashMap<>();
        definitionSheet = new DefinitionSheet();
        definitionSheets.put(AUTHORISATION_CASE_EVENT.getName(), definitionSheet);
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheets.put(CASE_EVENT.getName(), buildSheetForCaseEvent());

        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(TEST_CASE_ROLE_FOUND);
        caseRoleEntity.setCaseType(caseType);
        context.registerCaseRoles(Collections.singletonList(caseRoleEntity));
    }

    @Test
    void shouldParseEntityWithAccessProfileFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), CASE_CRUD_1);
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();
        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is(CASE_CRUD_1.trim())),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getAccessProfile(), is(mockAccessProfileEntity)),
            () -> assertThat(eventACLEntity.getCreate(), is(true)),
            () -> assertThat(eventACLEntity.getUpdate(), is(false)),
            () -> assertThat(eventACLEntity.getRead(), is(false)),
            () -> assertThat(eventACLEntity.getDelete(), is(true)),

            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
    }

    @Test
    void shouldParseEntityWithCaseRoleFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_CASE_ROLE_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), CASE_CRUD_1);
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();
        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is(CASE_CRUD_1.trim())),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getAccessProfile(), is(caseRoleEntity)),
            () -> assertThat(eventACLEntity.getCreate(), is(true)),
            () -> assertThat(eventACLEntity.getUpdate(), is(false)),
            () -> assertThat(eventACLEntity.getRead(), is(false)),
            () -> assertThat(eventACLEntity.getDelete(), is(true)),

            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
    }

    @Test
    void shouldParseEntityWithAccessProfileNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_NOT_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), CASE_CRUD_1);
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();
        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is(CASE_CRUD_1.trim())),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getAccessProfile(), is(nullValue())),

            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
    }

    @Test
    void shouldParseEntityWithInvalidCrud() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), CASE_CRUD_2);
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();

        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is(CASE_CRUD_2.trim())),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getAccessProfile(), is(mockAccessProfileEntity)),
            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
    }

    @Test
    void shouldParseEntityWithInvalidCrudAndAccessProfileNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_NOT_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), CASE_CRUD_2);
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();

        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is(CASE_CRUD_2.trim())),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getAccessProfile(), is(nullValue())),
            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
    }

    @Test
    void shouldParseEntityWithInvalidEvent() {
        EventEntity caseEvent1 = new EventEntity();
        caseEvent1.setReference("Invalid event");

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), CASE_CRUD_1);
        definitionSheet.addDataItem(item1);

        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent1));
        Collection<EventACLEntity> entities = caseEvent1.getEventACLEntities();
        assertThat(entities.size(), is(0));
    }

    @Test
    void shouldParseEntityWithInvalidCaseType() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_INVALID);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), CASE_CRUD_1);
        definitionSheet.addDataItem(item1);

        final MapperException mapperException = assertThrows(MapperException.class,
            () -> subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent)));
        assertEquals(String.format("Unknown Case Type '%s' in worksheet '%s'",
            CASE_TYPE_INVALID, SheetName.AUTHORISATION_CASE_EVENT.getName()), mapperException.getMessage());
    }

    @Test
    void shouldParseEntityWithNoDefinitions() {
        final MapperException mapperException = assertThrows(MapperException.class,
            () -> subject.parseAndSetEventACLEntities(null, caseType, Collections.singleton(caseEvent)));
        assertEquals("A definition must contain a sheet", mapperException.getMessage());
    }

    @Test
    void shouldParseEntityWithEmptyDefinitions() {
        Map<String, DefinitionSheet> definitionSheetsMap = new HashMap<>();

        final MapperException mapperException = assertThrows(MapperException.class,
            () -> subject.parseAndSetEventACLEntities(definitionSheetsMap, caseType, Collections.singleton(caseEvent)));
        assertEquals("A definition must contain a sheet", mapperException.getMessage());
    }

    @Test
    void shouldParseEntityWithNoDefinitionSheet() {
        Map<String, DefinitionSheet> definitionSheetsMap = new HashMap<>();
        definitionSheetsMap.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheetsMap.put(CASE_EVENT.getName(), buildSheetForCaseEvent());

        final MapperException mapperException = assertThrows(MapperException.class,
            () -> subject.parseAndSetEventACLEntities(definitionSheetsMap, caseType, Collections.singleton(caseEvent)));
        assertEquals(String.format("A definition must contain a '%s' sheet",
            SheetName.AUTHORISATION_CASE_EVENT.getName()), mapperException.getMessage());
    }

    @Test
    void shouldParseEntityWithNoDataItems() {
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();

        assertThat(entities.size(), is(0));
    }

    private DefinitionSheet buildSheetForCaseEvent() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(CASE_EVENT.getName());
        final DefinitionDataItem item = new DefinitionDataItem(CASE_EVENT.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.ID, CASE_EVENT_UNDER_TEST);
        item.addAttribute(ColumnName.NAME, CASE_EVENT_UNDER_TEST);
        sheet.addDataItem(item);
        return sheet;
    }
}
