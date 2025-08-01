package uk.gov.hmcts.ccd.definition.store.domain.service.display;

import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeLiteRepository;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.EventRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPage;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageComplexFieldOverride;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageField;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;

class DisplayGroupAdapterServiceTest {

    @Mock
    private DisplayGroupRepository displayGroupRepository;
    @Mock
    private CaseTypeLiteRepository caseTypeLiteRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private CaseTypeLiteEntity caseTypeEntity;

    private DisplayGroupAdapterService classUnderTest;

    @BeforeEach
    void setUpMocks() {
        MockitoAnnotations.openMocks(this);
        classUnderTest = new DisplayGroupAdapterService(displayGroupRepository,
            caseTypeLiteRepository,
            eventRepository,
            new CaseFieldEntityUtil());
    }

    @Nested
    class FindTabStructureForCaseTypeTests {
        private static final String CASE_TYPE_REFERENCE = "caseTypeReference";
        private static final String EVENT_REFERENCE = "eventReference";
        private final Integer caseTypeEntityId = 1000;

        @Test
        void findWizardPagesByCaseTypeId() {
            EventEntity eventEntityMock = mock(EventEntity.class);
            given(eventEntityMock.getEventCaseFields())
                .willReturn(singletonList(eventCaseFieldEntity(
                    DisplayContext.MANDATORY, caseFieldEntity("someRef"), emptyList())));

            DisplayGroupEntity displayGroupEntity = displayGroupEntity(eventEntityMock,
                singletonList(displayGroupCaseField(caseFieldEntity("someRef"))));

            given(caseTypeEntity.getId()).willReturn(caseTypeEntityId);
            given(caseTypeLiteRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE))
                .willReturn(Optional.of(caseTypeEntity));

            given(eventRepository.findByReferenceAndCaseTypeId(EVENT_REFERENCE, caseTypeEntityId))
                .willReturn(singletonList(eventEntityMock));
            given(displayGroupRepository.findByTypeAndCaseTypeIdAndEventOrderByOrder(DisplayGroupType.PAGE,
                caseTypeEntityId,
                eventEntityMock)).willReturn(singletonList(displayGroupEntity));

            WizardPageCollection wizardPagesByCaseTypeId = classUnderTest.findWizardPagesByCaseTypeId(
                CASE_TYPE_REFERENCE,
                EVENT_REFERENCE);

            assertThat(wizardPagesByCaseTypeId.getWizardPages().size(), is(1));

            WizardPage wizardPage = wizardPagesByCaseTypeId.getWizardPages()
                .stream()
                .filter(wp -> wp.getId().equals("solicitorCreate1"))
                .findFirst().orElseThrow(IllegalStateException::new);

            assertEquals(displayGroupEntity.getLabel(), wizardPage.getLabel());
            assertEquals(displayGroupEntity.getOrder(), wizardPage.getOrder());
            assertEquals(displayGroupEntity.getWebhookMidEvent().getUrl(), wizardPage.getCallBackURLMidEvent());
            assertEquals(displayGroupEntity.getWebhookMidEvent().getTimeouts(), wizardPage.getRetriesTimeoutMidEvent());
            assertEquals(displayGroupEntity.getShowCondition(), wizardPage.getShowCondition());

            assertThat(displayGroupEntity.getDisplayGroupCaseFields().size(),
                is(wizardPage.getWizardPageFields().size()));

            assertEquals(displayGroupEntity.getDisplayGroupCaseFields().iterator().next().getCaseField().getReference(),
                wizardPage.getWizardPageFields().get(0).getCaseFieldId());
            assertEquals(displayGroupEntity.getDisplayGroupCaseFields().iterator().next().getOrder(),
                wizardPage.getWizardPageFields().get(0).getOrder());
            assertEquals(displayGroupEntity.getDisplayGroupCaseFields().iterator().next().getColumnNumber(),
                wizardPage.getWizardPageFields().get(0).getPageColumnNumber());
        }

