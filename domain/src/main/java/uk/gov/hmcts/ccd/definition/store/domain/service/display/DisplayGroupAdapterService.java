package uk.gov.hmcts.ccd.definition.store.domain.service.display;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeLiteRepository;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.EventRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPage;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageComplexFieldOverride;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageField;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class DisplayGroupAdapterService {

    private DisplayGroupRepository displayGroupRepository;
    private CaseTypeLiteRepository caseTypeLiteRepository;
    private EventRepository eventRepository;
    private CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public DisplayGroupAdapterService(DisplayGroupRepository displayGroupRepository,
                                      CaseTypeLiteRepository caseTypeLiteRepository,
                                      EventRepository eventRepository,
                                      CaseFieldEntityUtil caseFieldEntityUtil) {
        this.displayGroupRepository = displayGroupRepository;
        this.caseTypeLiteRepository = caseTypeLiteRepository;
        this.eventRepository = eventRepository;
        this.caseFieldEntityUtil = caseFieldEntityUtil;
    }

    public WizardPageCollection findWizardPagesByCaseTypeId(String caseTypeReference, String eventReference) {
        Optional<CaseTypeLiteEntity> caseTypeEntity =
            caseTypeLiteRepository.findCurrentVersionForReference(caseTypeReference);
        if (caseTypeEntity.isPresent()) {
            List<EventEntity> events = eventRepository.findByReferenceAndCaseTypeId(
                eventReference, caseTypeEntity.get().getId());
            if (events.size() == 1) {
                final List<DisplayGroupEntity> displayGroupEntityList = displayGroupRepository
                    .findByTypeAndCaseTypeIdAndEventOrderByOrder(
                        DisplayGroupType.PAGE, caseTypeEntity.get().getId(), events.get(0));

                WizardPageCollection wizardPageCollection = new WizardPageCollection(caseTypeReference, eventReference);
                wizardPageCollection.getWizardPages().addAll(displayGroupEntityList.stream()
                    .map(this::createWizardPage)
                    .collect(Collectors.toList()));

                return wizardPageCollection;
            }
        }
        return null;
    }

    private WizardPage createWizardPage(final DisplayGroupEntity displayGroupEntity) {
        WizardPage wizardPage = new WizardPage();
        wizardPage.setId(displayGroupEntity.getReference());
        wizardPage.setLabel(displayGroupEntity.getLabel());
        wizardPage.setOrder(displayGroupEntity.getOrder());

        Optional.ofNullable(displayGroupEntity.getWebhookMidEvent()).ifPresent(webhookEntity -> {
            wizardPage.setCallBackURLMidEvent(webhookEntity.getUrl());
            wizardPage.setRetriesTimeoutMidEvent(webhookEntity.getTimeouts());
        });

        Set<String> allSubTypePossibilities = createAllSubtypeLeafCombinations(displayGroupEntity);

        wizardPage.setShowCondition(displayGroupEntity.getShowCondition());
        wizardPage.getWizardPageFields().addAll(
            displayGroupEntity.getDisplayGroupCaseFields().stream()
                .map(displayGroupCaseFieldEntity -> createWizardCaseField(
                    displayGroupEntity, allSubTypePossibilities, displayGroupCaseFieldEntity))
                .collect(Collectors.toSet()));

        return wizardPage;
    }

    private WizardPageField createWizardCaseField(final DisplayGroupEntity displayGroupEntity,
                                                  final Set<String> allSubTypePossibilities,
                                                  final DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity) {
        String reference = displayGroupCaseFieldEntity.getCaseField().getReference();
        WizardPageField wizardPageField = new WizardPageField();
        wizardPageField.setCaseFieldId(reference);
        wizardPageField.setOrder(displayGroupCaseFieldEntity.getOrder());
        wizardPageField.setPageColumnNumber(displayGroupCaseFieldEntity.getColumnNumber());
        EventCaseFieldEntity eventCaseFieldEntity = getEventCaseFieldEntityByReference(
            reference, displayGroupEntity.getEvent());

        if (DisplayContext.COMPLEX == eventCaseFieldEntity.getDisplayContext()) {

            wizardPageField.addAllComplexFieldOverrides(eventCaseFieldEntity.getEventComplexTypes()
                .stream()
                .map(eventComplexTypeEntity -> createWizardPageComplexFieldOverride(reference, eventComplexTypeEntity))
                .collect(Collectors.toList()));

            wizardPageField.addAllComplexFieldOverrides(determineHiddenFieldsOverrides(
                allSubTypePossibilities, displayGroupCaseFieldEntity, wizardPageField)
                .stream()
                .map(DisplayGroupAdapterService::hiddenWizardPageComplexFieldOverride)
                .collect(Collectors.toList()));
        }

        return wizardPageField;
    }

    private Set<String> determineHiddenFieldsOverrides(final Set<String> allSubTypePossibilities,
                                                        final DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity,
                                                        final WizardPageField wizardPageField) {
        Set<String> complexFieldOverrideIds = wizardPageField.getComplexFieldOverrides()
            .stream()
            .map(WizardPageComplexFieldOverride::getComplexFieldElementId)
            .collect(Collectors.toSet());

        return allSubTypePossibilities.stream()
            .filter(e -> e.startsWith(displayGroupCaseFieldEntity.getCaseField().getReference() + '.'))
            .filter(e -> !e.equals(displayGroupCaseFieldEntity.getCaseField().getReference()))
            .filter(e -> !complexFieldOverrideIds.contains(e))
            .collect(Collectors.toSet());
    }

    private WizardPageComplexFieldOverride createWizardPageComplexFieldOverride(
        final String reference, final EventComplexTypeEntity eventComplexTypeEntity) {
        WizardPageComplexFieldOverride override = new WizardPageComplexFieldOverride();
        override.setComplexFieldElementId(reference + "." + eventComplexTypeEntity.getReference());
        override.setDisplayContext(eventComplexTypeEntity.getDisplayContext().toString());
        override.setLabel(eventComplexTypeEntity.getLabel());
        override.setHintText(eventComplexTypeEntity.getHint());
        override.setShowCondition(eventComplexTypeEntity.getShowCondition());
        override.setDefaultValue(eventComplexTypeEntity.getDefaultValue());
        override.setRetainHiddenValue(eventComplexTypeEntity.getRetainHiddenValue());
        return override;
    }

    private Set<String> createAllSubtypeLeafCombinations(final DisplayGroupEntity displayGroupEntity) {
        return displayGroupEntity.getEvent().getEventCaseFields() != null
            && !displayGroupEntity.getEvent().getEventCaseFields().isEmpty()
            ? caseFieldEntityUtil.buildDottedComplexFieldPossibilities(
                displayGroupEntity.getEvent().getEventCaseFields().stream().map(EventCaseFieldEntity::getCaseField)
                    .collect(Collectors.toSet())) : Collections.emptySet();
    }

    private EventCaseFieldEntity getEventCaseFieldEntityByReference(String reference, EventEntity event) {
        List<EventCaseFieldEntity> eventCaseFieldEntities = event.getEventCaseFields();

        return eventCaseFieldEntities.stream()
            .filter(e -> reference.equals(e.getCaseField().getReference()))
            .findFirst()
            .orElseThrow(() -> new MissingEventCaseFieldEntityException(
                format("EventCaseField.caseField %s missing", reference)));
    }

    private static WizardPageComplexFieldOverride hiddenWizardPageComplexFieldOverride(String reference) {
        WizardPageComplexFieldOverride override = new WizardPageComplexFieldOverride();
        override.setComplexFieldElementId(reference);
        override.setDisplayContext("HIDDEN");
        return override;
    }
}
