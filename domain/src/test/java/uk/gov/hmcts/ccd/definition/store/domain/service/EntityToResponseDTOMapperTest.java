package uk.gov.hmcts.ccd.definition.store.domain.service;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventLiteACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCasesResultFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SortOrder;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessControlList;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseEvent;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseEventField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseEventFieldComplex;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseEventLite;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseRole;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseState;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeLite;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeTab;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeTabField;
import uk.gov.hmcts.ccd.definition.store.repository.model.ComplexACL;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfig;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchAliasField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchCasesResultField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchInputField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchResultsField;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketResultField;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkbasketInputField;

class EntityToResponseDTOMapperTest {

    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String LIVE_FROM = "2017-02-02";

    private static final String LIVE_TO = "2018-03-03";

    private final EntityToResponseDTOMapper classUnderTest = new EntityToResponseDTOMapperImpl();

    private EntityToResponseDTOMapper spyOnClassUnderTest;

    @BeforeEach
    void setUpSpy() {
        spyOnClassUnderTest = spy(classUnderTest);
    }

    @Nested
    @DisplayName("Should return a CaseEventField which matches the EventCaseFieldEntity")
    class MapEventCaseFieldEntity {

        @Test
        void testMapEventCaseFieldEntity() {
            EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
            eventCaseFieldEntity.setShowCondition("PersonFirstName=\"John\"");
            eventCaseFieldEntity.setShowSummaryChangeOption(true);
            eventCaseFieldEntity.setShowSummaryContentOption(2);
            eventCaseFieldEntity.setDisplayContext(DisplayContext.MANDATORY);

            CaseEventField caseEventField = spyOnClassUnderTest.map(
                eventCaseFieldEntity
            );

            assertAll(
                () -> assertEquals("displayContext", eventCaseFieldEntity.getDisplayContext().name(),
                    caseEventField.getDisplayContext()),
                () -> assertEquals("showCondition", eventCaseFieldEntity.getShowCondition(),
                    caseEventField.getShowCondition()),
                () -> assertEquals("showSummaryChangeOption", eventCaseFieldEntity.getShowSummaryChangeOption(),
                    caseEventField.getShowSummaryChangeOption()),
                () -> assertEquals("showSummaryContentOption", eventCaseFieldEntity.getShowSummaryContentOption(),
                    caseEventField.getShowSummaryContentOption())
            );
        }

