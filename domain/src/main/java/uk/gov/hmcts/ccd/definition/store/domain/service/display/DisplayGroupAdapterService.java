package uk.gov.hmcts.ccd.definition.store.domain.service.display;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.EventRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPage;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageCollection;
import uk.gov.hmcts.ccd.definition.store.repository.model.WizardPageField;

import java.util.List;
import java.util.Optional;

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

                    wizardPage.setShowCondition(displayGroupEntity.getShowCondition());
                    displayGroupEntity.getDisplayGroupCaseFields().forEach(displayGroupCaseFieldEntity -> {
                        WizardPageField wizardPageField = new WizardPageField();
                        wizardPageField.setCaseFieldId(displayGroupCaseFieldEntity.getCaseField().getReference());
                        wizardPageField.setOrder(displayGroupCaseFieldEntity.getOrder());
                        wizardPageField.setPageColumnNumber(displayGroupCaseFieldEntity.getColumnNumber());
                        wizardPage.getWizardPageFields().add(wizardPageField);
                    });
                    wizardPageCollection.getWizardPages().add(wizardPage);
                });
                return wizardPageCollection;
            }
        }
        return null;
    }
}
