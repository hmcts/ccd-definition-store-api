package uk.gov.hmcts.ccd.definition.store.domain.service.display;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.EventRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPage;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageComplexFieldOverride;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageField;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class DisplayGroupAdapterService {

    private DisplayGroupRepository displayGroupRepository;
    private CaseTypeRepository caseTypeRepository;
    private EventRepository eventRepository;

    @Autowired
    public DisplayGroupAdapterService(DisplayGroupRepository displayGroupRepository, CaseTypeRepository caseTypeRepository, EventRepository eventRepository) {
        this.displayGroupRepository = displayGroupRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.eventRepository = eventRepository;
    }

    public WizardPageCollection findWizardPagesByCaseTypeId(String caseTypeReference, String eventReference) {
        Optional<CaseTypeEntity> caseTypeEntity = caseTypeRepository.findCurrentVersionForReference(caseTypeReference);
        if (caseTypeEntity.isPresent()) {
            List<EventEntity> events = eventRepository.findByReferenceAndCaseTypeId(eventReference, caseTypeEntity.get().getId());
            if (events.size() == 1) {
                final List<DisplayGroupEntity> displayGroupEntityList = displayGroupRepository.findByTypeAndCaseTypeIdAndEventOrderByOrder(
                    DisplayGroupType.PAGE, caseTypeEntity.get().getId(), events.get(0));
                WizardPageCollection wizardPageCollection = new WizardPageCollection(caseTypeReference, eventReference);
                displayGroupEntityList.forEach(displayGroupEntity -> {
                    WizardPage wizardPage = new WizardPage();
                    wizardPage.setId(displayGroupEntity.getReference());
                    wizardPage.setLabel(displayGroupEntity.getLabel());
                    wizardPage.setOrder(displayGroupEntity.getOrder());

                    Optional.ofNullable(displayGroupEntity.getWebhookMidEvent()).ifPresent(webhookEntity -> {
                        wizardPage.setCallBackURLMidEvent(webhookEntity.getUrl());
                        wizardPage.setRetriesTimeoutMidEvent(webhookEntity.getTimeouts());
                    });

                    List<String> allSubTypePossibilities =
                        displayGroupEntity.getEvent().getEventCaseFields() != null && !displayGroupEntity.getEvent().getEventCaseFields().isEmpty()
                            ? CaseFieldEntityUtil.buildDottedComplexFieldPossibilities(displayGroupEntity.getEvent().getEventCaseFields()
                            .stream().map(EventCaseFieldEntity::getCaseField)
                            .collect(Collectors.toList())) : Collections.emptyList();

                    wizardPage.setShowCondition(displayGroupEntity.getShowCondition());
                    displayGroupEntity.getDisplayGroupCaseFields().forEach(displayGroupCaseFieldEntity -> {

                        String reference = displayGroupCaseFieldEntity.getCaseField().getReference();
                        EventCaseFieldEntity eventCaseFieldEntity = getEventCaseFieldEntityByReference(
                            reference, displayGroupEntity.getEvent());

                        WizardPageField wizardPageField = new WizardPageField();
                        wizardPageField.setCaseFieldId(reference);
                        wizardPageField.setOrder(displayGroupCaseFieldEntity.getOrder());
                        wizardPageField.setPageColumnNumber(displayGroupCaseFieldEntity.getColumnNumber());
                        wizardPageField.setDisplayContext(eventCaseFieldEntity.getDisplayContext());

                        if (DisplayContext.COMPLEX == eventCaseFieldEntity.getDisplayContext()) {
                            eventCaseFieldEntity.getEventComplexTypes().forEach(eventComplexTypeEntity -> {
                                WizardPageComplexFieldOverride override = new WizardPageComplexFieldOverride();
                                override.setComplexFieldElementId(reference + "." + eventComplexTypeEntity.getReference());
                                override.setDisplayContext(eventComplexTypeEntity.getDisplayContext().toString());
                                override.setOrder(eventComplexTypeEntity.getOrder());
                                override.setLabel(eventComplexTypeEntity.getLabel());
                                override.setHintText(eventComplexTypeEntity.getHint());
                                override.setShowCondition(eventComplexTypeEntity.getShowCondition());

                                wizardPageField.addComplexFieldOverride(override);
                            });

                            List<String> complexFieldOverrideIds = wizardPageField.getComplexFieldOverrides()
                                .stream()
                                .map(WizardPageComplexFieldOverride::getComplexFieldElementId)
                                .collect(Collectors.toList());

                            List<String> hiddenFieldOverrideToCreate = allSubTypePossibilities.stream()
                                .filter(e -> e.startsWith(displayGroupCaseFieldEntity.getCaseField().getReference()))
                                .filter(e -> !e.equals(displayGroupCaseFieldEntity.getCaseField().getReference()))
                                .filter(e -> !complexFieldOverrideIds.contains(e))
                                .collect(Collectors.toList());

                            hiddenFieldOverrideToCreate.forEach(hiddenFieldOverride ->
                                wizardPageField.addComplexFieldOverride(hiddenWizardPageComplexFieldOverride(hiddenFieldOverride)));
                        }
                        wizardPage.getWizardPageFields().add(wizardPageField);
                    });
                    wizardPageCollection.getWizardPages().add(wizardPage);
                });
                return wizardPageCollection;
            }
        }
        return null;
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