        @Test
        void findWizardPagesByCaseTypeIdForComplexTypes() {
            EventComplexTypeEntity bailiffName = new EventComplexTypeEntity();
            bailiffName.setReference("bailiffName");
            bailiffName.setDisplayContext(DisplayContext.READONLY);
            bailiffName.setOrder(3);
            bailiffName.setHint("Hint text override");
            bailiffName.setLabel("Bailiff Name");
            bailiffName.setShowCondition("fieldName1.subField2=\"potato\"");
            bailiffName.setDefaultValue("DefaultValue1");
            bailiffName.setRetainHiddenValue(true);

            EventComplexTypeEntity addressLine1 = new EventComplexTypeEntity();
            addressLine1.setReference("addressAttended.AddressLine1");
            addressLine1.setDisplayContext(DisplayContext.MANDATORY);
            addressLine1.setOrder(1);
            addressLine1.setDefaultValue("DefaultValue2");

            EventComplexTypeEntity postcode = new EventComplexTypeEntity();
            postcode.setReference("addressAttended.Postcode");
            postcode.setDisplayContext(DisplayContext.OPTIONAL);
            postcode.setOrder(2);
            postcode.setDefaultValue("DefaultValue3");

            EventEntity eventEntityMock = mock(EventEntity.class);

            CaseFieldEntity finalReturnOther = caseFieldEntity("finalReturnOther",
                "Final return other",
                null, fieldTypeEntity("Text", textFieldTypeEntity(), emptyList()));
            EventCaseFieldEntity eventFinalReturnOther = eventCaseFieldEntity(
                DisplayContext.OPTIONAL, finalReturnOther, emptyList());

            given(eventEntityMock.getEventCaseFields()).willReturn(
                asList(
                    eventCaseFieldEntity(
                        DisplayContext.COMPLEX,
                        caseFieldEntity("finalReturn", "Final Return", null,
                            fieldTypeEntity("Return", baseFieldType("Complex"),
                                asList(complexFieldEntity(
                                    "bailiffName", "Bailiff",
                                    fieldType("FixedList-BailiffList")),
                                    complexFieldEntity(
                                        "dateOfVisit", "Date of visit", fieldType("Date")),
                                    complexFieldEntity("addressAttended", "Address Attended",
                                        fieldTypeEntity(PREDEFINED_COMPLEX_ADDRESS_UK,
                                            baseFieldType("Complex"),
                                            asList(
                                                complexFieldEntity("AddressLine1", "Building and Street",
                                                    fieldType("TextMax150")),
                                                complexFieldEntity("AddressLine2", "Building and Street",
                                                    fieldType("TextMax50")),
                                                complexFieldEntity("AddressLine3", "Building and Street",
                                                    fieldType("TextMax50")),
                                                complexFieldEntity("PostTown", "Town or City",
                                                    fieldType("TextMax50")),
                                                complexFieldEntity("County", "County",
                                                    fieldType("TextMax50")),
                                                complexFieldEntity("PostCode", "Postcode/Zipcode",
                                                    fieldType("TextMax14")),
                                                complexFieldEntity("Country", "Country",
                                                    fieldType("TextMax50")))))))

                        ),
                        asList(bailiffName, addressLine1, postcode)),
                    eventFinalReturnOther
                ));

            DisplayGroupEntity displayGroupEntity = displayGroupEntity(eventEntityMock,
                asList(displayGroupCaseField(caseFieldEntity("finalReturn")),
                    displayGroupCaseField(caseFieldEntity("finalReturnOther"))));

            given(caseTypeEntity.getId()).willReturn(caseTypeEntityId);
            given(caseTypeLiteRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE))
                .willReturn(Optional.of(caseTypeEntity));

            given(eventRepository.findByReferenceAndCaseTypeId(EVENT_REFERENCE, caseTypeEntityId))
                .willReturn(singletonList(eventEntityMock));
            given(displayGroupRepository.findByTypeAndCaseTypeIdAndEventOrderByOrder(DisplayGroupType.PAGE,
                caseTypeEntityId,
                eventEntityMock)).willReturn(singletonList(displayGroupEntity));

