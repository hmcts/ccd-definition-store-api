package uk.gov.hmcts.ccd.definition.store.event;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;

public class DefinitionImportedEvent extends ImportEvent<List<CaseTypeEntity>> {

    public DefinitionImportedEvent(List<CaseTypeEntity> caseTypes) {
        super(caseTypes);
    }
}
