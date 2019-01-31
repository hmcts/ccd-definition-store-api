package uk.gov.hmcts.ccd.definition.store.domain.service.display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageComplexFieldMask;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageField;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class DisplayGroupAdapterService {

    private static final Logger LOG = LoggerFactory.getLogger(DisplayGroupAdapterService.class);

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
                        displayGroupEntity.getEvent().getEventCaseFields() != null && displayGroupEntity.getEvent().getEventCaseFields().size() > 0 ?
                            CaseFieldEntityUtil.buildDottedComplexFieldPossibilities(displayGroupEntity.getEvent().getEventCaseFields()
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
                                WizardPageComplexFieldMask mask = new WizardPageComplexFieldMask();
                                mask.setComplexFieldId(reference + "." +eventComplexTypeEntity.getReference());
                                mask.setDisplayContext(eventComplexTypeEntity.getDisplayContext().toString());
                                mask.setOrder(eventComplexTypeEntity.getOrder());
                                mask.setLabel(eventComplexTypeEntity.getLabel());
                                mask.setHintText(eventComplexTypeEntity.getHint());
                                mask.setShowCondition(eventComplexTypeEntity.getShowCondition());

                                wizardPageField.addComplexFieldMask(mask);
                            });

                            List<String> complexFieldMaskIds = wizardPageField.getComplexFieldMaskList()
                                .stream()
                                .map(WizardPageComplexFieldMask::getComplexFieldId)
                                .collect(Collectors.toList());

                            List<String> hiddenFieldMaskToCreate = allSubTypePossibilities.stream()
                                .filter(e -> e.startsWith(displayGroupCaseFieldEntity.getCaseField().getReference()))
                                .filter(e -> !e.equals(displayGroupCaseFieldEntity.getCaseField().getReference()))
                                .filter(e -> !complexFieldMaskIds.contains(e))
                                .collect(Collectors.toList());

                            hiddenFieldMaskToCreate.forEach(hiddenFieldMask ->
                                wizardPageField.addComplexFieldMask(hiddenWizardPageComplexFieldMask(hiddenFieldMask)));
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

    private static WizardPageComplexFieldMask hiddenWizardPageComplexFieldMask(String reference) {
        WizardPageComplexFieldMask mask = new WizardPageComplexFieldMask();
        mask.setComplexFieldId(reference);
        mask.setDisplayContext("HIDDEN");
        return mask;
    }
}
