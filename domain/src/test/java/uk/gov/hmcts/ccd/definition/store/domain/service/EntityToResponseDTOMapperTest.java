package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;
import uk.gov.hmcts.ccd.definition.store.repository.model.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class EntityToResponseDTOMapperTest {

    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String LIVE_FROM = "2017-02-02";

    private static final String LIVE_TO = "2018-03-03";

    EntityToResponseDTOMapper classUnderTest = new EntityToResponseDTOMapperImpl();

    EntityToResponseDTOMapper spyOnClassUnderTest;

    @BeforeEach
    public void setUpSpy() throws Exception {
        spyOnClassUnderTest = spy(classUnderTest);
    }

    @Nested
    @DisplayName("Should return a CaseEventField which matches the EventCaseFieldEntity")
    class MapEventCaseFieldEntity {

        @Test
        public void testMapEventCaseFieldEntity() throws Exception {
            EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
            eventCaseFieldEntity.setShowCondition("PersonFirstName=\"John\"");
            eventCaseFieldEntity.setShowSummaryChangeOption(true);
            eventCaseFieldEntity.setShowSummaryContentOption(2);
            eventCaseFieldEntity.setDisplayContext(DisplayContext.MANDATORY);

            CaseEventField caseEventField = spyOnClassUnderTest.map(
                eventCaseFieldEntity
            );

            assertAll(
                () -> assertEquals("displayContext", eventCaseFieldEntity.getDisplayContext().name(), caseEventField.getDisplayContext()),
                () -> assertEquals("showCondition", eventCaseFieldEntity.getShowCondition(), caseEventField.getShowCondition()),
                () -> assertEquals("showSummaryChangeOption", eventCaseFieldEntity.getShowSummaryChangeOption(), caseEventField.getShowSummaryChangeOption()),
                () -> assertEquals("showSummaryContentOption", eventCaseFieldEntity.getShowSummaryContentOption(), caseEventField.getShowSummaryContentOption())
            );
        }
    }

    @Nested
    @DisplayName("Should return a CaseEventField which matches the EventCaseFieldEntity")
    class MapEventEntity {

        @Test
        public void shouldMapToCaseEvent() throws Exception {
            EventEntity eventEntity = new EventEntity();
            eventEntity.setShowEventNotes(true);

            CaseEvent caseEvent = spyOnClassUnderTest.map(
                eventEntity
            );

            assertAll(
                () -> assertEquals(eventEntity.getShowEventNotes(), caseEvent.getShowEventNotes())
            );
        }
    }

    @Nested
    @DisplayName("Should return a CaseType which matches the CaseTypeEntity")
    class MapCaseTypeEntityTests {

        @Test
        public void testMapCaseTypeEntity() throws Exception {

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

            CaseTypeUserRoleEntity roleWithCreateOnly = caseTypeUserRoleEntity("role-with-create-only", true, false, false, false);
            CaseTypeUserRoleEntity roleWithReadOnly = caseTypeUserRoleEntity("role-with-read-only", false, true, false, false);
            CaseTypeUserRoleEntity roleWithUpdateOnly = caseTypeUserRoleEntity("role-with-update-only", false, false, true, false);
            CaseTypeUserRoleEntity roleWithDeleteOnly = caseTypeUserRoleEntity("role-with-delete-only", false, false, false, true);

            CaseTypeEntity caseTypeEntity = caseTypeEntity(
                jurisdictionEntity,
                Arrays.asList(eventEntity1, eventEntity2, eventEntity3),
                Arrays.asList(stateEntity1, stateEntity2, stateEntity3),
                Arrays.asList(roleWithCreateOnly, roleWithReadOnly, roleWithUpdateOnly, roleWithDeleteOnly),
                Arrays.asList(caseFieldEntity1, caseFieldEntity2, caseFieldEntity3)
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
            assertAcls(caseTypeEntity.getCaseTypeUserRoleEntities(), caseType.getAcls());

            assertEquals(3, caseType.getCaseFields().size());
            assertThat(caseType.getCaseFields(), hasItems(caseField1, caseField2, caseField3));

        }

        @Test
        public void testMapEmptyCaseTypeEntity() throws Exception {

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
                                              List<CaseTypeUserRoleEntity> roles,
                                              List<CaseFieldEntity> caseFieldEntities) {
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
            caseTypeEntity.addCaseTypeUserRoles(roles);
            caseTypeEntity.addCaseFields(caseFieldEntities);

            return caseTypeEntity;
        }

        private CaseTypeUserRoleEntity caseTypeUserRoleEntity(String role,
                                                              Boolean create,
                                                              Boolean read,
                                                              Boolean update,
                                                              Boolean delete) {
            CaseTypeUserRoleEntity caseTypeUserRoleEntity = new CaseTypeUserRoleEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setRole(role);
            caseTypeUserRoleEntity.setUserRole(userRoleEntity);
            caseTypeUserRoleEntity.setCreate(create);
            caseTypeUserRoleEntity.setRead(read);
            caseTypeUserRoleEntity.setUpdate(update);
            caseTypeUserRoleEntity.setDelete(delete);
            return caseTypeUserRoleEntity;
        }
    }

    @Nested
    @DisplayName("Should return a Jurisdiction which matches the JurisdictionEntity")
    class MapJurisdictionEntityTests {

        @Test
        public void testMapJurisdictionEntity() throws Exception {

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
        public void testMapEmptyJurisdictionEntity() throws Exception {

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
    @DisplayName("Should create a CaseEvent matching EventEntity fields, with the following exceptions/amendments:" +
        "- preStates should be empty if canCreate is true " +
        "- preStates should default to a single 'wildcard' entry if not defined in entity" +
        "- postState should default to 'wildcard' if not defined in entity")
    class MapEventEntityTests {

        @Test
        public void testMapEventEntity() throws Exception {
            for (Parameters parameters : createParameters()) {
                CaseEvent caseEvent = mapEventWithStatesAssertCommonFieldsAndReturn(
                    parameters
                );
                assertEquals(parameters.getPreStateExpectation(), caseEvent.getPreStates());
                assertEquals(parameters.getPostStateExpectation(), caseEvent.getPostState());
            }
        }

        @Test
        public void testMapEmptyEventEntity() throws Exception {
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

            assertEquals(1, caseEvent.getPreStates().size());
            assertThat(caseEvent.getPreStates(), hasItems("*"));
            assertEquals("*", caseEvent.getPostState());

        }

        private List<Parameters> createParameters() {
            return Arrays.asList(
                new Parameters(
                    false, Collections.EMPTY_LIST, null,
                    Arrays.asList("*"), "*"
                ),
                new Parameters(
                    false, Collections.EMPTY_LIST, stateEntity("PostState"),
                    Arrays.asList("*"), "PostState"
                ),
                new Parameters(
                    false, Arrays.asList(stateEntity("preState1"), stateEntity("preState2"), stateEntity("preState3")), null,
                    Arrays.asList("preState1", "preState2", "preState3"), "*"
                ),
                new Parameters(
                    false, Arrays.asList(stateEntity("preState1"), stateEntity("preState2"), stateEntity("preState3")), stateEntity("PostState"),
                    Arrays.asList("preState1", "preState2", "preState3"), "PostState"
                ),
                new Parameters(
                    true, Collections.EMPTY_LIST, null,
                    Collections.EMPTY_LIST, "*"
                ),
                new Parameters(
                    true, Collections.EMPTY_LIST, stateEntity("PostState"),
                    Collections.EMPTY_LIST, "PostState"
                ),
                new Parameters(
                    true, Arrays.asList(stateEntity("preState1"), stateEntity("preState2"), stateEntity("preState3")), null,
                    Collections.EMPTY_LIST, "*"
                ),
                new Parameters(
                    true, Arrays.asList(stateEntity("preState1"), stateEntity("preState2"), stateEntity("preState3")), stateEntity("PostState"),
                    Collections.EMPTY_LIST, "PostState"
                )
            );
        }

        private StateEntity stateEntity(String reference) {
            StateEntity stateEntity = new StateEntity();
            stateEntity.setReference(reference);
            return stateEntity;
        }

        private class Parameters {

            private Boolean canCreate;
            private List<StateEntity> preStates;
            private StateEntity postState;
            private List<String> preStateExpectation;
            private String postStateExpectation;

            public Parameters(Boolean canCreate,
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

            public Boolean getCanCreate() {
                return canCreate;
            }

            public List<StateEntity> getPreStates() {
                return preStates;
            }

            public StateEntity getPostState() {
                return postState;
            }

            public List<String> getPreStateExpectation() {
                return preStateExpectation;
            }

            public String getPostStateExpectation() {
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

            Integer startTimeout1 = 691;
            Integer startTimeout2 = 692;
            Integer startTimeout3 = 693;
            eventEntity.setWebhookStart(webHook("Start", startTimeout1, startTimeout2, startTimeout3));
            Integer preSubmitTimeout1 = 694;
            Integer preSubmitTimeout2 = 695;
            Integer preSubmitTimeout3 = 696;
            eventEntity.setWebhookPreSubmit(webHook("PreSubmit", preSubmitTimeout1, preSubmitTimeout2, preSubmitTimeout3));
            Integer postSubmitTimeout1 = 697;
            Integer postSubmitTimeout2 = 698;
            Integer postSubmitTimeout3 = 699;
            eventEntity.setWebhookPostSubmit(webHook("PostSubmit", postSubmitTimeout1, postSubmitTimeout2, postSubmitTimeout3));

            EventCaseFieldEntity eventCaseFieldEntity1 = new EventCaseFieldEntity();
            EventCaseFieldEntity eventCaseFieldEntity2 = new EventCaseFieldEntity();
            EventCaseFieldEntity eventCaseFieldEntity3 = new EventCaseFieldEntity();
            CaseEventField caseEventField1 = new CaseEventField();
            CaseEventField caseEventField2 = new CaseEventField();
            CaseEventField caseEventField3 = new CaseEventField();
            when(spyOnClassUnderTest.map(eventCaseFieldEntity1)).thenReturn(caseEventField1);
            when(spyOnClassUnderTest.map(eventCaseFieldEntity2)).thenReturn(caseEventField2);
            when(spyOnClassUnderTest.map(eventCaseFieldEntity3)).thenReturn(caseEventField3);
            eventEntity.addEventCaseFields(Arrays.asList(eventCaseFieldEntity1, eventCaseFieldEntity2, eventCaseFieldEntity3));

            EventUserRoleEntity roleWithCreateOnly = eventUserRoleEntity("role-with-create-only", true, false, false, false);
            EventUserRoleEntity roleWithReadOnly = eventUserRoleEntity("role-with-read-only", false, true, false, false);
            EventUserRoleEntity roleWithUpdateOnly = eventUserRoleEntity("role-with-update-only", false, false, true, false);
            EventUserRoleEntity roleWithDeleteOnly = eventUserRoleEntity("role-with-delete-only", false, false, false, true);
            eventEntity.addEventUserRoles(Arrays.asList(roleWithCreateOnly, roleWithReadOnly, roleWithUpdateOnly, roleWithDeleteOnly));

            eventEntity.setCanCreate(parameters.getCanCreate());
            parameters.getPreStates().forEach(ps -> eventEntity.addPreState(ps));
            eventEntity.setPostState(parameters.getPostState());

            CaseEvent caseEvent = spyOnClassUnderTest.map(eventEntity);

            assertEquals(eventEntity.getReference(), caseEvent.getId());
            assertEquals(eventEntity.getName(), caseEvent.getName());
            assertEquals(eventEntity.getDescription(), caseEvent.getDescription());
            assertEquals(eventEntity.getOrder(), caseEvent.getOrder());

            assertEquals(3, caseEvent.getCaseFields().size());
            assertThat(caseEvent.getCaseFields(), hasItems(caseEventField1, caseEventField2, caseEventField3));

            assertEquals(eventEntity.getWebhookStart().getUrl(), caseEvent.getCallBackURLAboutToStartEvent());
            assertEquals(eventEntity.getWebhookStart().getTimeouts().size(), caseEvent.getRetriesTimeoutAboutToStartEvent().size());
            assertThat(caseEvent.getRetriesTimeoutAboutToStartEvent(), hasItems(startTimeout1, startTimeout2, startTimeout3));

            assertEquals(eventEntity.getWebhookPreSubmit().getUrl(), caseEvent.getCallBackURLAboutToSubmitEvent());
            assertEquals(eventEntity.getWebhookPreSubmit().getTimeouts().size(), caseEvent.getRetriesTimeoutURLAboutToSubmitEvent().size());
            assertThat(caseEvent.getRetriesTimeoutURLAboutToSubmitEvent(), hasItems(preSubmitTimeout1, preSubmitTimeout2, preSubmitTimeout3));

            assertEquals(eventEntity.getWebhookPostSubmit().getUrl(), caseEvent.getCallBackURLSubmittedEvent());
            assertEquals(eventEntity.getWebhookPostSubmit().getTimeouts().size(), caseEvent.getRetriesTimeoutURLSubmittedEvent().size());
            assertThat(caseEvent.getRetriesTimeoutURLSubmittedEvent(), hasItems(postSubmitTimeout1, postSubmitTimeout2, postSubmitTimeout3));

            assertEquals(eventEntity.getSecurityClassification(), caseEvent.getSecurityClassification());
            assertAcls(eventEntity.getEventUserRoles(), caseEvent.getAcls());
            assertEquals(eventEntity.getShowSummary(), caseEvent.getShowSummary());
            assertEquals(eventEntity.getEndButtonLabel(), caseEvent.getEndButtonLabel());

            return caseEvent;

        }

        private WebhookEntity webHook(String url, Integer... retriesTimeouts) {
            WebhookEntity webhookEntity = new WebhookEntity();
            webhookEntity.setUrl(url);
            for (Integer timeout : retriesTimeouts){
                webhookEntity.addTimeout(timeout);
            }
            return webhookEntity;
        }

        private EventUserRoleEntity eventUserRoleEntity(String role,
                                                        Boolean create,
                                                        Boolean read,
                                                        Boolean update,
                                                        Boolean delete) {
            EventUserRoleEntity eventUserRoleEntity = new EventUserRoleEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setRole(role);
            eventUserRoleEntity.setUserRole(userRoleEntity);
            eventUserRoleEntity.setCreate(create);
            eventUserRoleEntity.setRead(read);
            eventUserRoleEntity.setUpdate(update);
            eventUserRoleEntity.setDelete(delete);
            return eventUserRoleEntity;
        }
    }

    @Nested
    @DisplayName("Should create a CaseState matching StateEntity fields")
    class MapStateEntityTests {

        @Test
        public void testMapStateEntity() throws Exception {
            StateEntity stateEntity = new StateEntity();
            stateEntity.setReference("reference");
            stateEntity.setName("name");
            stateEntity.setDescription("description");
            stateEntity.setOrder(69);
            StateUserRoleEntity roleWithCreateOnly = stateUserRoleEntity("role-with-create-only", true, false, false, false);
            StateUserRoleEntity roleWithReadOnly = stateUserRoleEntity("role-with-read-only", false, true, false, false);
            StateUserRoleEntity roleWithUpdateOnly = stateUserRoleEntity("role-with-update-only", false, false, true, false);
            StateUserRoleEntity roleWithDeleteOnly = stateUserRoleEntity("role-with-delete-only", false, false, false, true);
            stateEntity.addStateUserRoles(Arrays.asList(roleWithCreateOnly, roleWithReadOnly, roleWithUpdateOnly, roleWithDeleteOnly));

            CaseState caseState = classUnderTest.map(stateEntity);

            assertEquals(stateEntity.getReference(), caseState.getId());
            assertEquals(stateEntity.getName(), caseState.getName());
            assertEquals(stateEntity.getDescription(), caseState.getDescription());
            assertEquals(stateEntity.getOrder(), caseState.getOrder());
            assertAcls(stateEntity.getStateUserRoles(), caseState.getAcls());
        }

        @Test
        public void testMapEmptyStateEntity() throws Exception {

            CaseState caseState = classUnderTest.map(new StateEntity());

            assertNull(caseState.getId());
            assertNull(caseState.getName());
            assertNull(caseState.getDescription());
            assertNull(caseState.getOrder());
            assertTrue(caseState.getAcls().isEmpty());
        }

        private StateUserRoleEntity stateUserRoleEntity(String role,
                                                        Boolean create,
                                                        Boolean read,
                                                        Boolean update,
                                                        Boolean delete) {
            StateUserRoleEntity eventUserRoleEntity = new StateUserRoleEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setRole(role);
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
        public void testMapCaseFieldEntity() throws Exception {

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

            CaseFieldUserRoleEntity roleWithCreateOnly = caseFieldUserRoleEntity("role-with-create-only", true, false, false, false);
            CaseFieldUserRoleEntity roleWithReadOnly = caseFieldUserRoleEntity("role-with-read-only", false, true, false, false);
            CaseFieldUserRoleEntity roleWithUpdateOnly = caseFieldUserRoleEntity("role-with-update-only", false, false, true, false);
            CaseFieldUserRoleEntity roleWithDeleteOnly = caseFieldUserRoleEntity("role-with-delete-only", false, false, false, true);
            caseFieldEntity.addCaseFieldUserRoles(Arrays.asList(roleWithCreateOnly, roleWithReadOnly, roleWithUpdateOnly, roleWithDeleteOnly));

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

        }

        @Test
        public void testMapEmptyCaseFieldEntity() throws Exception {

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

        private CaseFieldUserRoleEntity caseFieldUserRoleEntity(String role,
                                                        Boolean create,
                                                        Boolean read,
                                                        Boolean update,
                                                        Boolean delete) {
            CaseFieldUserRoleEntity eventUserRoleEntity = new CaseFieldUserRoleEntity();
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setRole(role);
            eventUserRoleEntity.setUserRole(userRoleEntity);
            eventUserRoleEntity.setCreate(create);
            eventUserRoleEntity.setRead(read);
            eventUserRoleEntity.setUpdate(update);
            eventUserRoleEntity.setDelete(delete);
            return eventUserRoleEntity;
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
        public void testMapFieldTypeEntityWithBaseField() {
            FieldTypeEntity fieldTypeEntity = fieldTypeEntity("fieldTypeEntityReference");
            fieldTypeEntity.setBaseFieldType(fieldTypeEntity("baseFieldTypeEntityReference"));
            FieldType fieldType = mapAssertCommonFieldsAndReturn(fieldTypeEntity);
            assertEquals(fieldTypeEntity.getBaseFieldType().getReference(), fieldType.getType());
        }

        @Test
        public void testMapFieldTypeEntityWithoutBaseField() {
            FieldTypeEntity fieldTypeEntity = fieldTypeEntity("fieldTypeEntityReference");
            FieldType fieldType = mapAssertCommonFieldsAndReturn(fieldTypeEntity);
            assertEquals(fieldTypeEntity.getReference(), fieldType.getType());
        }

        @Test
        public void testEmptyMapFieldTypeEntity() {
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
            FieldTypeListItemEntity fieldTypeListItemEntity2 = new FieldTypeListItemEntity();
            FieldTypeListItemEntity fieldTypeListItemEntity3 = new FieldTypeListItemEntity();
            FixedListItem fixedListItem1 = new FixedListItem();
            FixedListItem fixedListItem2 = new FixedListItem();
            FixedListItem fixedListItem3 = new FixedListItem();
            when(spyOnClassUnderTest.map(fieldTypeListItemEntity1)).thenReturn(fixedListItem1);
            when(spyOnClassUnderTest.map(fieldTypeListItemEntity2)).thenReturn(fixedListItem2);
            when(spyOnClassUnderTest.map(fieldTypeListItemEntity3)).thenReturn(fixedListItem3);
            fieldTypeEntity.addListItems(Arrays.asList(fieldTypeListItemEntity1, fieldTypeListItemEntity2, fieldTypeListItemEntity3));

            ComplexFieldEntity complexFieldEntity1 = new ComplexFieldEntity();
            ComplexFieldEntity complexFieldEntity2 = new ComplexFieldEntity();
            ComplexFieldEntity complexFieldEntity3 = new ComplexFieldEntity();
            CaseField complexField1 = new CaseField();
            CaseField complexField2 = new CaseField();
            CaseField complexField3 = new CaseField();
            when(spyOnClassUnderTest.map(complexFieldEntity1)).thenReturn(complexField1);
            when(spyOnClassUnderTest.map(complexFieldEntity2)).thenReturn(complexField2);
            when(spyOnClassUnderTest.map(complexFieldEntity3)).thenReturn(complexField3);
            fieldTypeEntity.addComplexFields(Arrays.asList(complexFieldEntity1, complexFieldEntity2, complexFieldEntity3));

            FieldTypeEntity collectionFieldTypeEntity = fieldTypeEntity("CollectionFieldType");
            FieldType collectionFieldType = new FieldType();
            when(spyOnClassUnderTest.map(collectionFieldTypeEntity)).thenReturn(collectionFieldType);

            FieldType fieldType = spyOnClassUnderTest.map(fieldTypeEntity);

            assertEquals(fieldTypeEntity.getReference(), fieldType.getId());
            assertEquals(fieldTypeEntity.getMinimum(), fieldType.getMin());
            assertEquals(fieldTypeEntity.getMaximum(), fieldType.getMax());
            assertEquals(fieldTypeEntity.getRegularExpression(), fieldType.getRegularExpression());

            assertEquals(fieldTypeEntity.getComplexFields().size(), fieldType.getComplexFields().size());
            assertThat(fieldType.getComplexFields(), hasItems(complexField1, complexField2, complexField3));

            assertEquals(fieldTypeEntity.getListItems().size(), fieldType.getFixedListItems().size());
            assertThat(fieldType.getFixedListItems(), hasItems(fixedListItem1, fixedListItem2, fixedListItem3));

            assertEquals(fieldTypeEntity.getCollectionFieldType(), fieldType.getCollectionFieldType());

            return fieldType;

        }

    }

    @Nested
    @DisplayName("Should create a CaseTypeTab matching DisplayGroupEntity fields")
    class MapDisplayGroupEntityTests {

        @Test
        public void testMapDisplayGroupEntity() {

            DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
            displayGroupEntity.setReference("Reference");
            displayGroupEntity.setLabel("Label");
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
                Arrays.asList(
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

        }

    }

    @Nested
    @DisplayName("Should create a CaseTypeTabField matching DisplayGroupCaseFieldEntity fields")
    class DisplayGroupCaseFieldEntityTests {

        @Test
            public void testMapDisplayGroupCaseFieldEntity() {
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
        public void testMapSearchInputCaseFieldEntity() {
            SearchInputCaseFieldEntity searchInputCaseFieldEntity = new SearchInputCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            searchInputCaseFieldEntity.setCaseField(caseFieldEntity);
            searchInputCaseFieldEntity.setLabel("Label");
            searchInputCaseFieldEntity.setOrder(69);;

            SearchInputField searchInputField = spyOnClassUnderTest.map(searchInputCaseFieldEntity);

            assertEquals(searchInputCaseFieldEntity.getOrder(), searchInputField.getOrder());
            assertEquals(searchInputCaseFieldEntity.getLabel(), searchInputField.getLabel());
            assertEquals(searchInputCaseFieldEntity.getCaseField().getReference(), searchInputField.getCaseFieldId());
        }

    }

    @Nested
    @DisplayName("Should create a SearchInputField matching SearchInputCaseFieldEntity fields")
    class SearchResultCaseFieldEntityTests {

        @Test
        public void testMapSearchResultCaseFieldEntity() {
            SearchResultCaseFieldEntity searchResultCaseFieldEntity = new SearchResultCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            searchResultCaseFieldEntity.setCaseField(caseFieldEntity);
            searchResultCaseFieldEntity.setLabel("Label");
            searchResultCaseFieldEntity.setOrder(69);;

            SearchResultsField searchResultsField = spyOnClassUnderTest.map(searchResultCaseFieldEntity);

            assertEquals(searchResultCaseFieldEntity.getOrder(), searchResultsField.getOrder());
            assertEquals(searchResultCaseFieldEntity.getLabel(), searchResultsField.getLabel());
            assertEquals(searchResultCaseFieldEntity.getCaseField().getReference(), searchResultsField.getCaseFieldId());
        }

    }

    @Nested
    @DisplayName("Should create a WorkBasketInputField matching WorkBasketInputCaseFieldEntity fields")
    class WorkBasketInputCaseFieldEntityTests {

        @Test
        void testMapWorkBasketInputCaseFieldEntity() {
            WorkBasketInputCaseFieldEntity workBasketInputCaseFieldEntity = new WorkBasketInputCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            workBasketInputCaseFieldEntity.setCaseField(caseFieldEntity);
            workBasketInputCaseFieldEntity.setLabel("Label");
            workBasketInputCaseFieldEntity.setOrder(69);

            WorkbasketInputField workbasketInputField = spyOnClassUnderTest.map(workBasketInputCaseFieldEntity);

            assertEquals(workBasketInputCaseFieldEntity.getOrder(), workbasketInputField.getOrder());
            assertEquals(workBasketInputCaseFieldEntity.getLabel(), workbasketInputField.getLabel());
            assertEquals(workBasketInputCaseFieldEntity.getCaseField().getReference(), workbasketInputField.getCaseFieldId());
        }

    }

    @Nested
    @DisplayName("Should create a WorkBasketResult matching WorkBasketCaseFieldEntity fields")
    class WorkBasketCaseFieldEntityTests {

        @Test
        public void testMapWorkBasketCaseFieldEntity() {
            WorkBasketCaseFieldEntity workBasketCaseFieldEntity = new WorkBasketCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            workBasketCaseFieldEntity.setCaseField(caseFieldEntity);
            workBasketCaseFieldEntity.setLabel("Label");
            workBasketCaseFieldEntity.setOrder(69);;

            WorkBasketResultField workBasketResult = spyOnClassUnderTest.map(workBasketCaseFieldEntity);

            assertEquals(workBasketCaseFieldEntity.getOrder(), workBasketResult.getOrder());
            assertEquals(workBasketCaseFieldEntity.getLabel(), workBasketResult.getLabel());
            assertEquals(workBasketCaseFieldEntity.getCaseField().getReference(), workBasketResult.getCaseFieldId());
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

        String role = authorisation.getUserRole().getRole();

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

}
