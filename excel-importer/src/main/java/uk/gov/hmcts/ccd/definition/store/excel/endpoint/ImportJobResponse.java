package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record ImportJobResponse(
    UUID id,
    ImportJobStatus status,
    String submittedBy,
    LocalDateTime submittedAt,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    String errorSummary,
    List<String> warnings,
    String reindexTaskId
) {
    public static ImportJobResponse from(ImportJobEntity entity) {
        String warningsStr = entity.getWarnings();
        List<String> warningsList;
        if (warningsStr == null || warningsStr.isEmpty()) {
            warningsList = Collections.emptyList();
        } else {
            warningsList = Arrays.asList(warningsStr.split("\n", -1));
        }

        return new ImportJobResponse(
            entity.getId(),
            entity.getStatus(),
            entity.getSubmittedBy(),
            entity.getSubmittedAt(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getErrorSummary(),
            warningsList,
            entity.getReindexTaskId()
        );
    }
}
