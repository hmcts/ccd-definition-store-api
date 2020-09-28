package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseTypeParserTest.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

@DisplayName("Authorisation Case Event Parser Test")
class AuthorisationCaseEventParserTest {

    private static final String CASE_TYPE_UNDER_TEST = "Some Case Type";
    private static final String CASE_EVENT_UNDER_TEST = "Are we there yet";
    private AuthorisationCaseEventParser subject;
    private CaseTypeEntity caseType;
    private Map<String, DefinitionSheet> definitionSheets;
    private DefinitionSheet definitionSheet;

    @Mock
    private UserRoleEntity mockUserRoleEntity;

    private CaseRoleEntity caseRoleEntity;

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    private EventEntity caseEvent;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ParseContext context = new ParseContext();
        final String role = "CaseWorker 1";
        given(mockUserRoleEntity.getReference()).willReturn(role);
        context.registerUserRoles(Arrays.asList(mockUserRoleEntity));

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

        final String caseRole = "[CLAIMANT]";
        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(caseRole);
        caseRoleEntity.setCaseType(caseType);
        context.registerCaseRoles(Arrays.asList(caseRoleEntity));
    }

    @Test
    void shouldParseEntityWithUserRoleFound() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();
        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is("CCCd")),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getUserRole(), is(mockUserRoleEntity)),
            () -> assertThat(eventACLEntity.getCreate(), is(true)),
            () -> assertThat(eventACLEntity.getUpdate(), is(false)),
            () -> assertThat(eventACLEntity.getRead(), is(false)),
            () -> assertThat(eventACLEntity.getDelete(), is(true)),

            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
    }

    @Test
    public void shouldParseEntityWithCaseRoleFound() {
        final String caseRole = "[CLAIMANT]";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), caseRole);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();
        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is("CCCd")),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getUserRole(), is(caseRoleEntity)),
            () -> assertThat(eventACLEntity.getCreate(), is(true)),
            () -> assertThat(eventACLEntity.getUpdate(), is(false)),
            () -> assertThat(eventACLEntity.getRead(), is(false)),
            () -> assertThat(eventACLEntity.getDelete(), is(true)),

            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
    }

    @Test
    public void shouldParseEntityWithUserRoleNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();
        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is("CCCd")),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getUserRole(), is(nullValue())),

            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
    }

    @Test
    public void shouldParseEntityWithInvalidCrud() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();

        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is("X y")),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getUserRole(), is(mockUserRoleEntity)),
            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
    }

    @Test
    public void shouldParseEntityWithInvalidCrudAndUserNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_EVENT.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        subject.parseAndSetEventACLEntities(definitionSheets, caseType, Collections.singleton(caseEvent));
        Collection<EventACLEntity> entities = caseEvent.getEventACLEntities();

        assertThat(entities.size(), is(1));

        final EventACLEntity eventACLEntity = new ArrayList<>(entities).get(0);
        assertAll(() -> assertThat(eventACLEntity.getCrudAsString(), is("X y")),
            () -> assertThat(eventACLEntity.getId(), is(nullValue())),
            () -> assertThat(eventACLEntity.getUserRole(), is(nullValue())),
            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(eventACLEntity),
                is(Optional.of(item1))));
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
