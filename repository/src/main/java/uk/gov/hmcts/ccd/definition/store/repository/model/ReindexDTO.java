package uk.gov.hmcts.ccd.definition.store.repository.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReindexDTO {
    private boolean reindex;
    private boolean deleteOldIndex;
    private String caseType;
    private String jurisdiction;
    private String indexName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String exceptionMessage;
    private String reindexResponse;
}
