package uk.gov.hmcts.ccd.definition.store.event;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;

public class DefinitionImportedEvent extends ImportEvent<List<CaseTypeEntity>> {

    private final boolean reindex;
    private final boolean deleteOldIndex;
    @Setter @Getter private String taskId;

    public DefinitionImportedEvent(List<CaseTypeEntity> caseTypes, boolean reindex, boolean deleteOldIndex) {
        super(caseTypes);
        this.reindex = reindex;
        this.deleteOldIndex = deleteOldIndex;
    }

    public boolean isReindex() {
        return reindex;
    }

    public boolean isDeleteOldIndex() {
        return deleteOldIndex;
    }
}