        @Test
        void testMapEventCaseFieldEntityWithComplexFields() {
            EventComplexTypeEntity eventComplexTypeEntity1 = new EventComplexTypeEntity();
            String ref1 = "Some ref";
            eventComplexTypeEntity1.setReference(ref1);
            eventComplexTypeEntity1.setShowCondition("PersonFirstName=\"Anna\"");
            eventComplexTypeEntity1.setOrder(1);
            eventComplexTypeEntity1.setDisplayContext(DisplayContext.MANDATORY);
            eventComplexTypeEntity1.setHint("Hint text");
            eventComplexTypeEntity1.setLabel("Label text");
            eventComplexTypeEntity1.setDefaultValue("DefaultValue1");

            EventComplexTypeEntity eventComplexTypeEntity2 = new EventComplexTypeEntity();
            String ref2 = "Some ref2";
            eventComplexTypeEntity2.setReference(ref2);
            eventComplexTypeEntity2.setShowCondition("PersonFirstName=\"Anna2\"");
            eventComplexTypeEntity2.setOrder(2);
            eventComplexTypeEntity2.setDisplayContext(DisplayContext.OPTIONAL);
            eventComplexTypeEntity2.setHint("Hint text2");
            eventComplexTypeEntity2.setLabel("Label text2");
            eventComplexTypeEntity2.setDefaultValue("Ref2DefaultValue");

            EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
            eventCaseFieldEntity.setShowSummaryChangeOption(true);
            eventCaseFieldEntity.setShowSummaryContentOption(2);
            eventCaseFieldEntity.setDisplayContext(DisplayContext.COMPLEX);
            eventCaseFieldEntity.addComplexFields(asList(eventComplexTypeEntity1, eventComplexTypeEntity2));

            CaseEventField caseEventField = spyOnClassUnderTest.map(eventCaseFieldEntity);

            assertAll(
                () -> assertThat(caseEventField.getCaseEventFieldComplex().size(), is(2)),
                () -> assertEquals("showCondition",
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getShowCondition(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getShowCondition()),
                () -> assertEquals("hint",
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getHint(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getHint()),
                () -> assertEquals("label",
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getLabel(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getLabel()),
                () -> assertEquals("displayContext",
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getDisplayContext(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getDisplayContext()),

                () -> assertEquals("DefaultValue1",
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getDefaultValue(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getDefaultValue()),

                () -> assertEquals("order",
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getOrder(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getOrder())
            );
        }
    }

    private EventComplexTypeEntity findEventComplexTypeEntity(
        List<EventComplexTypeEntity> eventComplexTypeEntities, String reference) {
        return eventComplexTypeEntities.stream()
            .filter(e -> e.getReference().equals(reference)).findFirst().get();
    }

    private CaseEventFieldComplex findCaseEventFieldComplex(
        List<CaseEventFieldComplex> caseEventFieldComplexes, String reference) {
        return caseEventFieldComplexes.stream()
            .filter(e -> e.getReference().equals(reference)).findFirst().get();
    }

    @Nested
    @DisplayName("Should return a CaseEvent which matches the EventEntity")
    class MapEventEntity {

        @Test
        void shouldMapToCaseEvent() {
            EventEntity eventEntity = new EventEntity();
            eventEntity.setShowEventNotes(true);
            eventEntity.setCanSaveDraft(true);

            CaseEvent caseEvent = spyOnClassUnderTest.map(
                eventEntity
            );

            assertAll(
                () -> assertEquals(eventEntity.getShowEventNotes(), caseEvent.getShowEventNotes()),
                () -> assertEquals(eventEntity.getCanSaveDraft(), caseEvent.getCanSaveDraft())
            );
        }
    }

    @Nested
    @DisplayName("Should return a CaseType which matches the CaseTypeEntity")
    class MapCaseTypeEntityTests {

        @Test
        void testMapCaseTypeEntity() throws Exception {

            // Set up
            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
            Jurisdiction jurisdiction = new Jurisdiction();
            when(spyOnClassUnderTest.map(jurisdictionEntity)).thenReturn(jurisdiction);

            EventEntity eventEntity1 = new EventEntity();
            EventEntity eventEntity2 = new EventEntity();
            EventEntity eventEntity3 = new EventEntity();
            CaseEvent caseEvent1 = new CaseEvent();
            CaseEvent caseEvent2 = new CaseEvent();
            CaseEvent caseEvent3 = new CaseEvent();
            when(spyOnClassUnderTest.map(eventEntity1)).thenReturn(caseEvent1);
            when(spyOnClassUnderTest.map(eventEntity2)).thenReturn(caseEvent2);
            when(spyOnClassUnderTest.map(eventEntity3)).thenReturn(caseEvent3);

            StateEntity stateEntity1 = new StateEntity();
            StateEntity stateEntity2 = new StateEntity();
            StateEntity stateEntity3 = new StateEntity();
            CaseState caseState1 = new CaseState();
            CaseState caseState2 = new CaseState();
            CaseState caseState3 = new CaseState();
            when(spyOnClassUnderTest.map(stateEntity1)).thenReturn(caseState1);
            when(spyOnClassUnderTest.map(stateEntity2)).thenReturn(caseState2);
            when(spyOnClassUnderTest.map(stateEntity3)).thenReturn(caseState3);

            CaseFieldEntity caseFieldEntity1 = new CaseFieldEntity();
            CaseFieldEntity caseFieldEntity2 = new CaseFieldEntity();
            CaseFieldEntity caseFieldEntity3 = new CaseFieldEntity();
            CaseField caseField1 = new CaseField();
            CaseField caseField2 = new CaseField();
            CaseField caseField3 = new CaseField();
            when(spyOnClassUnderTest.map(caseFieldEntity1)).thenReturn(caseField1);
            when(spyOnClassUnderTest.map(caseFieldEntity2)).thenReturn(caseField2);
            when(spyOnClassUnderTest.map(caseFieldEntity3)).thenReturn(caseField3);

            SearchAliasFieldEntity searchAliasFieldEntity1 = new SearchAliasFieldEntity();
            SearchAliasFieldEntity searchAliasFieldEntity2 = new SearchAliasFieldEntity();
            SearchAliasField searchAliasField1 = new SearchAliasField();
            SearchAliasField searchAliasField2 = new SearchAliasField();
            when(spyOnClassUnderTest.map(searchAliasFieldEntity1)).thenReturn(searchAliasField1);
            when(spyOnClassUnderTest.map(searchAliasFieldEntity2)).thenReturn(searchAliasField2);

            CaseTypeACLEntity roleWithCreateOnly = caseTypeUserRoleEntity("role-with-create-only", true, false,
                false, false);
            CaseTypeACLEntity roleWithReadOnly = caseTypeUserRoleEntity("role-with-read-only", false, true, false,
                false);
            CaseTypeACLEntity roleWithUpdateOnly = caseTypeUserRoleEntity("role-with-update-only", false, false,
                true, false);
            CaseTypeACLEntity roleWithDeleteOnly = caseTypeUserRoleEntity("role-with-delete-only", false, false,
                false, true);

            CaseTypeEntity caseTypeEntity = caseTypeEntity(
                jurisdictionEntity,
                asList(eventEntity1, eventEntity2, eventEntity3),
                asList(stateEntity1, stateEntity2, stateEntity3),
                asList(roleWithCreateOnly, roleWithReadOnly, roleWithUpdateOnly, roleWithDeleteOnly),
                asList(caseFieldEntity1, caseFieldEntity2, caseFieldEntity3),
                asList(searchAliasFieldEntity1, searchAliasFieldEntity2)
            );

            // Call the 'spied on' implementation
            CaseType caseType = spyOnClassUnderTest.map(
                caseTypeEntity
            );

            // Assertions
            assertEquals(caseType.getVersion().getNumber(), caseTypeEntity.getVersion());
            assertEquals(caseType.getVersion().getLiveFrom(), YEAR_FORMAT.parse(LIVE_FROM));
            assertEquals(caseType.getVersion().getLiveUntil(), YEAR_FORMAT.parse(LIVE_TO));
            assertEquals(caseType.getId(), caseTypeEntity.getReference());
            assertEquals(caseType.getName(), caseTypeEntity.getName());
            assertEquals(caseType.getSecurityClassification(), caseTypeEntity.getSecurityClassification());
            assertEquals(caseType.getPrintableDocumentsUrl(), caseTypeEntity.getPrintWebhook().getUrl());

            assertThat(caseType.getJurisdiction(), is(sameInstance(jurisdiction)));

            assertEquals(3, caseType.getEvents().size());
            assertThat(caseType.getEvents(), hasItems(caseEvent1, caseEvent2, caseEvent3));

            assertEquals(3, caseType.getStates().size());
            assertThat(caseType.getStates(), hasItems(caseState1, caseState2, caseState3));

            assertEquals(4, caseType.getAcls().size());
            assertAcls(caseTypeEntity.getCaseTypeACLEntities(), caseType.getAcls());

            assertEquals(3, caseType.getCaseFields().size());
            assertThat(caseType.getCaseFields(), hasItems(caseField1, caseField2, caseField3));

            assertEquals(2, caseType.getSearchAliasFields().size());
            assertThat(caseType.getSearchAliasFields(), hasItems(searchAliasField1, searchAliasField2));
        }

        @Test
        void testMapEmptyCaseTypeEntity() {

            CaseType caseType = classUnderTest.map(
                new CaseTypeEntity()
            );

            // Assertions
            assertNull(caseType.getVersion().getNumber());
            assertNull(caseType.getVersion().getLiveFrom());
            assertNull(caseType.getVersion().getLiveUntil());
            assertNull(caseType.getId());
            assertNull(caseType.getName());
            assertNull(caseType.getSecurityClassification());
            assertNull(caseType.getPrintableDocumentsUrl());
            assertNull(caseType.getJurisdiction());

            assertEquals(0, caseType.getEvents().size());
            assertEquals(0, caseType.getStates().size());
            assertEquals(0, caseType.getAcls().size());
            assertEquals(0, caseType.getCaseFields().size());
        }

        private CaseTypeEntity caseTypeEntity(JurisdictionEntity jurisdiction,
                                              List<EventEntity> events,
                                              Collection<StateEntity> states,
                                              List<CaseTypeACLEntity> roles,
                                              List<CaseFieldEntity> caseFieldEntities,
                                              List<SearchAliasFieldEntity> searchAliasFieldEntities) {
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setVersion(69);
            caseTypeEntity.setLiveFrom(LocalDate.parse(LIVE_FROM));
            caseTypeEntity.setLiveTo(LocalDate.parse(LIVE_TO));
            caseTypeEntity.setReference("Reference");
            caseTypeEntity.setName("Name");
            caseTypeEntity.setSecurityClassification(SecurityClassification.RESTRICTED);

            WebhookEntity webhookEntity = new WebhookEntity();
            webhookEntity.setUrl("Document Print URL");
            caseTypeEntity.setPrintWebhook(webhookEntity);

            caseTypeEntity.setJurisdiction(jurisdiction);
            caseTypeEntity.addEvents(events);
            caseTypeEntity.addStates(states);
            caseTypeEntity.addCaseTypeACLEntities(roles);
            caseTypeEntity.addCaseFields(caseFieldEntities);
            caseTypeEntity.addSearchAliasFields(searchAliasFieldEntities);

            return caseTypeEntity;
        }

        private CaseTypeACLEntity caseTypeUserRoleEntity(String reference,
                                                         Boolean create,
                                                         Boolean read,
                                                         Boolean update,
                                                         Boolean delete) {
            CaseTypeACLEntity caseTypeACLEntity = new CaseTypeACLEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference(reference);
            caseTypeACLEntity.setUserRole(userRoleEntity);
            caseTypeACLEntity.setCreate(create);
            caseTypeACLEntity.setRead(read);
            caseTypeACLEntity.setUpdate(update);
            caseTypeACLEntity.setDelete(delete);
            return caseTypeACLEntity;
        }
    }

    @Nested
    @DisplayName("Should return a Jurisdiction which matches the JurisdictionEntity")
    class MapJurisdictionEntityTests {

        @Test
        void testMapJurisdictionEntity() {

            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
            jurisdictionEntity.setDescription("Jurisdiction Description");
            jurisdictionEntity.setName("Jurisdiction Name");
            jurisdictionEntity.setReference("Jurisdiction Reference");
            jurisdictionEntity.setLiveFrom(new Date());
            jurisdictionEntity.setLiveTo(new Date());

            Jurisdiction jurisdiction = classUnderTest.map(jurisdictionEntity);

            assertEquals(jurisdictionEntity.getDescription(), jurisdiction.getDescription());
            assertEquals(jurisdictionEntity.getName(), jurisdiction.getName());
            assertEquals(jurisdictionEntity.getReference(), jurisdiction.getId());
            assertEquals(jurisdictionEntity.getLiveFrom(), jurisdiction.getLiveFrom());
            assertEquals(jurisdictionEntity.getLiveTo(), jurisdiction.getLiveUntil());

        }

        @Test
        void testMapEmptyJurisdictionEntity() {

            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();

            Jurisdiction jurisdiction = classUnderTest.map(jurisdictionEntity);

            assertNull(jurisdiction.getDescription());
            assertNull(jurisdiction.getName());
            assertNull(jurisdiction.getId());
            assertNull(jurisdiction.getLiveFrom());
            assertNull(jurisdiction.getLiveUntil());

        }
    }


    @Nested
    @DisplayName("Should return a CaseEventLite which matches the CaseEventLiteEntity")
    class MapEventLiteEntityTests {

        @Test
        void testMapEventLiteEntity() {

            StateEntity preState = new StateEntity();
            preState.setReference("some state");

            EventLiteEntity eventLiteEntity = new EventLiteEntity();
            eventLiteEntity.setId(1);
            eventLiteEntity.setCanCreate(false);
            eventLiteEntity.setName("Some name");
            eventLiteEntity.setReference("Some reference");
            eventLiteEntity.setDescription("Some Description");
            eventLiteEntity.getPreStates().add(preState);

            CaseEventLite caseEventLite = classUnderTest.map(eventLiteEntity);

            assertEquals(eventLiteEntity.getDescription(), caseEventLite.getDescription());
            assertEquals(eventLiteEntity.getName(), caseEventLite.getName());
            assertEquals(eventLiteEntity.getPreStates().size(), caseEventLite.getPreStates().size());
            assertEquals(eventLiteEntity.getReference(), caseEventLite.getId());

        }

        @Test
        void testMapEmptyJurisdictionEntity() {

            EventLiteEntity eventLiteEntity = new EventLiteEntity();
            eventLiteEntity.setCanCreate(true);

            CaseEventLite caseEventLite = classUnderTest.map(eventLiteEntity);

            assertNull(caseEventLite.getDescription());
            assertNull(caseEventLite.getName());
            assertEquals(0, caseEventLite.getPreStates().size());
            assertNull(caseEventLite.getId());

        }
    }

    @Nested
    @DisplayName("Should return a CaseTypeLite which matches the CaseTypeLiteEntity")
    class MapCaseTypeLiteEntityTests {

        @Test
        void testMapCaseTypeLiteEntity() {

            // Set up
            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
            Jurisdiction jurisdiction = new Jurisdiction();
            when(spyOnClassUnderTest.map(jurisdictionEntity)).thenReturn(jurisdiction);

            EventLiteEntity eventEntity1 = new EventLiteEntity();
            EventLiteEntity eventEntity2 = new EventLiteEntity();
            EventLiteEntity eventEntity3 = new EventLiteEntity();
            CaseEventLite caseEvent1 = new CaseEventLite();
            CaseEventLite caseEvent2 = new CaseEventLite();
            CaseEventLite caseEvent3 = new CaseEventLite();
            when(spyOnClassUnderTest.map(eventEntity1)).thenReturn(caseEvent1);
            when(spyOnClassUnderTest.map(eventEntity2)).thenReturn(caseEvent2);
            when(spyOnClassUnderTest.map(eventEntity3)).thenReturn(caseEvent3);

            StateEntity stateEntity1 = new StateEntity();
            StateEntity stateEntity2 = new StateEntity();
            StateEntity stateEntity3 = new StateEntity();
            CaseState caseState1 = new CaseState();
            CaseState caseState2 = new CaseState();
            CaseState caseState3 = new CaseState();
            when(spyOnClassUnderTest.map(stateEntity1)).thenReturn(caseState1);
            when(spyOnClassUnderTest.map(stateEntity2)).thenReturn(caseState2);
            when(spyOnClassUnderTest.map(stateEntity3)).thenReturn(caseState3);

            CaseTypeLiteACLEntity caseTypeLiteACLEntity = new CaseTypeLiteACLEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            caseTypeLiteACLEntity.setUserRole(userRoleEntity);

            EventLiteACLEntity eventLiteACLEntity = new EventLiteACLEntity();
            eventLiteACLEntity.setUserRole(userRoleEntity);
            EventLiteACLEntity eventLiteACLEntity2 = new EventLiteACLEntity();
            eventLiteACLEntity2.setUserRole(userRoleEntity);
            eventEntity1.addEventACL(eventLiteACLEntity);
            eventEntity2.addEventACL(eventLiteACLEntity);
            eventEntity2.addEventACL(eventLiteACLEntity2);
            eventEntity3.addEventACL(eventLiteACLEntity);

            StateACLEntity stateUserRoleEntity1 = new StateACLEntity();
            StateACLEntity stateUserRoleEntity2 = new StateACLEntity();
            stateUserRoleEntity1.setUserRole(userRoleEntity);
            stateUserRoleEntity2.setUserRole(userRoleEntity);
            stateEntity1.addStateACLEntities(asList(stateUserRoleEntity1, stateUserRoleEntity2));
            stateEntity2.addStateACL(stateUserRoleEntity1);
            stateEntity3.addStateACLEntities(asList(stateUserRoleEntity1, stateUserRoleEntity2));


            CaseTypeLiteEntity caseTypeLiteEntity = new CaseTypeLiteEntity();
            caseTypeLiteEntity.setName("some state name");
            caseTypeLiteEntity.setReference("some state ref");
            caseTypeLiteEntity.setDescription("some state description");
            caseTypeLiteEntity.addEvent(eventEntity1).addEvent(eventEntity2).addEvent(eventEntity3);
            caseTypeLiteEntity.addState(stateEntity1).addState(stateEntity2).addState(stateEntity3);
            caseTypeLiteEntity.setJurisdiction(jurisdictionEntity);
            caseTypeLiteEntity.addCaseTypeACL(caseTypeLiteACLEntity);

            CaseTypeLite caseTypeLite = classUnderTest.map(caseTypeLiteEntity);

            assertEquals(caseTypeLiteEntity.getDescription(), caseTypeLite.getDescription());
            assertEquals(caseTypeLiteEntity.getName(), caseTypeLite.getName());
            assertEquals(caseTypeLiteEntity.getReference(), caseTypeLite.getId());

            assertEquals(3, caseTypeLite.getEvents().size());
            assertEquals(1, caseTypeLite.getEvents().get(0).getAcls().size());
            assertEquals(2, caseTypeLite.getEvents().get(1).getAcls().size());
            assertEquals(1, caseTypeLite.getEvents().get(2).getAcls().size());

            assertEquals(3, caseTypeLite.getStates().size());
            assertEquals(2, caseTypeLite.getStates().get(0).getAcls().size());
            assertEquals(1, caseTypeLite.getStates().get(1).getAcls().size());
            assertEquals(2, caseTypeLite.getStates().get(2).getAcls().size());

            assertEquals(3, caseTypeLite.getStates().size());
            assertEquals(1, caseTypeLite.getAcls().size());
        }

        @Test
        void testMapEmptyCaseTypeLiteEntity() {

            CaseTypeLiteEntity caseTypeLiteEntity = new CaseTypeLiteEntity();

            CaseTypeLite caseTypeLite = classUnderTest.map(caseTypeLiteEntity);

            assertNull(caseTypeLite.getDescription());
            assertNull(caseTypeLite.getName());
            assertNull(caseTypeLite.getId());

        }
    }

    @Nested
    @DisplayName("Should create a CaseEvent matching EventEntity fields, with the following exceptions/amendments:"
        + "- preStates should be empty if canCreate is true "
        + "- preStates should default to a single 'wildcard' entry if not defined in entity"
        + "- postState should default to 'wildcard' if not defined in entity")
    class MapEventEntityTests {

        @Test
        void testMapEventEntity() {
            for (Parameters parameters : createParameters()) {
                CaseEvent caseEvent = mapEventWithStatesAssertCommonFieldsAndReturn(
                    parameters
                );
                assertEquals(parameters.getPreStateExpectation(), caseEvent.getPreStates());
                assertEquals(parameters.getPostStateExpectation(), caseEvent.getPostState());
            }
        }

        @Test
        void testMapEmptyEventEntity() {
            CaseEvent caseEvent = classUnderTest.map(new EventEntity());

            assertNull(caseEvent.getId());
            assertNull(caseEvent.getName());
            assertNull(caseEvent.getDescription());
            assertNull(caseEvent.getOrder());

            assertTrue(caseEvent.getCaseFields().isEmpty());

            assertNull(caseEvent.getCallBackURLAboutToStartEvent());
            assertTrue(caseEvent.getRetriesTimeoutAboutToStartEvent().isEmpty());

            assertNull(caseEvent.getCallBackURLAboutToSubmitEvent());
            assertTrue(caseEvent.getRetriesTimeoutURLAboutToSubmitEvent().isEmpty());

            assertNull(caseEvent.getCallBackURLSubmittedEvent());
            assertTrue(caseEvent.getRetriesTimeoutURLSubmittedEvent().isEmpty());

            assertNull(caseEvent.getSecurityClassification());
            assertTrue(caseEvent.getAcls().isEmpty());
            assertNull(caseEvent.getShowSummary());
            assertNull(caseEvent.getEndButtonLabel());
            assertNull(caseEvent.getCanSaveDraft());

            assertEquals(1, caseEvent.getPreStates().size());
            assertThat(caseEvent.getPreStates(), hasItems("*"));
            assertEquals("*", caseEvent.getPostState());

        }

        private List<Parameters> createParameters() {
            return asList(
                new Parameters(
                    false, Collections.emptyList(), null,
                    Collections.singletonList("*"), "*"
                ),
                new Parameters(
                    false, Collections.emptyList(), stateEntity("PostState"),
                    Collections.singletonList("*"), "PostState"
                ),
                new Parameters(
                    false, asList(stateEntity("preState1"), stateEntity("preState2"), stateEntity("preState3")),
                    null,
                    asList("preState1", "preState2", "preState3"), "*"
                ),
                new Parameters(
                    false, asList(stateEntity("preState1"), stateEntity("preState2"), stateEntity("preState3")),
                    stateEntity("PostState"),
                    asList("preState1", "preState2", "preState3"), "PostState"
                ),
                new Parameters(
                    true, Collections.emptyList(), null,
                    Collections.emptyList(), "*"
                ),
                new Parameters(
                    true, Collections.emptyList(), stateEntity("PostState"),
                    Collections.emptyList(), "PostState"
                ),
                new Parameters(
                    true, asList(stateEntity("preState1"), stateEntity("preState2"), stateEntity("preState3")),
                    null,
                    Collections.emptyList(), "*"
                ),
                new Parameters(
                    true, asList(stateEntity("preState1"), stateEntity("preState2"), stateEntity("preState3")),
                    stateEntity("PostState"),
                    Collections.emptyList(), "PostState"
                )
            );
        }

        private StateEntity stateEntity(String reference) {
            StateEntity stateEntity = new StateEntity();
            stateEntity.setReference(reference);
            return stateEntity;
        }

        private class Parameters {

            private final Boolean canCreate;
            private final List<StateEntity> preStates;
            private final StateEntity postState;
            private final List<String> preStateExpectation;
            private final String postStateExpectation;

            Parameters(Boolean canCreate,
                       List<StateEntity> preStates,
                       StateEntity postState,
                       List<String> preStateExpectation,
                       String postStateExpectation) {
                this.canCreate = canCreate;
                this.preStates = preStates;
                this.postState = postState;
                this.preStateExpectation = preStateExpectation;
                this.postStateExpectation = postStateExpectation;
            }

            private Boolean getCanCreate() {
                return canCreate;
            }

            private List<StateEntity> getPreStates() {
                return preStates;
            }

            private StateEntity getPostState() {
                return postState;
            }

            private List<String> getPreStateExpectation() {
                return preStateExpectation;
            }

            private String getPostStateExpectation() {
                return postStateExpectation;
            }
        }

        private CaseEvent mapEventWithStatesAssertCommonFieldsAndReturn(Parameters parameters) {

            EventEntity eventEntity = new EventEntity();
            eventEntity.setReference("Event Reference");
            eventEntity.setName("Event Name");
            eventEntity.setDescription("Event Description");
            eventEntity.setOrder(69);
            eventEntity.setSecurityClassification(SecurityClassification.RESTRICTED);
            eventEntity.setShowSummary(Boolean.TRUE);
            eventEntity.setEndButtonLabel("Create Draft");
            eventEntity.setCanSaveDraft(true);

            Integer startTimeout1 = 691;
            Integer startTimeout2 = 692;
            Integer startTimeout3 = 693;
            eventEntity.setWebhookStart(webHook("Start", startTimeout1, startTimeout2, startTimeout3));
            Integer preSubmitTimeout1 = 694;
            Integer preSubmitTimeout2 = 695;
            Integer preSubmitTimeout3 = 696;
            eventEntity.setWebhookPreSubmit(
                webHook("PreSubmit", preSubmitTimeout1, preSubmitTimeout2, preSubmitTimeout3));
            Integer postSubmitTimeout1 = 697;
            Integer postSubmitTimeout2 = 698;
            Integer postSubmitTimeout3 = 699;
            eventEntity.setWebhookPostSubmit(
                webHook("PostSubmit", postSubmitTimeout1, postSubmitTimeout2, postSubmitTimeout3));

            EventCaseFieldEntity eventCaseFieldEntity1 = new EventCaseFieldEntity();
            EventCaseFieldEntity eventCaseFieldEntity2 = new EventCaseFieldEntity();
            EventCaseFieldEntity eventCaseFieldEntity3 = new EventCaseFieldEntity();
            CaseEventField caseEventField1 = new CaseEventField();
            CaseEventField caseEventField2 = new CaseEventField();
            CaseEventField caseEventField3 = new CaseEventField();
            when(spyOnClassUnderTest.map(eventCaseFieldEntity1)).thenReturn(caseEventField1);
            when(spyOnClassUnderTest.map(eventCaseFieldEntity2)).thenReturn(caseEventField2);
            when(spyOnClassUnderTest.map(eventCaseFieldEntity3)).thenReturn(caseEventField3);
            eventEntity.addEventCaseFields(
                asList(eventCaseFieldEntity1, eventCaseFieldEntity2, eventCaseFieldEntity3));

            EventACLEntity roleWithCreateOnly = eventUserRoleEntity("role-with-create-only", true, false, false,
                false);
            EventACLEntity roleWithReadOnly = eventUserRoleEntity("role-with-read-only", false, true, false,
                false);
            EventACLEntity roleWithUpdateOnly = eventUserRoleEntity("role-with-update-only", false, false, true,
                false);
            EventACLEntity roleWithDeleteOnly = eventUserRoleEntity("role-with-delete-only", false, false, false,
                true);
            eventEntity.addEventACLEntities(
                asList(roleWithCreateOnly, roleWithReadOnly, roleWithUpdateOnly, roleWithDeleteOnly));

            eventEntity.setCanCreate(parameters.getCanCreate());
            parameters.getPreStates().forEach(eventEntity::addPreState);
            eventEntity.setPostState(parameters.getPostState());

            CaseEvent caseEvent = spyOnClassUnderTest.map(eventEntity);

            assertEquals(eventEntity.getReference(), caseEvent.getId());
            assertEquals(eventEntity.getName(), caseEvent.getName());
            assertEquals(eventEntity.getDescription(), caseEvent.getDescription());
            assertEquals(eventEntity.getOrder(), caseEvent.getOrder());

            assertEquals(3, caseEvent.getCaseFields().size());
            assertThat(caseEvent.getCaseFields(), hasItems(caseEventField1, caseEventField2, caseEventField3));

            assertEquals(eventEntity.getWebhookStart().getUrl(), caseEvent.getCallBackURLAboutToStartEvent());
            assertEquals(eventEntity.getWebhookStart().getTimeouts().size(),
                caseEvent.getRetriesTimeoutAboutToStartEvent().size());
            assertThat(caseEvent.getRetriesTimeoutAboutToStartEvent(),
                hasItems(startTimeout1, startTimeout2, startTimeout3));

            assertEquals(eventEntity.getWebhookPreSubmit().getUrl(), caseEvent.getCallBackURLAboutToSubmitEvent());
            assertEquals(eventEntity.getWebhookPreSubmit().getTimeouts().size(),
                caseEvent.getRetriesTimeoutURLAboutToSubmitEvent().size());
            assertThat(caseEvent.getRetriesTimeoutURLAboutToSubmitEvent(),
                hasItems(preSubmitTimeout1, preSubmitTimeout2, preSubmitTimeout3));

            assertEquals(eventEntity.getWebhookPostSubmit().getUrl(), caseEvent.getCallBackURLSubmittedEvent());
            assertEquals(eventEntity.getWebhookPostSubmit().getTimeouts().size(),
                caseEvent.getRetriesTimeoutURLSubmittedEvent().size());
            assertThat(caseEvent.getRetriesTimeoutURLSubmittedEvent(),
                hasItems(postSubmitTimeout1, postSubmitTimeout2, postSubmitTimeout3));

            assertEquals(eventEntity.getSecurityClassification(), caseEvent.getSecurityClassification());
            assertAcls(eventEntity.getEventACLEntities(), caseEvent.getAcls());
            assertEquals(eventEntity.getShowSummary(), caseEvent.getShowSummary());
            assertEquals(eventEntity.getEndButtonLabel(), caseEvent.getEndButtonLabel());
            assertEquals(eventEntity.getCanSaveDraft(), caseEvent.getCanSaveDraft());

            return caseEvent;

        }

        private WebhookEntity webHook(String url, Integer... retriesTimeouts) {
            WebhookEntity webhookEntity = new WebhookEntity();
            webhookEntity.setUrl(url);
            webhookEntity.setTimeouts(Lists.newArrayList(retriesTimeouts));
            return webhookEntity;
        }

        private EventACLEntity eventUserRoleEntity(String reference,
                                                   Boolean create,
                                                   Boolean read,
                                                   Boolean update,
                                                   Boolean delete) {
            EventACLEntity eventACLEntity = new EventACLEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference(reference);
            eventACLEntity.setUserRole(userRoleEntity);
            eventACLEntity.setCreate(create);
            eventACLEntity.setRead(read);
            eventACLEntity.setUpdate(update);
            eventACLEntity.setDelete(delete);
            return eventACLEntity;
        }
    }

    @Nested
    @DisplayName("Should create a CaseState matching StateEntity fields")
    class MapStateEntityTests {

        @Test
        void testMapStateEntity() {
            StateEntity stateEntity = new StateEntity();
            stateEntity.setReference("reference");
            stateEntity.setName("name");
            stateEntity.setDescription("description");
            stateEntity.setOrder(69);
            StateACLEntity roleWithCreateOnly = stateUserRoleEntity("role-with-create-only", true, false, false,
                false);
            StateACLEntity roleWithReadOnly = stateUserRoleEntity("role-with-read-only", false, true, false,
                false);
            StateACLEntity roleWithUpdateOnly = stateUserRoleEntity("role-with-update-only", false, false, true,
                false);
            StateACLEntity roleWithDeleteOnly = stateUserRoleEntity("role-with-delete-only", false, false, false,
                true);
            stateEntity.addStateACLEntities(
                asList(roleWithCreateOnly, roleWithReadOnly, roleWithUpdateOnly, roleWithDeleteOnly));

            CaseState caseState = classUnderTest.map(stateEntity);

            assertEquals(stateEntity.getReference(), caseState.getId());
            assertEquals(stateEntity.getName(), caseState.getName());
            assertEquals(stateEntity.getDescription(), caseState.getDescription());
            assertEquals(stateEntity.getOrder(), caseState.getOrder());
            assertAcls(stateEntity.getStateACLEntities(), caseState.getAcls());
        }

        @Test
        void testMapEmptyStateEntity() {

            CaseState caseState = classUnderTest.map(new StateEntity());

            assertNull(caseState.getId());
            assertNull(caseState.getName());
            assertNull(caseState.getDescription());
            assertNull(caseState.getOrder());
            assertTrue(caseState.getAcls().isEmpty());
        }

        private StateACLEntity stateUserRoleEntity(String reference,
                                                   Boolean create,
                                                   Boolean read,
                                                   Boolean update,
                                                   Boolean delete) {
            StateACLEntity eventUserRoleEntity = new StateACLEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference(reference);
            eventUserRoleEntity.setUserRole(userRoleEntity);
            eventUserRoleEntity.setCreate(create);
            eventUserRoleEntity.setRead(read);
            eventUserRoleEntity.setUpdate(update);
            eventUserRoleEntity.setDelete(delete);
            return eventUserRoleEntity;
        }

    }

    @Nested
    @DisplayName("Should create a CaseField matching CaseFieldEntity fields")
    class MapCaseFieldEntityTests {

        @Test
        void testMapCaseFieldEntity() {

            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setCaseType(caseTypeEntity("caseTypeReference"));
            caseFieldEntity.setLabel("CaseFieldLabel");
            caseFieldEntity.setHint("CaseFieldHint");

            FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
            FieldType fieldType = new FieldType();
            when(spyOnClassUnderTest.map(fieldTypeEntity)).thenReturn(fieldType);
            caseFieldEntity.setFieldType(fieldTypeEntity);

            caseFieldEntity.setHidden(true);
            caseFieldEntity.setSecurityClassification(SecurityClassification.RESTRICTED);
            caseFieldEntity.setLiveFrom(LocalDate.parse(LIVE_FROM));
            caseFieldEntity.setLiveTo(LocalDate.parse(LIVE_TO));
            caseFieldEntity.setDataFieldType(DataFieldType.METADATA);

            CaseFieldACLEntity roleWithCreateOnly = caseFieldUserRoleEntity("role-with-create-only", true, false,
                false, false);
            CaseFieldACLEntity roleWithReadOnly = caseFieldUserRoleEntity("role-with-read-only", false, true,
                false, false);
            CaseFieldACLEntity roleWithUpdateOnly = caseFieldUserRoleEntity("role-with-update-only", false, false,
                true, false);
            CaseFieldACLEntity roleWithDeleteOnly = caseFieldUserRoleEntity("role-with-delete-only", false, false,
                false, true);
            caseFieldEntity.addCaseACLEntities(
                asList(roleWithCreateOnly, roleWithReadOnly, roleWithUpdateOnly, roleWithDeleteOnly));

            ComplexFieldACLEntity complexACLWithCreateOnly = complexFieldUserRoleEntity("list.element.code",
                "role-with-create-only", true, false, false, false);
            ComplexFieldACLEntity complexACLWithReadOnly = complexFieldUserRoleEntity("list.element.code.item1",
                "role-with-create-only", true, false, false, false);
            ComplexFieldACLEntity complexACLWithUpdateOnly = complexFieldUserRoleEntity("list.element.code.item2",
                "role-with-create-only", true, false, false, false);
            ComplexFieldACLEntity complexACLWithDeleteOnly = complexFieldUserRoleEntity("list.element.code.item3",
                "role-with-create-only", true, false, false, false);
            ComplexFieldACLEntity complexACLWithAllCRUD = complexFieldUserRoleEntity("list.another.element.code",
                "role-with-create-only", true, true, true, true);
            caseFieldEntity.addComplexFieldACLEntities(asList(complexACLWithCreateOnly, complexACLWithReadOnly,
                complexACLWithUpdateOnly, complexACLWithDeleteOnly, complexACLWithAllCRUD));

            CaseField caseField = spyOnClassUnderTest.map(caseFieldEntity);

            assertEquals(caseFieldEntity.getReference(), caseField.getId());
            assertEquals(caseFieldEntity.getCaseType().getReference(), caseField.getCaseTypeId());
            assertEquals(caseFieldEntity.getLabel(), caseField.getLabel());
            assertEquals(caseFieldEntity.getHint(), caseField.getHintText());
            assertEquals(fieldType, caseField.getFieldType());
            assertEquals(caseFieldEntity.getHidden(), caseField.getHidden());
            assertEquals(caseFieldEntity.getSecurityClassification().toString(), caseField.getSecurityClassification());
            assertEquals(LIVE_FROM, caseField.getLiveFrom());
            assertEquals(LIVE_TO, caseField.getLiveUntil());
            assertThat(caseField.isMetadata(), is(true));
            assertAcls(caseFieldEntity.getCaseFieldACLEntities(), caseField.getAcls());
            assertComplexACLs(caseFieldEntity.getComplexFieldACLEntities(), caseField.getComplexACLs());
        }

        @Test
        void testMapEmptyCaseFieldEntity() {

            CaseField caseField = classUnderTest.map(new CaseFieldEntity());

            assertNull(caseField.getId());
            assertNull(caseField.getCaseTypeId());
            assertNull(caseField.getLabel());
            assertNull(caseField.getHintText());
            assertNull(caseField.getFieldType());
            assertNull(caseField.getHidden());
            assertNull(caseField.getSecurityClassification());
            assertNull(caseField.getLiveFrom());
            assertNull(caseField.getLiveUntil());
        }

        private CaseFieldACLEntity caseFieldUserRoleEntity(String reference,
                                                           Boolean create,
                                                           Boolean read,
                                                           Boolean update,
                                                           Boolean delete) {
            CaseFieldACLEntity caseFieldACLEntity = new CaseFieldACLEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference(reference);
            caseFieldACLEntity.setUserRole(userRoleEntity);
            caseFieldACLEntity.setCreate(create);
            caseFieldACLEntity.setRead(read);
            caseFieldACLEntity.setUpdate(update);
            caseFieldACLEntity.setDelete(delete);
            return caseFieldACLEntity;
        }

        private ComplexFieldACLEntity complexFieldUserRoleEntity(String code, String reference,
                                                                 Boolean create,
                                                                 Boolean read,
                                                                 Boolean update,
                                                                 Boolean delete) {
            ComplexFieldACLEntity complexFieldACLEntity = new ComplexFieldACLEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference(reference);
            complexFieldACLEntity.setUserRole(userRoleEntity);
            complexFieldACLEntity.setCreate(create);
            complexFieldACLEntity.setRead(read);
            complexFieldACLEntity.setUpdate(update);
            complexFieldACLEntity.setDelete(delete);
            complexFieldACLEntity.setListElementCode(code);
            return complexFieldACLEntity;
        }

        private CaseTypeEntity caseTypeEntity(String reference) {
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference(reference);
            return caseTypeEntity;
        }

    }

    @Nested
    @DisplayName("Should create a FieldType matching FieldTypeEntity fields")
    class MapFieldTypeEntityTests {

        @Test
        void testMapFieldTypeEntityWithBaseField() {
            FieldTypeEntity fieldTypeEntity = fieldTypeEntity("fieldTypeEntityReference");
            fieldTypeEntity.setBaseFieldType(fieldTypeEntity("baseFieldTypeEntityReference"));
            fieldTypeEntity.getBaseFieldType().setReference("FixedList");
            FieldType fieldType = mapAssertCommonFieldsAndReturn(fieldTypeEntity);
            assertEquals(fieldTypeEntity.getBaseFieldType().getReference(), fieldType.getType());
        }

        @Test
        void testMapFieldTypeEntityWithoutBaseField() {
            FieldTypeEntity fieldTypeEntity = fieldTypeEntity("fieldTypeEntityReference");
            fieldTypeEntity.setReference("FixedList");
            FieldType fieldType = mapAssertCommonFieldsAndReturn(fieldTypeEntity);
            assertEquals(fieldTypeEntity.getReference(), fieldType.getType());
        }

        @Test
        void testMapFieldTypeEntityWithComplexBaseField() {
            FieldTypeEntity fieldTypeEntity = fieldTypeEntity("fieldTypeEntityReference");
            fieldTypeEntity.setBaseFieldType(fieldTypeEntity("baseFieldTypeEntityReference"));
            fieldTypeEntity.getBaseFieldType().setReference("Complex");
            FieldType complexFieldType = mapAssertComplexCommonFieldsAndReturn(fieldTypeEntity);
            assertEquals(fieldTypeEntity.getBaseFieldType().getReference(), complexFieldType.getType());
        }

        @Test
        void testMapFieldTypeEntityWithoutComplexBaseField() {
            FieldTypeEntity fieldTypeEntity = fieldTypeEntity("fieldTypeEntityReference");
            fieldTypeEntity.setReference("Complex");
            FieldType complexFieldType = mapAssertComplexCommonFieldsAndReturn(fieldTypeEntity);
            assertEquals(fieldTypeEntity.getReference(), complexFieldType.getType());
        }

        @Test
        void testEmptyMapFieldTypeEntity() {
            FieldType fieldType = classUnderTest.map(new FieldTypeEntity());

            assertNull(fieldType.getId());
            assertNull(fieldType.getType());
            assertNull(fieldType.getMin());
            assertNull(fieldType.getMax());
            assertNull(fieldType.getRegularExpression());
            assertEquals(0, fieldType.getComplexFields().size());
            assertEquals(0, fieldType.getFixedListItems().size());
            assertNull(fieldType.getCollectionFieldType());

        }

        private FieldTypeEntity fieldTypeEntity(String reference) {
            FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
            fieldTypeEntity.setReference(reference);
            return fieldTypeEntity;
        }

        private FieldType mapAssertCommonFieldsAndReturn(FieldTypeEntity fieldTypeEntity) {

            fieldTypeEntity.setMinimum("Min");
            fieldTypeEntity.setMaximum("Max");
            fieldTypeEntity.setRegularExpression("SomeRegex");

            FieldTypeListItemEntity fieldTypeListItemEntity1 = new FieldTypeListItemEntity();
            fieldTypeListItemEntity1.setLabel("label1");
            fieldTypeListItemEntity1.setOrder(3);
            FieldTypeListItemEntity fieldTypeListItemEntity2 = new FieldTypeListItemEntity();
            fieldTypeListItemEntity2.setLabel("label2");
            fieldTypeListItemEntity2.setOrder(2);
            FieldTypeListItemEntity fieldTypeListItemEntity3 = new FieldTypeListItemEntity();
            fieldTypeListItemEntity3.setLabel("label3");
            fieldTypeListItemEntity3.setOrder(1);
            FieldTypeListItemEntity fieldTypeListItemEntity4 = new FieldTypeListItemEntity();
            fieldTypeListItemEntity4.setLabel("label4");
            fieldTypeListItemEntity4.setOrder(null);
            FieldTypeListItemEntity fieldTypeListItemEntity5 = new FieldTypeListItemEntity();
            fieldTypeListItemEntity5.setLabel("label5");
            fieldTypeListItemEntity5.setOrder(null);
            FixedListItem fixedListItem1 = new FixedListItem();
            fixedListItem1.setLabel("label1");
            FixedListItem fixedListItem2 = new FixedListItem();
            fixedListItem2.setLabel("label2");
            FixedListItem fixedListItem3 = new FixedListItem();
            fixedListItem3.setLabel("label3");
            FixedListItem fixedListItem4 = new FixedListItem();
            fixedListItem4.setLabel("label4");
            FixedListItem fixedListItem5 = new FixedListItem();
            fixedListItem5.setLabel("label5");
            when(spyOnClassUnderTest.map(fieldTypeListItemEntity1)).thenReturn(fixedListItem1);
            when(spyOnClassUnderTest.map(fieldTypeListItemEntity2)).thenReturn(fixedListItem2);
            when(spyOnClassUnderTest.map(fieldTypeListItemEntity3)).thenReturn(fixedListItem3);
            when(spyOnClassUnderTest.map(fieldTypeListItemEntity4)).thenReturn(fixedListItem4);
            when(spyOnClassUnderTest.map(fieldTypeListItemEntity5)).thenReturn(fixedListItem5);
            fieldTypeEntity.addListItems(
                asList(fieldTypeListItemEntity1, fieldTypeListItemEntity4, fieldTypeListItemEntity5, fieldTypeListItemEntity2, fieldTypeListItemEntity3));

            FieldTypeEntity collectionFieldTypeEntity = fieldTypeEntity("CollectionFieldType");
            FieldType collectionFieldType = new FieldType();
            when(spyOnClassUnderTest.map(collectionFieldTypeEntity)).thenReturn(collectionFieldType);

            FieldType fieldType = spyOnClassUnderTest.map(fieldTypeEntity);

            assertEquals(fieldTypeEntity.getReference(), fieldType.getId());
            assertEquals(fieldTypeEntity.getMinimum(), fieldType.getMin());
            assertEquals(fieldTypeEntity.getMaximum(), fieldType.getMax());
            assertEquals(fieldTypeEntity.getRegularExpression(), fieldType.getRegularExpression());

            assertEquals(fieldTypeEntity.getListItems().size(), fieldType.getFixedListItems().size());
            assertEquals(fieldType.getFixedListItems().get(0).getLabel(), fixedListItem3.getLabel());
            assertEquals(fieldType.getFixedListItems().get(1).getLabel(), fixedListItem2.getLabel());
            assertEquals(fieldType.getFixedListItems().get(2).getLabel(), fixedListItem1.getLabel());
            assertEquals(fieldType.getFixedListItems().get(3).getLabel(), fixedListItem4.getLabel());
            assertEquals(fieldType.getFixedListItems().get(4).getLabel(), fixedListItem5.getLabel());

            return fieldType;

        }

        private FieldType mapAssertComplexCommonFieldsAndReturn(FieldTypeEntity fieldTypeEntity) {
            fieldTypeEntity.setMinimum("Min");
            fieldTypeEntity.setMaximum("Max");
            fieldTypeEntity.setRegularExpression("SomeRegex");

            ComplexFieldEntity complexFieldEntity1 = new ComplexFieldEntity();
            complexFieldEntity1.setLabel("label1");
            ComplexFieldEntity complexFieldEntity2 = new ComplexFieldEntity();
            complexFieldEntity2.setLabel("label2");
            ComplexFieldEntity complexFieldEntity3 = new ComplexFieldEntity();
            complexFieldEntity3.setLabel("label3");
            CaseField complexField1 = new CaseField();
            complexField1.setLabel("label1");
            CaseField complexField2 = new CaseField();
            complexField2.setLabel("label2");
            CaseField complexField3 = new CaseField();
            complexField3.setLabel("label3");
            when(spyOnClassUnderTest.map(complexFieldEntity1)).thenReturn(complexField1);
            when(spyOnClassUnderTest.map(complexFieldEntity2)).thenReturn(complexField2);
            when(spyOnClassUnderTest.map(complexFieldEntity3)).thenReturn(complexField3);
            fieldTypeEntity.addComplexFields(asList(complexFieldEntity1, complexFieldEntity2, complexFieldEntity3));
            FieldTypeEntity collectionFieldTypeEntity = fieldTypeEntity("CollectionFieldType");
            FieldType collectionFieldType = new FieldType();
            when(spyOnClassUnderTest.map(collectionFieldTypeEntity)).thenReturn(collectionFieldType);

            FieldType fieldType = spyOnClassUnderTest.map(fieldTypeEntity);

            assertEquals(fieldTypeEntity.getReference(), fieldType.getId());
            assertEquals(fieldTypeEntity.getMinimum(), fieldType.getMin());
            assertEquals(fieldTypeEntity.getMaximum(), fieldType.getMax());
            assertEquals(fieldTypeEntity.getRegularExpression(), fieldType.getRegularExpression());

            assertEquals(fieldTypeEntity.getComplexFields().size(), fieldType.getComplexFields().size());
            assertEquals(fieldType.getComplexFields().get(0).getLabel(), complexField1.getLabel());
            assertEquals(fieldType.getComplexFields().get(1).getLabel(), complexField2.getLabel());
            assertEquals(fieldType.getComplexFields().get(2).getLabel(), complexField3.getLabel());

            return fieldType;

        }

    }

    @Nested
    @DisplayName("Should create a CaseTypeTab matching DisplayGroupEntity fields")
    class MapDisplayGroupEntityTests {

        @Test
        void testMapDisplayGroupEntity() {

            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference("Role1");
            userRoleEntity.setName("Role 1");
            DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
            displayGroupEntity.setReference("Reference");
            displayGroupEntity.setLabel("Label");
            displayGroupEntity.setUserRole(userRoleEntity);
            displayGroupEntity.setOrder(69);

            DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity1 = new DisplayGroupCaseFieldEntity();
            DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity2 = new DisplayGroupCaseFieldEntity();
            DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity3 = new DisplayGroupCaseFieldEntity();

            CaseTypeTabField caseTypeTabField1 = new CaseTypeTabField();
            CaseTypeTabField caseTypeTabField2 = new CaseTypeTabField();
            CaseTypeTabField caseTypeTabField3 = new CaseTypeTabField();

            when(spyOnClassUnderTest.map(displayGroupCaseFieldEntity1)).thenReturn(caseTypeTabField1);
            when(spyOnClassUnderTest.map(displayGroupCaseFieldEntity2)).thenReturn(caseTypeTabField2);
            when(spyOnClassUnderTest.map(displayGroupCaseFieldEntity3)).thenReturn(caseTypeTabField3);

            displayGroupEntity.addDisplayGroupCaseFields(
                asList(
                    displayGroupCaseFieldEntity1,
                    displayGroupCaseFieldEntity2,
                    displayGroupCaseFieldEntity3
                )
            );

            CaseTypeTab caseTypeTab = spyOnClassUnderTest.map(displayGroupEntity);

            assertEquals(displayGroupEntity.getReference(), caseTypeTab.getId());
            assertEquals(displayGroupEntity.getLabel(), caseTypeTab.getLabel());
            assertEquals(displayGroupEntity.getOrder(), caseTypeTab.getOrder());

            assertEquals(displayGroupEntity.getDisplayGroupCaseFields().size(), caseTypeTab.getTabFields().size());
            assertThat(caseTypeTab.getTabFields(), hasItems(caseTypeTabField1, caseTypeTabField2, caseTypeTabField3));
            assertEquals(caseTypeTab.getRole(), displayGroupEntity.getUserRole().getReference());

        }

    }

    @Nested
    @DisplayName("Should return a CaseRole that matches CaseRoleEntity")
    class CaseRoleEntityTests {
        private final String reference = "Ref";
        private final String name = "Name";
        private final String description = "Some description";

        @Test
        void shouldReturnCaseRole() {
            CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
            caseRoleEntity.setReference(reference);
            caseRoleEntity.setName(name);
            caseRoleEntity.setDescription(description);

            final CaseRole caseRole = classUnderTest.map(caseRoleEntity);

            assertAll(
                () -> assertThat(caseRole.getId(), is(caseRoleEntity.getReference())),
                () -> assertThat(caseRole.getName(), is(caseRoleEntity.getName())),
                () -> assertThat(caseRole.getDescription(), is(caseRoleEntity.getDescription()))
            );
        }
    }

    @Nested
    @DisplayName("Should create a CaseTypeTabField matching DisplayGroupCaseFieldEntity fields")
    class DisplayGroupCaseFieldEntityTests {

        @Test
        void testMapDisplayGroupCaseFieldEntity() {
            DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
            displayGroupCaseFieldEntity.setOrder(69);

            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            CaseField caseField = new CaseField();
            when(spyOnClassUnderTest.map(caseFieldEntity)).thenReturn(caseField);
            displayGroupCaseFieldEntity.setCaseField(caseFieldEntity);

            CaseTypeTabField caseTypeTabField = spyOnClassUnderTest.map(displayGroupCaseFieldEntity);

            assertEquals(displayGroupCaseFieldEntity.getOrder(), caseTypeTabField.getOrder());
            assertEquals(caseField, caseTypeTabField.getCaseField());
        }

    }

    @Nested
    @DisplayName("Should create a SearchInputField matching SearchInputCaseFieldEntity fields")
    class SearchInputCaseFieldEntityTests {

        @Test
        void testMapSearchInputCaseFieldEntity() {
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference("role1");
            SearchInputCaseFieldEntity searchInputCaseFieldEntity = new SearchInputCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            searchInputCaseFieldEntity.setCaseField(caseFieldEntity);
            searchInputCaseFieldEntity.setLabel("Label");
            searchInputCaseFieldEntity.setOrder(69);
            searchInputCaseFieldEntity.setUserRole(userRoleEntity);
            searchInputCaseFieldEntity.setCaseFieldElementPath("Field1.Field2");
            searchInputCaseFieldEntity.setShowCondition("aShowCondition");
            searchInputCaseFieldEntity.setDisplayContextParameter("DisplayContextParameter");

            SearchInputField searchInputField = spyOnClassUnderTest.map(searchInputCaseFieldEntity);

            assertEquals(searchInputCaseFieldEntity.getOrder(), searchInputField.getOrder());
            assertEquals(searchInputCaseFieldEntity.getLabel(), searchInputField.getLabel());
            assertEquals(searchInputCaseFieldEntity.getCaseFieldElementPath(), searchInputField.getCaseFieldElementPath());
            assertEquals(searchInputCaseFieldEntity.getShowCondition(), searchInputField.getShowCondition());
            assertEquals(searchInputCaseFieldEntity.getCaseField().getReference(), searchInputField.getCaseFieldId());
            assertEquals(searchInputCaseFieldEntity.getDisplayContextParameter(), searchInputField.getDisplayContextParameter());
            assertEquals(userRoleEntity.getReference(), searchInputField.getRole());
        }

    }

    @Nested
    @DisplayName("Should create a SearchResultField matching SearchResultCaseFieldEntity fields")
    class SearchResultCaseFieldEntityTests {

        @Test
        void testMapSearchResultCaseFieldEntity() {
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference("role1");
            SearchResultCaseFieldEntity searchResultCaseFieldEntity = new SearchResultCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            searchResultCaseFieldEntity.setCaseField(caseFieldEntity);
            searchResultCaseFieldEntity.setCaseFieldElementPath("SomePath");
            searchResultCaseFieldEntity.setLabel("Label");
            searchResultCaseFieldEntity.setOrder(69);
            searchResultCaseFieldEntity.setUserRole(userRoleEntity);
            SortOrder sortOrder = new SortOrder(2, "ASC");
            searchResultCaseFieldEntity.setSortOrder(sortOrder);
            searchResultCaseFieldEntity.setDisplayContextParameter("DisplayContextParameter");

            SearchResultsField searchResultsField = spyOnClassUnderTest.map(searchResultCaseFieldEntity);

            assertEquals(searchResultCaseFieldEntity.getOrder(), searchResultsField.getOrder());
            assertEquals(searchResultCaseFieldEntity.getCaseFieldElementPath(), searchResultsField.getCaseFieldElementPath());
            assertEquals(searchResultCaseFieldEntity.getLabel(), searchResultsField.getLabel());
            assertEquals(searchResultCaseFieldEntity.getCaseField().getReference(),
                searchResultsField.getCaseFieldId());
            assertEquals(searchResultCaseFieldEntity.getDisplayContextParameter(), searchResultsField.getDisplayContextParameter());
            assertThat(searchResultsField.isMetadata(), is(false));
            assertEquals(userRoleEntity.getReference(), searchResultsField.getRole());

            assertEquals(sortOrder.getDirection(), searchResultsField.getSortOrder().getDirection());
            assertEquals(sortOrder.getPriority(), searchResultsField.getSortOrder().getPriority());
        }

        @Test
        void shouldSetMetadataFlagOnDto() {
            SearchResultCaseFieldEntity searchResultCaseFieldEntity = new SearchResultCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            caseFieldEntity.setDataFieldType(DataFieldType.METADATA);
            searchResultCaseFieldEntity.setCaseField(caseFieldEntity);
            searchResultCaseFieldEntity.setLabel("Label");
            searchResultCaseFieldEntity.setOrder(69);

            SearchResultsField searchResultsField = spyOnClassUnderTest.map(searchResultCaseFieldEntity);

            assertThat(searchResultsField.isMetadata(), is(true));
        }

    }

    @Nested
    @DisplayName("Should create a WorkBasketInputField matching WorkBasketInputCaseFieldEntity fields")
    class WorkBasketInputCaseFieldEntityTests {

        @Test
        void testMapWorkBasketInputCaseFieldEntity() {
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference("role1");
            WorkBasketInputCaseFieldEntity workBasketInputCaseFieldEntity = new WorkBasketInputCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            workBasketInputCaseFieldEntity.setCaseField(caseFieldEntity);
            workBasketInputCaseFieldEntity.setLabel("Label");
            workBasketInputCaseFieldEntity.setOrder(69);
            workBasketInputCaseFieldEntity.setUserRole(userRoleEntity);
            workBasketInputCaseFieldEntity.setCaseFieldElementPath("Field1.Field2");
            workBasketInputCaseFieldEntity.setShowCondition("aShowCondition");
            workBasketInputCaseFieldEntity.setDisplayContextParameter("DisplayContextParameter");

            WorkbasketInputField workbasketInputField = spyOnClassUnderTest.map(workBasketInputCaseFieldEntity);

            assertEquals(workBasketInputCaseFieldEntity.getShowCondition(), workbasketInputField.getShowCondition());
            assertEquals(workBasketInputCaseFieldEntity.getOrder(), workbasketInputField.getOrder());
            assertEquals(workBasketInputCaseFieldEntity.getLabel(), workbasketInputField.getLabel());
            assertEquals(workBasketInputCaseFieldEntity.getCaseFieldElementPath(), workbasketInputField.getCaseFieldElementPath());
            assertEquals(workBasketInputCaseFieldEntity.getCaseField().getReference(), workbasketInputField.getCaseFieldId());
            assertEquals(workBasketInputCaseFieldEntity.getDisplayContextParameter(), workbasketInputField.getDisplayContextParameter());
            assertEquals(userRoleEntity.getReference(), workbasketInputField.getRole());
        }

    }

    @Nested
    @DisplayName("Should create a WorkBasketResult matching WorkBasketCaseFieldEntity fields")
    class WorkBasketCaseFieldEntityTests {

        @Test
        void testMapWorkBasketCaseFieldEntity() {
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference("role1");
            WorkBasketCaseFieldEntity workBasketCaseFieldEntity = new WorkBasketCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            workBasketCaseFieldEntity.setCaseField(caseFieldEntity);
            workBasketCaseFieldEntity.setCaseFieldElementPath("SomePath");
            workBasketCaseFieldEntity.setLabel("Label");
            workBasketCaseFieldEntity.setOrder(69);
            workBasketCaseFieldEntity.setUserRole(userRoleEntity);
            SortOrder sortOrder = new SortOrder(2, "ASC");
            workBasketCaseFieldEntity.setSortOrder(sortOrder);
            workBasketCaseFieldEntity.setDisplayContextParameter("DisplayContextParameter");

            WorkBasketResultField workBasketResult = spyOnClassUnderTest.map(workBasketCaseFieldEntity);

            assertEquals(workBasketCaseFieldEntity.getOrder(), workBasketResult.getOrder());
            assertEquals(workBasketCaseFieldEntity.getCaseFieldElementPath(), workBasketResult.getCaseFieldElementPath());
            assertEquals(workBasketCaseFieldEntity.getLabel(), workBasketResult.getLabel());
            assertEquals(workBasketCaseFieldEntity.getCaseField().getReference(), workBasketResult.getCaseFieldId());
            assertEquals(workBasketCaseFieldEntity.getDisplayContextParameter(), workBasketResult.getDisplayContextParameter());
            assertEquals(userRoleEntity.getReference(), workBasketResult.getRole());

            assertEquals(sortOrder.getDirection(), workBasketResult.getSortOrder().getDirection());
            assertEquals(sortOrder.getPriority(), workBasketResult.getSortOrder().getPriority());
        }

        @Test
        void shouldSetMetadataFlagOnDto() {
            WorkBasketCaseFieldEntity workBasketCaseFieldEntity = new WorkBasketCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            caseFieldEntity.setDataFieldType(DataFieldType.METADATA);
            workBasketCaseFieldEntity.setCaseField(caseFieldEntity);
            workBasketCaseFieldEntity.setLabel("Label");
            workBasketCaseFieldEntity.setOrder(69);

            WorkBasketResultField workBasketResult = spyOnClassUnderTest.map(workBasketCaseFieldEntity);

            assertThat(workBasketResult.isMetadata(), is(true));
        }

    }

    @Nested
    @DisplayName("Should create a SearchCasesResultFields matching SearchCasesResultFieldsEntity fields")
    class SearchCasesResultFieldsEntityTests {

        @Test
        void testMapSearchCasesResultFieldEntity() {
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setReference("role1");
            SearchCasesResultFieldEntity searchCasesResultFieldEntity = new SearchCasesResultFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            searchCasesResultFieldEntity.setCaseField(caseFieldEntity);
            searchCasesResultFieldEntity.setCaseFieldElementPath("SomePath");
            searchCasesResultFieldEntity.setLabel("Label");
            searchCasesResultFieldEntity.setOrder(69);
            searchCasesResultFieldEntity.setUserRole(userRoleEntity);
            SortOrder sortOrder = new SortOrder(2, "ASC");
            searchCasesResultFieldEntity.setSortOrder(sortOrder);
            searchCasesResultFieldEntity.setDisplayContextParameter("DisplayContextParameter");
            searchCasesResultFieldEntity.setUseCase("orgCase");

            SearchCasesResultField searchCasesResultField = spyOnClassUnderTest.map(searchCasesResultFieldEntity);

            assertEquals(searchCasesResultFieldEntity.getOrder(), searchCasesResultField.getOrder());
            assertEquals(searchCasesResultFieldEntity.getCaseFieldElementPath(), searchCasesResultField.getCaseFieldElementPath());
            assertEquals(searchCasesResultFieldEntity.getLabel(), searchCasesResultField.getLabel());
            assertEquals(searchCasesResultFieldEntity.getCaseField().getReference(), searchCasesResultField.getCaseFieldId());
            assertEquals(searchCasesResultFieldEntity.getDisplayContextParameter(), searchCasesResultField.getDisplayContextParameter());
            assertEquals(searchCasesResultFieldEntity.getUseCase(), searchCasesResultField.getUseCase());
            assertEquals(userRoleEntity.getReference(), searchCasesResultField.getRole());

            assertEquals(sortOrder.getDirection(), searchCasesResultField.getSortOrder().getDirection());
            assertEquals(sortOrder.getPriority(), searchCasesResultField.getSortOrder().getPriority());

        }

        @Test
        void shouldSetMetadataFlagOnDto() {
            SearchCasesResultFieldEntity searchCasesResultFieldEntity = new SearchCasesResultFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            caseFieldEntity.setDataFieldType(DataFieldType.METADATA);
            searchCasesResultFieldEntity.setCaseField(caseFieldEntity);
            searchCasesResultFieldEntity.setLabel("Label");
            searchCasesResultFieldEntity.setOrder(69);

            SearchCasesResultField searchCasesResultField = spyOnClassUnderTest.map(searchCasesResultFieldEntity);

            assertThat(searchCasesResultField.isMetadata(), is(true));
        }

    }

    @Nested
    @DisplayName("Should return a CaseTypeLite model object whose fields match those in the CaseTypeLiteEntity")
    class MapCaseTypeEntitySubsetTests {

        @Test
        void testMapSubsetCaseTypeEntity() {
            CaseTypeLiteEntity caseTypeLiteEntity = caseTypeLiteEntity();

            CaseTypeLite caseTypeLite = classUnderTest.map(caseTypeLiteEntity);

            // Assertions
            assertEquals(caseTypeLite.getId(), caseTypeLiteEntity.getReference());
            assertEquals(caseTypeLite.getDescription(), caseTypeLiteEntity.getDescription());
            assertEquals(caseTypeLite.getName(), caseTypeLiteEntity.getName());
            assertEquals(1, caseTypeLite.getStates().size());
        }

        @Test
        void testMapEmptyCaseTypeEntity() {
            CaseTypeLiteEntity caseTypeLiteEntity = new CaseTypeLiteEntity();

            CaseTypeLite caseTypeLite = classUnderTest.map(caseTypeLiteEntity);

            // Assertions
            assertNull(caseTypeLite.getId());
            assertNull(caseTypeLite.getDescription());
            assertNull(caseTypeLite.getName());
            assertTrue(caseTypeLite.getStates().isEmpty());
        }

        private CaseTypeLiteEntity caseTypeLiteEntity() {
            CaseTypeLiteEntity caseTypeLiteEntity = new CaseTypeLiteEntity();
            caseTypeLiteEntity.setVersion(100);
            caseTypeLiteEntity.setReference("Reference");
            caseTypeLiteEntity.setName("Name");
            caseTypeLiteEntity.addState(new StateEntity());
            return caseTypeLiteEntity;
        }
    }

    @Nested
    @DisplayName("Should return a JurisdictionUiConfig model whose fields match those in the JurisdictionUiConfigEntity")
    class MapJurisdictionUiConfigEntityTests {

        @Test
        void testMapJurisdictionUiConfigEntity() {
            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
            jurisdictionEntity.setReference("Reference");
            jurisdictionEntity.setName("Name");

            JurisdictionUiConfigEntity jurisdictionUiConfigEntity = new JurisdictionUiConfigEntity();
            jurisdictionUiConfigEntity.setShuttered(true);
            jurisdictionUiConfigEntity.setJurisdiction(jurisdictionEntity);

            JurisdictionUiConfig jurisdictionUiConfig = classUnderTest.map(jurisdictionUiConfigEntity);

            assertEquals(jurisdictionUiConfig.getShuttered(), jurisdictionUiConfigEntity.getShuttered());
            assertEquals(jurisdictionUiConfig.getName(), jurisdictionUiConfigEntity.getJurisdiction().getName());
            assertEquals(jurisdictionUiConfig.getId(), jurisdictionUiConfigEntity.getJurisdiction().getReference());
        }

        @Test
        void testMapEmptyJurisdictionUiConfigEntity() {
            JurisdictionUiConfigEntity jurisdictionUiConfigEntity = new JurisdictionUiConfigEntity();

            JurisdictionUiConfig jurisdictionUiConfig = classUnderTest.map(jurisdictionUiConfigEntity);

            assertNull(jurisdictionUiConfig.getId());
            assertNull(jurisdictionUiConfig.getName());
            assertNull(jurisdictionUiConfig.getShuttered());
        }
    }

    private void assertComplexACLs(List<ComplexFieldACLEntity> authorisation, List<ComplexACL> accessControlList) {
        for (ComplexFieldACLEntity authItem : authorisation) {
            assertThat(accessControlList, hasItem(aclWhichMatchesComplexFieldACL(authItem)));
        }
    }

    private void assertAcls(List<? extends Authorisation> authorisation, List<AccessControlList> accessControlList) {
        assertEquals(authorisation.size(), accessControlList.size());
        for (Authorisation authItem : authorisation) {
            assertThat(accessControlList,
                hasItem(
                    aclWhichMatchesAuthorisation(authItem)
                )
            );
        }
    }

    private <T> Matcher<T> aclWhichMatchesAuthorisation(Authorisation authorisation) {

        String role = authorisation.getUserRole().getReference();

        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof AccessControlList
                    && ((AccessControlList) o).getRole().equals(role)
                    && ((AccessControlList) o).getCreate().equals(authorisation.getCreate())
                    && ((AccessControlList) o).getRead().equals(authorisation.getRead())
                    && ((AccessControlList) o).getUpdate().equals(authorisation.getUpdate())
                    && ((AccessControlList) o).getDelete().equals(authorisation.getDelete());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                    String.format(
                        "an AccessControlList with role %s, create %s, read %s, update %s, delete %s",
                        role,
                        authorisation.getCreate(),
                        authorisation.getRead(),
                        authorisation.getUpdate(),
                        authorisation.getDelete()
                    )
                );
            }
        };
    }

    private <T> Matcher<T> aclWhichMatchesComplexFieldACL(ComplexFieldACLEntity aclEntity) {

        String role = aclEntity.getUserRole().getReference();

        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof ComplexACL
                    && ((ComplexACL) o).getRole().equals(role)
                    && ((ComplexACL) o).getCreate().equals(aclEntity.getCreate())
                    && ((ComplexACL) o).getRead().equals(aclEntity.getRead())
                    && ((ComplexACL) o).getUpdate().equals(aclEntity.getUpdate())
                    && ((ComplexACL) o).getDelete().equals(aclEntity.getDelete())
                    && ((ComplexACL) o).getListElementCode().equals(aclEntity.getListElementCode());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                    String.format(
                        "an AccessControlList with role %s, create %s, read %s, update %s, delete %s, listElementCode %s",
                        role,
                        aclEntity.getCreate(),
                        aclEntity.getRead(),
                        aclEntity.getUpdate(),
                        aclEntity.getDelete(),
                        aclEntity.getListElementCode()
                    )
                );
            }
        };
    }