            WizardPageCollection wizardPagesByCaseTypeId = classUnderTest.findWizardPagesByCaseTypeId(
                CASE_TYPE_REFERENCE,
                EVENT_REFERENCE);

            assertThat(wizardPagesByCaseTypeId.getWizardPages().size(), is(1));

            WizardPage wizardPage = wizardPagesByCaseTypeId.getWizardPages().get(0);

            assertThat(findWizardPageField(wizardPage, "finalReturn").getOrder(), is(77));
            assertThat(findWizardPageField(wizardPage, "finalReturn").getPageColumnNumber(), is(66));

            assertThat(findWizardPageComplexFieldOverrides(wizardPage, "finalReturn").size(), is(10));
            assertThat(findWizardPageComplexFieldOverride(
                wizardPage, "finalReturn", "finalReturn.bailiffName").getDisplayContext(),
                is("READONLY"));
            assertThat(findWizardPageComplexFieldOverride(
                wizardPage, "finalReturn", "finalReturn.bailiffName").getLabel(),
                is("Bailiff Name"));
            assertThat(findWizardPageComplexFieldOverride(
                wizardPage, "finalReturn", "finalReturn.bailiffName").getDefaultValue(),
                is("DefaultValue1"));
            assertThat(findWizardPageComplexFieldOverride(
                wizardPage, "finalReturn", "finalReturn.bailiffName").getHintText(),
                is("Hint text override"));
            assertThat(findWizardPageComplexFieldOverride(
                wizardPage, "finalReturn", "finalReturn.bailiffName").getRetainHiddenValue(),
                is(true));
            assertThat(findWizardPageComplexFieldOverride(
                wizardPage, "finalReturn",
                "finalReturn.bailiffName").getShowCondition(), is("fieldName1.subField2=\"potato\""));

            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.dateOfVisit").getDisplayContext(), is("HIDDEN"));
            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.addressAttended.AddressLine1").getDisplayContext(), is("MANDATORY"));
            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.addressAttended.AddressLine1").getDefaultValue(), is("DefaultValue2"));

            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.addressAttended.AddressLine2").getDisplayContext(), is("HIDDEN"));
            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.addressAttended.AddressLine3").getDisplayContext(), is("HIDDEN"));
            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.addressAttended.PostTown").getDisplayContext(), is("HIDDEN"));
            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.addressAttended.County").getDisplayContext(), is("HIDDEN"));
            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.addressAttended.Postcode").getDisplayContext(), is("OPTIONAL"));

            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.addressAttended.Postcode").getDefaultValue(), is("DefaultValue3"));

