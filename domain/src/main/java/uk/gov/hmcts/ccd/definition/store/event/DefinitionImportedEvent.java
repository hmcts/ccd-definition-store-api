package uk.gov.hmcts.ccd.definition.store.event;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;

@Getter
public class DefinitionImportedEvent extends ImportEvent<List<CaseTypeEntity>> {

    private final boolean reindex;
    private final boolean deleteOldIndex;
    @Setter private String taskId;

    public DefinitionImportedEvent(List<CaseTypeEntity> caseTypes, boolean reindex, boolean deleteOldIndex) {
        super(caseTypes);
        this.reindex = reindex;
        this.deleteOldIndex = deleteOldIndex;
    }

    public DefinitionImportedEvent(List<CaseTypeEntity> caseTypes) {
        super(caseTypes);
        this.reindex = false;
        this.deleteOldIndex = false;
    }
}