    @Nested
    @DisplayName("Should return `a Banner which matches the BannerEntity")
    class MapBannerEntityTests {

        @Test
        void testMapBannerEntity() {

            BannerEntity bannerEntity = new BannerEntity();
            bannerEntity.setBannerUrlText("Click here to see it.>>>");
            bannerEntity.setBannerEnabled(true);
            bannerEntity.setBannerUrl("http://localhost:3451/test");
            bannerEntity.setBannerDescription("Test Description");

            Banner banner = classUnderTest.map(bannerEntity);

            assertEquals(bannerEntity.getBannerDescription(), banner.getBannerDescription());
            assertEquals(bannerEntity.getBannerEnabled(), banner.getBannerEnabled());
            assertEquals(bannerEntity.getBannerUrl(), banner.getBannerUrl());
            assertEquals(bannerEntity.getBannerUrlText(), banner.getBannerUrlText());
        }

        @Test
        void testMapEmptyBannerEntity() {

            BannerEntity bannerEntity = new BannerEntity();

            Banner banner = classUnderTest.map(bannerEntity);

            assertNull(banner.getBannerDescription());
            assertNull(banner.getBannerEnabled());
            assertNull(banner.getBannerUrl());
            assertNull(banner.getBannerUrlText());
        }
    }

}
