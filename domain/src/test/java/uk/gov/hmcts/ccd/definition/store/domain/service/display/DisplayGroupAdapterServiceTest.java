package uk.gov.hmcts.ccd.definition.store.domain.service.display;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.EventRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPage;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageCollection;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

class DisplayGroupAdapterServiceTest {

    @Mock
    private DisplayGroupRepository displayGroupRepository;
    @Mock
    private CaseTypeRepository caseTypeRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private CaseTypeEntity caseTypeEntity;

    @InjectMocks
    private DisplayGroupAdapterService classUnderTest;

    @BeforeEach
    public void setUpMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    class FindTabStructureForCaseTypeTests {
        private static final String CASE_TYPE_REFERENCE = "caseTypeReference";
        private static final String EVENT_REFERENCE = "eventReference";
        private final Integer caseTypeEntityId = 1000;

        @Test
        public void findWizardPagesByCaseTypeId() {

            DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
            displayGroupEntity.setReference("solicitorCreate1");
            displayGroupEntity.setLabel("label");
            displayGroupEntity.setShowCondition("showCondition");
            displayGroupEntity.setOrder(5);
            displayGroupEntity.setWebhookMidEvent(webhookEntity());
            displayGroupEntity.addDisplayGroupCaseFields(Collections.singletonList(displayGroupCaseField()));

            given(caseTypeEntity.getId()).willReturn(caseTypeEntityId);
            given(caseTypeRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE))
                .willReturn(Optional.of(caseTypeEntity));
            EventEntity eventEntityMock = mock(EventEntity.class);
            given(eventRepository.findByReferenceAndCaseTypeId(EVENT_REFERENCE, caseTypeEntityId))
                .willReturn(Collections.singletonList(eventEntityMock));
            given(displayGroupRepository.findByTypeAndCaseTypeIdAndEventOrderByOrder(DisplayGroupType.PAGE,
                caseTypeEntityId,
                eventEntityMock)).willReturn(Collections.singletonList(displayGroupEntity));

            WizardPageCollection wizardPagesByCaseTypeId = classUnderTest.findWizardPagesByCaseTypeId(
                CASE_TYPE_REFERENCE,
                EVENT_REFERENCE);

            assertThat(wizardPagesByCaseTypeId.getWizardPages().size(), is(1));

            WizardPage wizardPage = wizardPagesByCaseTypeId.getWizardPages()
                .stream()
                .filter(wp -> wp.getId().equals(
                    "solicitorCreate1"))
                .findFirst().orElseThrow(IllegalStateException::new);

            assertEquals(displayGroupEntity.getLabel(), wizardPage.getLabel());
            assertEquals(displayGroupEntity.getOrder(), wizardPage.getOrder());
            assertEquals(displayGroupEntity.getWebhookMidEvent().getUrl(), wizardPage.getCallBackURLMidEvent());
            assertEquals(displayGroupEntity.getWebhookMidEvent().getTimeouts(), wizardPage.getRetriesTimeoutMidEvent());
            assertEquals(displayGroupEntity.getShowCondition(), wizardPage.getShowCondition());

            assertThat(displayGroupEntity.getDisplayGroupCaseFields().size(),
                is(wizardPage.getWizardPageFields().size()));

            assertEquals(displayGroupEntity.getDisplayGroupCaseFields().get(0).getCaseField().getReference(),
                wizardPage.getWizardPageFields().get(0).getCaseFieldId());
            assertEquals(displayGroupEntity.getDisplayGroupCaseFields().get(0).getOrder(),
                wizardPage.getWizardPageFields().get(0).getOrder());
            assertEquals(displayGroupEntity.getDisplayGroupCaseFields().get(0).getColumnNumber(),
                wizardPage.getWizardPageFields().get(0).getPageColumnNumber());
        }

        @Test
        public void findWizardPagesByCaseTypeIdReturnsNullWhenNoCaseTypeFound() {
            given(caseTypeRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE)).willReturn(Optional.empty());

            WizardPageCollection wizardPagesByCaseTypeId = classUnderTest.findWizardPagesByCaseTypeId(
                CASE_TYPE_REFERENCE,
                EVENT_REFERENCE);

            assertNull(wizardPagesByCaseTypeId);
        }

        @Test
        public void findWizardPagesByCaseTypeIdReturnsNullWhenNoEventFound() {
            given(caseTypeRepository.findCurrentVersionForReference(CASE_TYPE_REFERENCE))
                .willReturn(Optional.of(caseTypeEntity));
            given(eventRepository.findByReferenceAndCaseTypeId(anyString(), anyInt())).willReturn(Collections.emptyList());

            WizardPageCollection wizardPagesByCaseTypeId = classUnderTest.findWizardPagesByCaseTypeId(
                CASE_TYPE_REFERENCE,
                EVENT_REFERENCE);

            assertNull(wizardPagesByCaseTypeId);
        }
    }

    private static WebhookEntity webhookEntity() {
        WebhookEntity webhookEntity = new WebhookEntity();
        webhookEntity.setUrl("http://test1.com");
        webhookEntity.addTimeout(120);
        return webhookEntity;
    }

    private static DisplayGroupCaseFieldEntity displayGroupCaseField() {
        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity.setCaseField(caseFieldEntity());
        displayGroupCaseFieldEntity.setOrder(7);
        displayGroupCaseFieldEntity.setColumnNumber(6);
        return displayGroupCaseFieldEntity;
    }

    private static CaseFieldEntity caseFieldEntity() {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference("someRef");
        return caseFieldEntity;
    }
}