            assertThat(findWizardPageComplexFieldOverride(wizardPage, "finalReturn",
                "finalReturn.addressAttended.Country").getDisplayContext(), is("HIDDEN"));
        }

        @Test
        void findWizardPagesByCaseTypeIdReturnsNullWhenNoCaseTypeFound() {
            given(caseTypeLiteRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE))
                .willReturn(Optional.empty());

            WizardPageCollection wizardPagesByCaseTypeId = classUnderTest.findWizardPagesByCaseTypeId(
                CASE_TYPE_REFERENCE,
                EVENT_REFERENCE);

            assertNull(wizardPagesByCaseTypeId);
        }

        @Test
        void findWizardPagesByCaseTypeIdReturnsNullWhenNoEventFound() {
            given(caseTypeLiteRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE))
                .willReturn(Optional.of(caseTypeEntity));
            given(eventRepository.findByReferenceAndCaseTypeId(anyString(), anyInt())).willReturn(emptyList());

            WizardPageCollection wizardPagesByCaseTypeId = classUnderTest.findWizardPagesByCaseTypeId(
                CASE_TYPE_REFERENCE,
                EVENT_REFERENCE);

            assertNull(wizardPagesByCaseTypeId);
        }
    }

    private static WizardPageField findWizardPageField(WizardPage wizardPage, String fieldId) {
        return wizardPage.getWizardPageFields()
            .stream()
            .filter(e -> e.getCaseFieldId().equals(fieldId))
            .findFirst()
            .get();
    }

    private static WizardPageComplexFieldOverride findWizardPageComplexFieldOverride(WizardPage wizardPage,
                                                                                     String fieldId,
                                                                                     String complexFieldId) {
        return findWizardPageComplexFieldOverrides(wizardPage, fieldId)
            .stream().filter(w -> w.getComplexFieldElementId().equals(complexFieldId)).findFirst().get();
    }

    private static List<WizardPageComplexFieldOverride> findWizardPageComplexFieldOverrides(WizardPage wizardPage,
                                                                                            String fieldId) {
        WizardPageField wizardPageField = wizardPage.getWizardPageFields().stream()
            .filter(e -> e.getCaseFieldId().equals(fieldId))
            .findFirst().get();

        return wizardPageField.getComplexFieldOverrides();
    }

    private static WebhookEntity webhookEntity() {
        WebhookEntity webhookEntity = new WebhookEntity();
        webhookEntity.setUrl("http://test1.com");
        webhookEntity.setTimeouts(List.of(120));
        return webhookEntity;
    }

    private DisplayGroupEntity displayGroupEntity(
        EventEntity eventEntity, List<DisplayGroupCaseFieldEntity> displayGroupCaseFieldEntities) {
        DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
        displayGroupEntity.setReference("solicitorCreate1");
        displayGroupEntity.setLabel("label");
        displayGroupEntity.setShowCondition("showCondition");
        displayGroupEntity.setOrder(55);
        displayGroupEntity.setWebhookMidEvent(webhookEntity());
        displayGroupEntity.addDisplayGroupCaseFields(displayGroupCaseFieldEntities);
        displayGroupEntity.setEvent(eventEntity);
        return displayGroupEntity;
    }

    private static DisplayGroupCaseFieldEntity displayGroupCaseField(CaseFieldEntity caseFieldEntity) {
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity.setCaseField(caseFieldEntity);
        displayGroupCaseFieldEntity.setOrder(77);
        displayGroupCaseFieldEntity.setColumnNumber(66);
        return displayGroupCaseFieldEntity;
    }

    private static FieldTypeEntity fieldTypeEntity(String reference, FieldTypeEntity baseFieldType,
                                                   List<ComplexFieldEntity> complexFieldEntities) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.setBaseFieldType(baseFieldType);
        fieldTypeEntity.addComplexFields(complexFieldEntities);
        return fieldTypeEntity;
    }

    private static FieldTypeEntity textFieldTypeEntity() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference("Text");
        fieldTypeEntity.addComplexFields(emptyList());
        return fieldTypeEntity;
    }

    private static FieldTypeEntity baseFieldType(String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        return fieldTypeEntity;
    }

    private static CaseFieldEntity caseFieldEntity(String reference) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        return caseFieldEntity;
    }

    private static CaseFieldEntity caseFieldEntity(String reference, String label,
                                                   String hint, FieldTypeEntity caseTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(caseTypeEntity);
        caseFieldEntity.setLabel(label);
        caseFieldEntity.setHint(hint);

        return caseFieldEntity;
    }

    private static ComplexFieldEntity complexFieldEntity(String reference, String label, FieldTypeEntity fieldType) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reference);
        complexFieldEntity.setLabel(label);
        complexFieldEntity.setFieldType(fieldType);
        return complexFieldEntity;
    }

    private static FieldTypeEntity fieldType(String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        return fieldTypeEntity;
    }

    private static EventCaseFieldEntity eventCaseFieldEntity(DisplayContext displayContext,
                                                             CaseFieldEntity caseField,
                                                             List<EventComplexTypeEntity> complexFields) {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setCaseField(caseField);
        eventCaseFieldEntity.setDisplayContext(displayContext);
        eventCaseFieldEntity.addComplexFields(complexFields);
        return eventCaseFieldEntity;
    }
}
