package uk.gov.hmcts.ccd.definition.store.event;

import java.util.List;

public record SnapshotCreationEvent(String jurisdiction, List<String> caseTypeReferences) {
    
}
