package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
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
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventPostStateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCasesResultFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SortOrder;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessControlList;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeField;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleField;
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
import uk.gov.hmcts.ccd.definition.store.repository.model.Category;
import uk.gov.hmcts.ccd.definition.store.repository.model.ChallengeQuestion;
import uk.gov.hmcts.ccd.definition.store.repository.model.ComplexACL;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfig;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfiles;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchAliasField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchCasesResultField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchCriteria;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchInputField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchParty;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchResultsField;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketResultField;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkbasketInputField;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

class  EntityToResponseDTOMapperTest {

    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String LIVE_FROM = "2017-02-02";

    private static final String LIVE_TO = "2018-03-03";

    private final EntityToResponseDTOMapper classUnderTest = new EntityToResponseDTOMapperImpl();

    private EntityToResponseDTOMapper spyOnClassUnderTest;

    @BeforeEach
    void setUpSpy() {
        spyOnClassUnderTest = spy(classUnderTest);
    }

    private CaseTypeEntity caseTypeEntity(String reference) {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(reference);
        return caseTypeEntity;
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
            eventCaseFieldEntity.setRetainHiddenValue(true);
            eventCaseFieldEntity.setPublish(true);
            eventCaseFieldEntity.setPublishAs("PublishAs test");
            eventCaseFieldEntity.setDefaultValue("DefaultValue test");
            eventCaseFieldEntity.setNullifyByDefault(true);

            CaseEventField caseEventField = spyOnClassUnderTest.map(
                eventCaseFieldEntity
            );

            assertAll(
                () -> assertEquals(eventCaseFieldEntity.getDisplayContext().name(),
                                   caseEventField.getDisplayContext(),
                                   "displayContext"),
                () -> assertEquals(eventCaseFieldEntity.getShowCondition(),
                                   caseEventField.getShowCondition(),
                                   "showCondition"),
                () -> assertEquals(eventCaseFieldEntity.getShowSummaryChangeOption(),
                                   caseEventField.getShowSummaryChangeOption(),
                                   "showSummaryChangeOption"),
                () -> assertEquals(eventCaseFieldEntity.getShowSummaryContentOption(),
                                   caseEventField.getShowSummaryContentOption(),
                                   "showSummaryContentOption"),
                () -> assertEquals(eventCaseFieldEntity.getRetainHiddenValue(),
                                   caseEventField.getRetainHiddenValue(),
                                   "retainHiddenValue"),
                () -> assertEquals(eventCaseFieldEntity.getPublish(),
                                   caseEventField.getPublish(),
                                   "publish"),
                () -> assertEquals(eventCaseFieldEntity.getPublishAs(),
                                   caseEventField.getPublishAs(),
                                   "publishAs"),
                () -> assertEquals(eventCaseFieldEntity.getDefaultValue(),
                                   caseEventField.getDefaultValue(),
                                   "defaultValue"),
                () -> assertEquals(eventCaseFieldEntity.getNullifyByDefault(),
                    caseEventField.getNullifyByDefault(),
                    "nullifyByDefault")
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
            eventComplexTypeEntity1.setPublish(true);
            eventComplexTypeEntity1.setPublishAs("PublishAs text");
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
                () -> assertEquals(
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getShowCondition(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getShowCondition(),
                    "showCondition"),
                () -> assertEquals(
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getHint(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getHint(),
                    "hint"),
                () -> assertEquals(
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getLabel(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getLabel(),
                    "label"),
                () -> assertEquals(
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getDisplayContext(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getDisplayContext(),
                    "displayContext"),

                () -> assertEquals(
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getDefaultValue(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getDefaultValue(),
                    "DefaultValue1"),

                () -> assertEquals(
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getPublish(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getPublish(),
                    "publish"),

                () -> assertEquals(
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getPublishAs(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getPublishAs(),
                    "publishAs"),

                () -> assertEquals(
                    findEventComplexTypeEntity(eventCaseFieldEntity.getEventComplexTypes(), ref1).getOrder(),
                    findCaseEventFieldComplex(caseEventField.getCaseEventFieldComplex(), ref1).getOrder(),
                    "order")
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
            eventEntity.setPublish(true);

            CaseEvent caseEvent = spyOnClassUnderTest.map(
                eventEntity
            );

            assertAll(
                () -> assertEquals(eventEntity.getShowEventNotes(), caseEvent.getShowEventNotes()),
                () -> assertEquals(eventEntity.getCanSaveDraft(), caseEvent.getCanSaveDraft()),
                () -> assertEquals(eventEntity.getPublish(), caseEvent.getPublish())
            );
        }
    }

    @Nested
    @DisplayName("Should return a CaseEvent which matches the EventEntity")
    class MapEventEnablingCondition {

        @Test
        void shouldMapToCaseEventWithEventEnablingCondition() {
            EventEntity eventEntity = new EventEntity();
            eventEntity.setShowEventNotes(true);
            eventEntity.setCanSaveDraft(true);
            final String validEventEnablingCondition = "FieldA!=\"\" AND FieldB=\"I'm innocent\"";
            eventEntity.setEventEnablingCondition(validEventEnablingCondition);

            CaseEvent caseEvent = spyOnClassUnderTest.map(
                eventEntity
            );

            assertAll(
                () -> assertEquals(eventEntity.getShowEventNotes(), caseEvent.getShowEventNotes()),
                () -> assertEquals(eventEntity.getCanSaveDraft(), caseEvent.getCanSaveDraft()),
                () -> assertEquals(eventEntity.getEventEnablingCondition(), caseEvent.getEventEnablingCondition())
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
            caseFieldEntity1.setId(1);
            CaseFieldEntity caseFieldEntity2 = new CaseFieldEntity();
            caseFieldEntity2.setId(2);
            CaseFieldEntity caseFieldEntity3 = new CaseFieldEntity();
            caseFieldEntity3.setId(3);
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

            CaseTypeACLEntity aclWithCreateOnly = caseTypeACLEntity(
                "acl-with-create-only", true, false,
                false, false);
            CaseTypeACLEntity aclWithReadOnly = caseTypeACLEntity(
                "acl-with-read-only", false, true, false,
                false);
            CaseTypeACLEntity aclWithUpdateOnly = caseTypeACLEntity(
                "acl-with-update-only", false, false,
                true, false);
            CaseTypeACLEntity aclWithDeleteOnly = caseTypeACLEntity(
                "acl-with-delete-only", false, false,
                false, true);

            CaseTypeEntity caseTypeEntity = caseTypeEntity(
                jurisdictionEntity,
                asList(eventEntity1, eventEntity2, eventEntity3),
                asList(stateEntity1, stateEntity2, stateEntity3),
                asList(aclWithCreateOnly, aclWithReadOnly, aclWithUpdateOnly, aclWithDeleteOnly),
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
            assertEquals(caseType.getCallbackGetCaseUrl(), caseTypeEntity.getGetCaseWebhook().getUrl());
            assertEquals(3, caseType.getRetriesGetCaseUrl().size());

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
            assertNull(caseType.getCallbackGetCaseUrl());
            assertNull(caseType.getJurisdiction());

            assertEquals(0, caseType.getEvents().size());
            assertEquals(0, caseType.getStates().size());
            assertEquals(0, caseType.getAcls().size());
            assertEquals(0, caseType.getCaseFields().size());
            assertEquals(0, caseType.getRetriesGetCaseUrl().size());
        }

        private CaseTypeEntity caseTypeEntity(JurisdictionEntity jurisdiction,
                                              List<EventEntity> events,
                                              Collection<StateEntity> states,
                                              List<CaseTypeACLEntity> acls,
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

            WebhookEntity getCaseWebhookEntity = new WebhookEntity();
            getCaseWebhookEntity.setUrl("Get Case URL");
            getCaseWebhookEntity.setTimeouts(asList(3, 5, 7));
            caseTypeEntity.setGetCaseWebhook(getCaseWebhookEntity);

            caseTypeEntity.setJurisdiction(jurisdiction);
            caseTypeEntity.addEvents(events);
            caseTypeEntity.addStates(states);
            caseTypeEntity.addCaseTypeACLEntities(acls);
            caseTypeEntity.addCaseFields(caseFieldEntities);
            caseTypeEntity.addSearchAliasFields(searchAliasFieldEntities);

            return caseTypeEntity;
        }

        private CaseTypeACLEntity caseTypeACLEntity(String reference,
                                                    Boolean create,
                                                    Boolean read,
                                                    Boolean update,
                                                    Boolean delete) {
            CaseTypeACLEntity caseTypeACLEntity = new CaseTypeACLEntity();
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference(reference);
            caseTypeACLEntity.setAccessProfile(accessProfileEntity);
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
            eventEntity1.setId(1);
            EventLiteEntity eventEntity2 = new EventLiteEntity();
            eventEntity2.setId(2);
            EventLiteEntity eventEntity3 = new EventLiteEntity();
            eventEntity3.setId(3);
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
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            caseTypeLiteACLEntity.setAccessProfile(accessProfileEntity);

            EventLiteACLEntity eventLiteACLEntity = new EventLiteACLEntity();
            eventLiteACLEntity.setAccessProfile(accessProfileEntity);
            EventLiteACLEntity eventLiteACLEntity2 = new EventLiteACLEntity();
            eventLiteACLEntity2.setAccessProfile(accessProfileEntity);
            eventEntity1.addEventACL(eventLiteACLEntity);
            eventEntity2.addEventACL(eventLiteACLEntity);
            eventEntity2.addEventACL(eventLiteACLEntity2);
            eventEntity3.addEventACL(eventLiteACLEntity);

            StateACLEntity stateACLEntity1 = new StateACLEntity();
            stateACLEntity1.setId(1);
            StateACLEntity stateACLEntity2 = new StateACLEntity();
            stateACLEntity2.setId(2);
            stateACLEntity1.setAccessProfile(accessProfileEntity);
            stateACLEntity2.setAccessProfile(accessProfileEntity);
            stateEntity1.addStateACLEntities(asList(stateACLEntity1, stateACLEntity2));
            stateEntity2.addStateACL(stateACLEntity1);
            stateEntity3.addStateACLEntities(asList(stateACLEntity1, stateACLEntity2));

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
                assertEquals(parameters.getPostStates().size(), caseEvent.getPostStates().size());
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
            assertEquals(0, caseEvent.getPostStates().size());

        }

        private List<Parameters> createParameters() {
            return asList(
                new Parameters(
                    false, Collections.emptyList(), null,
                    Collections.singletonList("*"), "*"
                ),
                new Parameters(
                    false, Collections.emptyList(),
                    asList(eventPostStateEntity("PostState", 1)),
                    Collections.singletonList("*"), "PostState"
                ),
                new Parameters(
                    false, asList(stateEntity("preState1"), stateEntity("preState2"),
                    stateEntity("preState3")),
                    null,
                    asList("preState1", "preState2", "preState3"), "*"
                ),
                new Parameters(
                    false, asList(stateEntity("preState1"), stateEntity("preState2"),
                    stateEntity("preState3")),
                    asList(eventPostStateEntity("PostState", 1)),
                    asList("preState1", "preState2", "preState3"), "PostState"
                ),
                new Parameters(
                    true, Collections.emptyList(), null,
                    Collections.emptyList(), "*"
                ),
                new Parameters(
                    true, Collections.emptyList(),
                    asList(eventPostStateEntity("PostState", 1)),
                    Collections.emptyList(), "PostState"
                ),
                new Parameters(
                    true, asList(stateEntity("preState1"), stateEntity("preState2"),
                    stateEntity("preState3")),
                    null,
                    Collections.emptyList(), "*"
                ),
                new Parameters(
                    true, asList(stateEntity("preState1"), stateEntity("preState2"),
                    stateEntity("preState3")),
                    asList(eventPostStateEntity("PostState", 1)),
                    Collections.emptyList(), "PostState"
                )
            );
        }

        private StateEntity stateEntity(String reference) {
            StateEntity stateEntity = new StateEntity();
            stateEntity.setReference(reference);
            return stateEntity;
        }

        private EventPostStateEntity eventPostStateEntity(String reference,
                                                          int priority) {
            EventPostStateEntity eventPostStateEntity = new EventPostStateEntity();
            eventPostStateEntity.setPostStateReference(reference);
            eventPostStateEntity.setPriority(priority);
            return eventPostStateEntity;
        }

        private class Parameters {

            private final Boolean canCreate;
            private final List<StateEntity> preStates;
            private final List<EventPostStateEntity> postStates;
            private final List<String> preStateExpectation;
            private final String postStateExpectation;

            Parameters(Boolean canCreate,
                       List<StateEntity> preStates,
                       List<EventPostStateEntity> postStates,
                       List<String> preStateExpectation,
                       String postStateExpectation) {
                this.canCreate = canCreate;
                this.preStates = preStates;
                this.postStates = postStates == null ? new ArrayList<>() : postStates;
                this.preStateExpectation = preStateExpectation;
                this.postStateExpectation = postStateExpectation;
            }

            private Boolean getCanCreate() {
                return canCreate;
            }

            private List<StateEntity> getPreStates() {
                return preStates;
            }

            private List<EventPostStateEntity> getPostStates() {
                return postStates;
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

            EventACLEntity aclWithCreateOnly = eventACLEntity("acl-with-create-only", true, false, false, false);
            EventACLEntity aclWithReadOnly = eventACLEntity("acl-with-read-only", false, true, false, false);
            EventACLEntity aclWithUpdateOnly = eventACLEntity("acl-with-update-only", false, false, true, false);
            EventACLEntity aclWithDeleteOnly = eventACLEntity("acl-with-delete-only", false, false, false, true);
            eventEntity.addEventACLEntities(
                asList(aclWithCreateOnly, aclWithReadOnly, aclWithUpdateOnly, aclWithDeleteOnly));

            eventEntity.setCanCreate(parameters.getCanCreate());
            parameters.getPreStates().forEach(eventEntity::addPreState);
            eventEntity.addEventPostStates(parameters.getPostStates());

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

        private EventACLEntity eventACLEntity(String reference,
                                              Boolean create,
                                              Boolean read,
                                              Boolean update,
                                              Boolean delete) {
            EventACLEntity eventACLEntity = new EventACLEntity();
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference(reference);
            eventACLEntity.setAccessProfile(accessProfileEntity);
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
            StateACLEntity aclWithCreateOnly = stateACLEntity("acl-with-create-only", true, false, false, false);
            StateACLEntity aclWithReadOnly = stateACLEntity("acl-with-read-only", false, true, false, false);
            StateACLEntity aclWithUpdateOnly = stateACLEntity("acl-with-update-only", false, false, true, false);
            StateACLEntity aclWithDeleteOnly = stateACLEntity("acl-with-delete-only", false, false, false, true);
            stateEntity.addStateACLEntities(
                asList(aclWithCreateOnly, aclWithReadOnly, aclWithUpdateOnly, aclWithDeleteOnly));

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

        private StateACLEntity stateACLEntity(String reference,
                                             Boolean create,
                                             Boolean read,
                                             Boolean update,
                                             Boolean delete) {
            StateACLEntity stateACLEntity = new StateACLEntity();
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference(reference);
            stateACLEntity.setAccessProfile(accessProfileEntity);
            stateACLEntity.setCreate(create);
            stateACLEntity.setRead(read);
            stateACLEntity.setUpdate(update);
            stateACLEntity.setDelete(delete);
            return stateACLEntity;
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

            CaseFieldACLEntity aclWithCreateOnly = caseFieldACLEntity(
                "acl-with-create-only", true, false, false, false);
            CaseFieldACLEntity aclWithReadOnly = caseFieldACLEntity(
                "acl-with-read-only", false, true, false, false);
            CaseFieldACLEntity aclWithUpdateOnly = caseFieldACLEntity(
                "acl-with-update-only", false, false, true, false);
            CaseFieldACLEntity aclWithDeleteOnly = caseFieldACLEntity(
                "acl-with-delete-only", false, false, false, true);
            caseFieldEntity.addCaseACLEntities(
                asList(aclWithCreateOnly, aclWithReadOnly, aclWithUpdateOnly, aclWithDeleteOnly));

            ComplexFieldACLEntity complexACLWithCreateOnly = complexFieldACLEntity("list.element.code",
                "acl-with-create-only", true, false, false, false);
            ComplexFieldACLEntity complexACLWithReadOnly = complexFieldACLEntity("list.element.code.item1",
                "acl-with-create-only", true, false, false, false);
            ComplexFieldACLEntity complexACLWithUpdateOnly = complexFieldACLEntity("list.element.code.item2",
                "acl-with-create-only", true, false, false, false);
            ComplexFieldACLEntity complexACLWithDeleteOnly = complexFieldACLEntity("list.element.code.item3",
                "acl-with-create-only", true, false, false, false);
            ComplexFieldACLEntity complexACLWithAllCRUD = complexFieldACLEntity("list.another.element.code",
                "acl-with-create-only", true, true, true, true);
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

        private CaseFieldACLEntity caseFieldACLEntity(String reference,
                                                      Boolean create,
                                                      Boolean read,
                                                      Boolean update,
                                                      Boolean delete) {
            CaseFieldACLEntity caseFieldACLEntity = new CaseFieldACLEntity();
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference(reference);
            caseFieldACLEntity.setAccessProfile(accessProfileEntity);
            caseFieldACLEntity.setCreate(create);
            caseFieldACLEntity.setRead(read);
            caseFieldACLEntity.setUpdate(update);
            caseFieldACLEntity.setDelete(delete);
            return caseFieldACLEntity;
        }

        private ComplexFieldACLEntity complexFieldACLEntity(String code, String reference,
                                                            Boolean create,
                                                            Boolean read,
                                                            Boolean update,
                                                            Boolean delete) {
            ComplexFieldACLEntity complexFieldACLEntity = new ComplexFieldACLEntity();
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference(reference);
            complexFieldACLEntity.setAccessProfile(accessProfileEntity);
            complexFieldACLEntity.setCreate(create);
            complexFieldACLEntity.setRead(read);
            complexFieldACLEntity.setUpdate(update);
            complexFieldACLEntity.setDelete(delete);
            complexFieldACLEntity.setListElementCode(code);
            return complexFieldACLEntity;
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
                asList(fieldTypeListItemEntity1, fieldTypeListItemEntity4, fieldTypeListItemEntity5,
                    fieldTypeListItemEntity2, fieldTypeListItemEntity3));

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

            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference("accessProfile1");
            accessProfileEntity.setName("Access Profile 1");
            DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
            displayGroupEntity.setReference("Reference");
            displayGroupEntity.setLabel("Label");
            displayGroupEntity.setAccessProfile(accessProfileEntity);
            displayGroupEntity.setOrder(69);

            DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity1 = new DisplayGroupCaseFieldEntity();
            displayGroupCaseFieldEntity1.setId(1);
            DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity2 = new DisplayGroupCaseFieldEntity();
            displayGroupCaseFieldEntity2.setId(2);
            DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity3 = new DisplayGroupCaseFieldEntity();
            displayGroupCaseFieldEntity3.setId(3);

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
            assertEquals(caseTypeTab.getRole(), displayGroupEntity.getAccessProfile().getReference());

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
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference("accessProfile1");
            SearchInputCaseFieldEntity searchInputCaseFieldEntity = new SearchInputCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            searchInputCaseFieldEntity.setCaseField(caseFieldEntity);
            searchInputCaseFieldEntity.setLabel("Label");
            searchInputCaseFieldEntity.setOrder(69);
            searchInputCaseFieldEntity.setAccessProfile(accessProfileEntity);
            searchInputCaseFieldEntity.setCaseFieldElementPath("Field1.Field2");
            searchInputCaseFieldEntity.setShowCondition("aShowCondition");
            searchInputCaseFieldEntity.setDisplayContextParameter("DisplayContextParameter");

            SearchInputField searchInputField = spyOnClassUnderTest.map(searchInputCaseFieldEntity);

            assertEquals(searchInputCaseFieldEntity.getOrder(), searchInputField.getOrder());
            assertEquals(searchInputCaseFieldEntity.getLabel(), searchInputField.getLabel());
            assertEquals(
                searchInputCaseFieldEntity.getCaseFieldElementPath(), searchInputField.getCaseFieldElementPath());
            assertEquals(searchInputCaseFieldEntity.getShowCondition(), searchInputField.getShowCondition());
            assertEquals(searchInputCaseFieldEntity.getCaseField().getReference(), searchInputField.getCaseFieldId());
            assertEquals(
                searchInputCaseFieldEntity.getDisplayContextParameter(), searchInputField.getDisplayContextParameter());
            assertEquals(accessProfileEntity.getReference(), searchInputField.getRole());
        }

    }

    @Nested
    @DisplayName("Should create a SearchResultField matching SearchResultCaseFieldEntity fields")
    class SearchResultCaseFieldEntityTests {

        @Test
        void testMapSearchResultCaseFieldEntity() {
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference("accessProfile1");
            SearchResultCaseFieldEntity searchResultCaseFieldEntity = new SearchResultCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            searchResultCaseFieldEntity.setCaseField(caseFieldEntity);
            searchResultCaseFieldEntity.setCaseFieldElementPath("SomePath");
            searchResultCaseFieldEntity.setLabel("Label");
            searchResultCaseFieldEntity.setOrder(69);
            searchResultCaseFieldEntity.setAccessProfile(accessProfileEntity);
            SortOrder sortOrder = new SortOrder(2, "ASC");
            searchResultCaseFieldEntity.setSortOrder(sortOrder);
            searchResultCaseFieldEntity.setDisplayContextParameter("DisplayContextParameter");
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference("Case_Type_1");
            searchResultCaseFieldEntity.setCaseType(caseTypeEntity);

            SearchResultsField searchResultsField = spyOnClassUnderTest.map(searchResultCaseFieldEntity);

            assertEquals(searchResultCaseFieldEntity.getOrder(), searchResultsField.getOrder());
            assertEquals(
                searchResultCaseFieldEntity.getCaseFieldElementPath(), searchResultsField.getCaseFieldElementPath());
            assertEquals(searchResultCaseFieldEntity.getLabel(), searchResultsField.getLabel());
            assertEquals(searchResultCaseFieldEntity.getCaseField().getReference(),
                searchResultsField.getCaseFieldId());
            assertEquals(searchResultCaseFieldEntity.getDisplayContextParameter(),
                searchResultsField.getDisplayContextParameter());
            assertThat(searchResultsField.isMetadata(), is(false));
            assertEquals(accessProfileEntity.getReference(), searchResultsField.getRole());

            assertEquals(sortOrder.getDirection(), searchResultsField.getSortOrder().getDirection());
            assertEquals(sortOrder.getPriority(), searchResultsField.getSortOrder().getPriority());
            assertEquals(caseTypeEntity.getReference(), searchResultsField.getCaseTypeId());
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
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference("accessProfile1");
            WorkBasketInputCaseFieldEntity workBasketInputCaseFieldEntity = new WorkBasketInputCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            workBasketInputCaseFieldEntity.setCaseField(caseFieldEntity);
            workBasketInputCaseFieldEntity.setLabel("Label");
            workBasketInputCaseFieldEntity.setOrder(69);
            workBasketInputCaseFieldEntity.setAccessProfile(accessProfileEntity);
            workBasketInputCaseFieldEntity.setCaseFieldElementPath("Field1.Field2");
            workBasketInputCaseFieldEntity.setShowCondition("aShowCondition");
            workBasketInputCaseFieldEntity.setDisplayContextParameter("DisplayContextParameter");

            WorkbasketInputField workbasketInputField = spyOnClassUnderTest.map(workBasketInputCaseFieldEntity);

            assertEquals(workBasketInputCaseFieldEntity.getShowCondition(), workbasketInputField.getShowCondition());
            assertEquals(workBasketInputCaseFieldEntity.getOrder(), workbasketInputField.getOrder());
            assertEquals(workBasketInputCaseFieldEntity.getLabel(), workbasketInputField.getLabel());
            assertEquals(workBasketInputCaseFieldEntity.getCaseFieldElementPath(),
                workbasketInputField.getCaseFieldElementPath());
            assertEquals(workBasketInputCaseFieldEntity.getCaseField().getReference(),
                workbasketInputField.getCaseFieldId());
            assertEquals(workBasketInputCaseFieldEntity.getDisplayContextParameter(),
                workbasketInputField.getDisplayContextParameter());
            assertEquals(accessProfileEntity.getReference(), workbasketInputField.getRole());
        }

    }

    @Nested
    @DisplayName("Should create a WorkBasketResult matching WorkBasketCaseFieldEntity fields")
    class WorkBasketCaseFieldEntityTests {

        @Test
        void testMapWorkBasketCaseFieldEntity() {
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference("accessProfile1");
            WorkBasketCaseFieldEntity workBasketCaseFieldEntity = new WorkBasketCaseFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            workBasketCaseFieldEntity.setCaseField(caseFieldEntity);
            workBasketCaseFieldEntity.setCaseFieldElementPath("SomePath");
            workBasketCaseFieldEntity.setLabel("Label");
            workBasketCaseFieldEntity.setOrder(69);
            workBasketCaseFieldEntity.setAccessProfile(accessProfileEntity);
            SortOrder sortOrder = new SortOrder(2, "ASC");
            workBasketCaseFieldEntity.setSortOrder(sortOrder);
            workBasketCaseFieldEntity.setDisplayContextParameter("DisplayContextParameter");
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference("Case_Type_1");
            workBasketCaseFieldEntity.setCaseType(caseTypeEntity);

            WorkBasketResultField workBasketResult = spyOnClassUnderTest.map(workBasketCaseFieldEntity);

            assertEquals(workBasketCaseFieldEntity.getOrder(), workBasketResult.getOrder());
            assertEquals(
                workBasketCaseFieldEntity.getCaseFieldElementPath(), workBasketResult.getCaseFieldElementPath());
            assertEquals(workBasketCaseFieldEntity.getLabel(), workBasketResult.getLabel());
            assertEquals(workBasketCaseFieldEntity.getCaseField().getReference(), workBasketResult.getCaseFieldId());
            assertEquals(workBasketCaseFieldEntity.getDisplayContextParameter(),
                workBasketResult.getDisplayContextParameter());
            assertEquals(accessProfileEntity.getReference(), workBasketResult.getRole());

            assertEquals(sortOrder.getDirection(), workBasketResult.getSortOrder().getDirection());
            assertEquals(sortOrder.getPriority(), workBasketResult.getSortOrder().getPriority());
            assertEquals(caseTypeEntity.getReference(), workBasketResult.getCaseTypeId());
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
            AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
            accessProfileEntity.setReference("accessProfile1");
            SearchCasesResultFieldEntity searchCasesResultFieldEntity = new SearchCasesResultFieldEntity();
            CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
            caseFieldEntity.setReference("CaseFieldReference");
            searchCasesResultFieldEntity.setCaseField(caseFieldEntity);
            searchCasesResultFieldEntity.setCaseFieldElementPath("SomePath");
            searchCasesResultFieldEntity.setLabel("Label");
            searchCasesResultFieldEntity.setOrder(69);
            searchCasesResultFieldEntity.setAccessProfile(accessProfileEntity);
            SortOrder sortOrder = new SortOrder(2, "ASC");
            searchCasesResultFieldEntity.setSortOrder(sortOrder);
            searchCasesResultFieldEntity.setDisplayContextParameter("DisplayContextParameter");
            searchCasesResultFieldEntity.setUseCase("orgCase");
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference("Case_Type_1");
            searchCasesResultFieldEntity.setCaseType(caseTypeEntity);


            SearchCasesResultField searchCasesResultField = spyOnClassUnderTest.map(searchCasesResultFieldEntity);

            assertEquals(searchCasesResultFieldEntity.getOrder(), searchCasesResultField.getOrder());
            assertEquals(searchCasesResultFieldEntity.getCaseFieldElementPath(),
                searchCasesResultField.getCaseFieldElementPath());
            assertEquals(searchCasesResultFieldEntity.getLabel(), searchCasesResultField.getLabel());
            assertEquals(searchCasesResultFieldEntity.getCaseField().getReference(),
                searchCasesResultField.getCaseFieldId());
            assertEquals(searchCasesResultFieldEntity.getDisplayContextParameter(),
                searchCasesResultField.getDisplayContextParameter());
            assertEquals(searchCasesResultFieldEntity.getUseCase(), searchCasesResultField.getUseCase());
            assertEquals(accessProfileEntity.getReference(), searchCasesResultField.getRole());

            assertEquals(sortOrder.getDirection(), searchCasesResultField.getSortOrder().getDirection());
            assertEquals(sortOrder.getPriority(), searchCasesResultField.getSortOrder().getPriority());
            assertEquals(caseTypeEntity.getReference(), searchCasesResultField.getCaseTypeId());

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

    @SuppressWarnings("checkstyle:LineLength")
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

    @Nested
    @DisplayName("Should return a ChallengeQuestion model whose fields match those in the ChallengeQuestionEntity")
    class MapChallengeQuestionEntityTests {

        @Test
        void testMapChallengeQuestionEntity() {
            ChallengeQuestionTabEntity challengeQuestionEntity = new ChallengeQuestionTabEntity();
            challengeQuestionEntity.setAnswerField("Answer Field");
            challengeQuestionEntity.setAnswerFieldType(fieldTypeEntity("FieldTypeReference"));
            challengeQuestionEntity.setCaseType(caseTypeEntity("Reference"));
            challengeQuestionEntity.setChallengeQuestionId("ChallengeQuestionId");
            challengeQuestionEntity.setDisplayContextParameter("DisplayContextParameter");
            challengeQuestionEntity.setId(1);
            challengeQuestionEntity.setOrder(2);
            challengeQuestionEntity.setQuestionId("QuestionId");
            challengeQuestionEntity.setQuestionText("QuestionText");
            challengeQuestionEntity.setIgnoreNullFields(false);

            ChallengeQuestion challengeQuestion = classUnderTest.map(challengeQuestionEntity);

            assertEquals(challengeQuestion.getAnswerField(), challengeQuestionEntity.getAnswerField());
            assertEquals(challengeQuestion.getAnswerFieldType().getId(),
                challengeQuestionEntity.getAnswerFieldType().getReference());
            assertEquals(challengeQuestion.getCaseTypeId(), challengeQuestionEntity.getCaseType().getReference());
            assertEquals(challengeQuestion.getChallengeQuestionId(), challengeQuestionEntity.getChallengeQuestionId());
            assertEquals(challengeQuestion.getDisplayContextParameter(),
                challengeQuestionEntity.getDisplayContextParameter());
            assertEquals(challengeQuestion.getOrder(), challengeQuestionEntity.getOrder());
            assertEquals(challengeQuestion.getQuestionId(), challengeQuestionEntity.getQuestionId());
            assertEquals(challengeQuestion.getQuestionText(), challengeQuestionEntity.getQuestionText());
            assertEquals(challengeQuestion.isIgnoreNullFields(), challengeQuestionEntity.isIgnoreNullFields());
        }

        @Test
        void testMapEmptyChallengeQuestionEntity() {
            ChallengeQuestionTabEntity challengeQuestionEntity = new ChallengeQuestionTabEntity();

            ChallengeQuestion challengeQuestion = classUnderTest.map(challengeQuestionEntity);

            assertNull(challengeQuestion.getAnswerField());
            assertNull(challengeQuestion.getAnswerFieldType());
            assertNull(challengeQuestion.getCaseTypeId());
            assertNull(challengeQuestion.getChallengeQuestionId());
            assertNull(challengeQuestion.getDisplayContextParameter());
            assertNull(challengeQuestion.getOrder());
            assertNull(challengeQuestion.getQuestionId());
            assertFalse(challengeQuestion.isIgnoreNullFields());
            assertNull(challengeQuestion.getQuestionText());
        }

        private FieldTypeEntity fieldTypeEntity(String reference) {
            FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
            fieldTypeEntity.setReference(reference);
            return fieldTypeEntity;
        }

        private CaseTypeLiteEntity caseTypeEntity(String reference) {
            CaseTypeLiteEntity caseTypeEntity = new CaseTypeLiteEntity();
            caseTypeEntity.setReference(reference);
            return caseTypeEntity;
        }
    }

    @Nested
    @DisplayName("Should return RoleToAccessProfile model whose fields match those in the RoleToAccessProfileEntity")
    class MapRoleToAccessProfilesTests {

        @Test
        void testMapRoleToAccessProfileEntity() {
            final Date liveFrom = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
            final Date liveTo = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

            RoleToAccessProfilesEntity roleToAccessProfilesEntity = new RoleToAccessProfilesEntity();
            roleToAccessProfilesEntity.setCaseType(caseTypeEntity("CaseTypeReference"));
            roleToAccessProfilesEntity.setRoleName("judge");
            roleToAccessProfilesEntity.setLiveFrom(liveFrom);
            roleToAccessProfilesEntity.setLiveTo(liveTo);
            roleToAccessProfilesEntity.setReadOnly(true);
            roleToAccessProfilesEntity.setDisabled(true);
            roleToAccessProfilesEntity.setAuthorisation("auth1,auth2");
            roleToAccessProfilesEntity.setAccessProfiles("caseworker-befta_master,caseworker-befta_master-solicitor");
            roleToAccessProfilesEntity.setCaseAccessCategories("Cat1,Cat2");

            final RoleToAccessProfiles actualRoleToAccessProfiles = classUnderTest.map(roleToAccessProfilesEntity);

            Assertions.assertThat(actualRoleToAccessProfiles)
                .isNotNull()
                .satisfies(actual -> {
                    Assertions.assertThat(actual.getCaseTypeId()).isEqualTo("CaseTypeReference");
                    Assertions.assertThat(actual.getRoleName()).isEqualTo("judge");
                    Assertions.assertThat(actual.getLiveFrom()).isEqualTo(liveFrom);
                    Assertions.assertThat(actual.getLiveTo()).isEqualTo(liveTo);
                    Assertions.assertThat(actual.getReadOnly()).isTrue();
                    Assertions.assertThat(actual.getDisabled()).isTrue();
                    Assertions.assertThat(actual.getAuthorisations()).isEqualTo("auth1,auth2");
                    Assertions.assertThat(actual.getAccessProfiles())
                        .isEqualTo("caseworker-befta_master,caseworker-befta_master-solicitor");
                    Assertions.assertThat(actual.getCaseAccessCategories()).isEqualTo("Cat1,Cat2");
                });
        }

        @Test
        void testMapEmptyRoleToAccessProfileEntity() {
            final RoleToAccessProfiles expectedRoleToAccessProfiles = new RoleToAccessProfiles();

            final RoleToAccessProfilesEntity roleToAccessProfilesEntity = new RoleToAccessProfilesEntity();

            final RoleToAccessProfiles actualRoleToAccessProfiles = classUnderTest.map(roleToAccessProfilesEntity);

            Assertions.assertThat(actualRoleToAccessProfiles)
                .isNotNull()
                .isEqualTo(expectedRoleToAccessProfiles);
        }
    }


    @Nested
    @DisplayName("Should return SearchCriteria model whose fields match those in the SearchCriteriaEntity")
    class MapSearchCriteriaTests {

        @Test
        void testMapSearchCriteriaEntity() {
            SearchCriteriaEntity searchCriteriaEntity = new SearchCriteriaEntity();
            searchCriteriaEntity.setCaseType(caseTypeEntity("CaseTypeReference"));
            searchCriteriaEntity.setOtherCaseReference("OtherCaseReference");
            searchCriteriaEntity.setLiveFrom(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
            searchCriteriaEntity.setLiveTo(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));

            SearchCriteria searchCriteria = classUnderTest.map(searchCriteriaEntity);

            assertEquals(searchCriteria.getCaseTypeId(), searchCriteriaEntity.getCaseType().getReference());
            assertEquals(searchCriteria.getOtherCaseReference(), searchCriteriaEntity.getOtherCaseReference());
            assertEquals(searchCriteria.getLiveTo(), searchCriteriaEntity.getLiveTo());
            assertEquals(searchCriteria.getLiveFrom(), searchCriteriaEntity.getLiveFrom());
        }

        @Test
        void testMapEmptyRoleToAccessProfileEntity() {
            SearchCriteriaEntity searchCriteriaEntity = new SearchCriteriaEntity();

            SearchCriteria searchCriteria = classUnderTest.map(searchCriteriaEntity);

            assertNull(searchCriteria.getCaseTypeId());
            assertNull(searchCriteria.getLiveFrom());
            assertNull(searchCriteria.getLiveTo());
            assertNull(searchCriteria.getOtherCaseReference());
        }

    }


    @Nested
    @DisplayName("Should return SearchParty model whose fields match those in the SearchPartyEntity")
    class MapSearchPartyTests {


        @Test
        void testMapSearchPartyEntity() {
            SearchPartyEntity searchPartyEntity = new SearchPartyEntity();
            searchPartyEntity.setCaseType(caseTypeEntity("CaseTypeReference"));


            SearchParty searchParty = classUnderTest.map(searchPartyEntity);

            assertEquals(searchParty.getCaseTypeId(), searchPartyEntity.getCaseType().getReference());
            assertEquals(searchParty.getSearchPartyName(), searchPartyEntity.getSearchPartyName());
            assertEquals(searchParty.getSearchPartyAddressLine1(), searchPartyEntity.getSearchPartyAddressLine1());
            assertEquals(searchParty.getSearchPartyEmailAddress(), searchPartyEntity.getSearchPartyEmailAddress());
            assertEquals(searchParty.getSearchPartyPostCode(), searchPartyEntity.getSearchPartyPostCode());
            assertEquals(searchParty.getSearchPartyDob(), searchPartyEntity.getSearchPartyDob());
            assertEquals(searchParty.getSearchPartyDod(), searchPartyEntity.getSearchPartyDod());
            assertEquals(searchParty.getLiveTo(), searchPartyEntity.getLiveTo());
            assertEquals(searchParty.getLiveFrom(), searchPartyEntity.getLiveFrom());
            assertEquals(searchParty.getSearchPartyCollectionFieldName(),
                searchPartyEntity.getSearchPartyCollectionFieldName());

        }

        @Test
        void testMapEmptySearchPartyEntity() {
            SearchPartyEntity searchPartyEntity = new SearchPartyEntity();

            SearchParty searchParty = classUnderTest.map(searchPartyEntity);

            assertNull(searchParty.getCaseTypeId());
            assertNull(searchParty.getLiveFrom());
            assertNull(searchParty.getLiveTo());
            assertNull(searchParty.getSearchPartyName());
            assertNull(searchParty.getSearchPartyAddressLine1());
            assertNull(searchParty.getSearchPartyEmailAddress());
            assertNull(searchParty.getSearchPartyPostCode());
            assertNull(searchParty.getSearchPartyDob());
            assertNull(searchParty.getSearchPartyDod());
            assertNull(searchParty.getSearchPartyCollectionFieldName());
        }

    }

    @Nested
    @DisplayName("Should return Category model whose fields match those in the CategoryEntity")
    class MapCategoryTests {


        @Test
        void testMapCategoryEntity() {
            CaseTypeLiteEntity caseTypeLiteEntity = new CaseTypeLiteEntity();
            caseTypeLiteEntity.setVersion(100);
            caseTypeLiteEntity.setReference("CaseTypeReference");
            caseTypeLiteEntity.setName("Name");
            caseTypeLiteEntity.addState(new StateEntity());

            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setCategoryId("Cat1,Cat2");
            categoryEntity.setCategoryLabel("CategoryLabel");
            categoryEntity.setParentCategoryId("ParentCat1");
            categoryEntity.setLiveFrom(LocalDate.parse(LIVE_FROM));
            categoryEntity.setLiveTo(LocalDate.parse(LIVE_TO));
            categoryEntity.setDisplayOrder(1);
            categoryEntity.setCaseType(caseTypeLiteEntity);


            Category category = classUnderTest.map(categoryEntity);

            assertEquals(categoryEntity.getCategoryId(), category.getCategoryId());
            assertEquals(categoryEntity.getCategoryLabel(), category.getCategoryLabel());
            assertEquals(categoryEntity.getParentCategoryId(), category.getParentCategoryId());
            assertEquals(categoryEntity.getLiveFrom(), category.getLiveFrom());
            assertEquals(categoryEntity.getLiveTo(), category.getLiveTo());
            assertEquals(categoryEntity.getDisplayOrder(), category.getDisplayOrder());
            assertEquals(categoryEntity.getCaseType().getReference(), category.getCaseTypeId());

        }

        @Test
        void testMapEmptyCategoryEntity() {
            CategoryEntity categoryEntity = new CategoryEntity();

            Category category = classUnderTest.map(categoryEntity);

            assertNull(category.getCategoryId());
            assertNull(category.getCategoryLabel());
            assertNull(category.getParentCategoryId());
            assertNull(category.getLiveFrom());
            assertNull(category.getLiveTo());
            assertNull(category.getDisplayOrder());
            assertNull(category.getCaseTypeId());
        }

    }

    private void assertComplexACLs(List<ComplexFieldACLEntity> authorisation, List<ComplexACL> accessControlList) {
        for (ComplexFieldACLEntity authItem : authorisation) {
            assertThat(accessControlList, hasItem(aclWhichMatchesComplexFieldACL(authItem)));
        }
    }

    private void assertAcls(Collection<? extends Authorisation> authorisation,
                            List<AccessControlList> accessControlList) {
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

        String accessProfile = authorisation.getAccessProfile().getReference();

        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof AccessControlList
                    && ((AccessControlList) o).getRole().equals(accessProfile)
                    && ((AccessControlList) o).getCreate().equals(authorisation.getCreate())
                    && ((AccessControlList) o).getRead().equals(authorisation.getRead())
                    && ((AccessControlList) o).getUpdate().equals(authorisation.getUpdate())
                    && ((AccessControlList) o).getDelete().equals(authorisation.getDelete());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                    String.format(
                        "an AccessControlList with AccessProfile %s, create %s, read %s, update %s, delete %s",
                        accessProfile,
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

        String accessProfile = aclEntity.getAccessProfile().getReference();

        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof ComplexACL
                    && ((ComplexACL) o).getRole().equals(accessProfile)
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
                        "an AccessControlList with accessProfile %s, create %s, read %s, "
                            + "update %s, delete %s, listElementCode %s",
                        accessProfile,
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

    @Nested
    @DisplayName("Should return `a AccessType which matches the AccessTypeEntity")
    class MapAccessTypeEntityTests {

        @Test
        void testMapAccessTypeRolesEntity() {

            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
            jurisdictionEntity.setName("name");

            CaseTypeEntity caseType = new CaseTypeEntity();
            caseType.setReference("id");
            caseType.setName("Test case");
            caseType.setVersion(1);
            caseType.setDescription("Some case type");
            caseType.setJurisdiction(jurisdictionEntity);
            caseType.setSecurityClassification(SecurityClassification.PUBLIC);

            AccessTypeEntity accessTypeEntity = new AccessTypeEntity();
            accessTypeEntity.setLiveFrom(LocalDate.of(2023, Month.FEBRUARY, 12));
            accessTypeEntity.setLiveTo(LocalDate.of(2027, Month.OCTOBER, 17));
            accessTypeEntity.setCaseType(toCaseTypeLiteEntity(caseType));
            accessTypeEntity.setAccessTypeId("some access type id");
            accessTypeEntity.setOrganisationProfileId("some org profile id");
            accessTypeEntity.setAccessMandatory(true);
            accessTypeEntity.setAccessDefault(true);
            accessTypeEntity.setDisplay(true);
            accessTypeEntity.setDescription("some description");
            accessTypeEntity.setHint("some hint");
            accessTypeEntity.setDisplayOrder(1);

            AccessTypeField accessTypeField = classUnderTest.map(accessTypeEntity);

            assertEquals(accessTypeEntity.getId(), accessTypeField.getId());
            assertEquals(accessTypeEntity.getLiveFrom(), accessTypeField.getLiveFrom());
            assertEquals(accessTypeEntity.getLiveTo(), accessTypeField.getLiveTo());
            assertEquals(accessTypeEntity.getCaseType().getReference(),
                accessTypeField.getCaseTypeId());
            assertEquals(accessTypeEntity.getAccessTypeId(), accessTypeField.getAccessTypeId());
            assertEquals(accessTypeEntity.getOrganisationProfileId(),
                accessTypeField.getOrganisationProfileId());
            assertEquals(accessTypeEntity.getAccessMandatory(), accessTypeField.getAccessMandatory());
            assertEquals(accessTypeEntity.getAccessDefault(), accessTypeField.getAccessDefault());
            assertEquals(accessTypeEntity.getDisplay(), accessTypeField.getDisplay());
            assertEquals(accessTypeEntity.getDescription(), accessTypeField.getDescription());
            assertEquals(accessTypeEntity.getHint(), accessTypeField.getHint());
            assertEquals(accessTypeEntity.getDisplayOrder(), accessTypeField.getDisplayOrder());
        }

        @Test
        void testMapEmptyAccessTypeRolesEntity() {

            AccessTypeEntity accessTypeEntity = new AccessTypeEntity();

            AccessTypeField accessTypeField = classUnderTest.map(accessTypeEntity);

            assertNull(accessTypeField.getId());
            assertNull(accessTypeField.getLiveFrom());
            assertNull(accessTypeField.getLiveTo());
            assertNull(accessTypeField.getCaseTypeId());
            assertNull(accessTypeField.getAccessTypeId());
            assertNull(accessTypeField.getOrganisationProfileId());
            assertNull(accessTypeField.getAccessMandatory());
            assertNull(accessTypeField.getAccessDefault());
            assertNull(accessTypeField.getDisplay());
            assertNull(accessTypeField.getDescription());
            assertNull(accessTypeField.getHint());
            assertNull(accessTypeField.getDisplayOrder());
        }
    }

    @Nested
    @DisplayName("Should return `a AccessTypeRole which matches the AccessTypeRoleEntity")
    class MapAccessTypeRoleEntityTests {

        @Test
        void testMapAccessTypeRolesEntity() {

            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();

            CaseTypeEntity caseType = new CaseTypeEntity();
            caseType.setReference("id");
            caseType.setName("Test case");
            caseType.setVersion(1);
            caseType.setDescription("Some case type");
            caseType.setJurisdiction(jurisdictionEntity);
            caseType.setSecurityClassification(SecurityClassification.PUBLIC);

            AccessTypeRoleEntity accessTypeRoleEntity = new AccessTypeRoleEntity();
            accessTypeRoleEntity.setLiveFrom(LocalDate.of(2023, Month.FEBRUARY, 12));
            accessTypeRoleEntity.setLiveTo(LocalDate.of(2027, Month.OCTOBER, 17));
            accessTypeRoleEntity.setCaseType(toCaseTypeLiteEntity(caseType));
            accessTypeRoleEntity.setAccessTypeId("some access type id");
            accessTypeRoleEntity.setOrganisationProfileId("some org profile id");
            accessTypeRoleEntity.setOrganisationalRoleName("some org role name");
            accessTypeRoleEntity.setGroupRoleName("some group role name");
            accessTypeRoleEntity.setCaseAssignedRoleField("some case assigned role field");
            accessTypeRoleEntity.setGroupAccessEnabled(true);
            accessTypeRoleEntity.setCaseAccessGroupIdTemplate("some access group id template");

            AccessTypeRoleField accessTypeRoleField = classUnderTest.map(accessTypeRoleEntity);

            assertEquals(accessTypeRoleEntity.getId(), accessTypeRoleField.getId());
            assertEquals(accessTypeRoleEntity.getLiveFrom(), accessTypeRoleField.getLiveFrom());
            assertEquals(accessTypeRoleEntity.getLiveTo(), accessTypeRoleField.getLiveTo());
            assertEquals(accessTypeRoleEntity.getCaseType().getReference(),
                accessTypeRoleField.getCaseTypeId());
            assertEquals(accessTypeRoleEntity.getAccessTypeId(), accessTypeRoleField.getAccessTypeId());
            assertEquals(accessTypeRoleEntity.getOrganisationProfileId(),
                accessTypeRoleField.getOrganisationProfileId());
            assertEquals(accessTypeRoleEntity.getOrganisationalRoleName(),
                accessTypeRoleField.getOrganisationalRoleName());
            assertEquals(accessTypeRoleEntity.getGroupRoleName(), accessTypeRoleField.getGroupRoleName());
            assertEquals(accessTypeRoleEntity.getCaseAssignedRoleField(),
                accessTypeRoleField.getCaseAssignedRoleField());
            assertEquals(accessTypeRoleEntity.getGroupAccessEnabled(), accessTypeRoleField.getGroupAccessEnabled());
            assertEquals(accessTypeRoleEntity.getCaseAccessGroupIdTemplate(),
                accessTypeRoleField.getCaseAccessGroupIdTemplate());
        }

        @Test
        void testMapEmptyAccessTypeRolesEntity() {

            AccessTypeRoleEntity accessTypeRoleEntity = new AccessTypeRoleEntity();

            AccessTypeRoleField accessTypeRoleField = classUnderTest.map(accessTypeRoleEntity);

            assertNull(accessTypeRoleField.getId());
            assertNull(accessTypeRoleField.getLiveFrom());
            assertNull(accessTypeRoleField.getLiveTo());
            assertNull(accessTypeRoleField.getCaseTypeId());
            assertNull(accessTypeRoleField.getAccessTypeId());
            assertNull(accessTypeRoleField.getOrganisationProfileId());
            assertNull(accessTypeRoleField.getOrganisationalRoleName());
            assertNull(accessTypeRoleField.getGroupRoleName());
            assertNull(accessTypeRoleField.getCaseAssignedRoleField());
            assertNull(accessTypeRoleField.getGroupAccessEnabled());
            assertNull(accessTypeRoleField.getCaseAccessGroupIdTemplate());
        }
    }

}
