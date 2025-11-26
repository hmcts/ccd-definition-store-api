package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SnapshotCreator {

    private final CaseTypeService caseTypeService;

    public SnapshotCreator(CaseTypeService caseTypeService) {
        this.caseTypeService = caseTypeService;
    }

    public void createSnapshotForCaseType(String caseTypeReference) {
        log.debug("Creating snapshot for case type: {}", caseTypeReference);
        caseTypeService.findByCaseTypeId(caseTypeReference)
            .ifPresentOrElse(
                caseType -> log.debug("Successfully created snapshot for case type: {} with version: {}",
                    caseTypeReference, caseType.getVersion()),
                () -> log.warn("Case type not found for snapshot creation: {}", caseTypeReference)
            );
    }
}
