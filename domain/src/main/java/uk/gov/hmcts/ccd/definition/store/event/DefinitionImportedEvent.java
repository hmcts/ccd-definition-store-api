package uk.gov.hmcts.ccd.definition.store.event;

import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class DefinitionImportedEvent extends ImportEvent<List<CaseTypeEntity>> {

    public DefinitionImportedEvent(List<CaseTypeEntity> caseTypes) {
        super(caseTypes);
    }
}
