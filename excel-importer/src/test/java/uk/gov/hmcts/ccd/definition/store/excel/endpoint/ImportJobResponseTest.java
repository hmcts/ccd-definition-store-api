package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImportJobResponseTest {

    @DisplayName("from entity with multi-line warnings → list of strings")
    @Test
    void fromEntity_multiLineWarnings_returnsList() {
        ImportJobEntity entity = buildEntity();
        entity.setWarnings("warn1\nwarn2\nwarn3");

        ImportJobResponse response = ImportJobResponse.from(entity);

        assertEquals(List.of("warn1", "warn2", "warn3"), response.warnings());
    }

    @DisplayName("from entity with null warnings → empty list")
    @Test
    void fromEntity_nullWarnings_returnsEmptyList() {
        ImportJobEntity entity = buildEntity();
        entity.setWarnings(null);

        ImportJobResponse response = ImportJobResponse.from(entity);

        assertNotNull(response.warnings());
        assertEquals(0, response.warnings().size());
    }

    @DisplayName("from entity with empty string warnings → empty list")
    @Test
    void fromEntity_emptyWarnings_returnsEmptyList() {
        ImportJobEntity entity = buildEntity();
        entity.setWarnings("");

        ImportJobResponse response = ImportJobResponse.from(entity);

        assertNotNull(response.warnings());
        assertEquals(0, response.warnings().size());
    }

    @DisplayName("from entity passes all other fields through unchanged")
    @Test
    void fromEntity_allFieldsPassedThrough() {
        UUID id = UUID.randomUUID();
        LocalDateTime submittedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime startedAt = LocalDateTime.of(2026, 1, 1, 10, 1);
        LocalDateTime completedAt = LocalDateTime.of(2026, 1, 1, 10, 5);

        ImportJobEntity entity = new ImportJobEntity();
        entity.setId(id);
        entity.setStatus(ImportJobStatus.COMPLETED);
        entity.setSubmittedBy("uid-abc");
        entity.setSubmittedAt(submittedAt);
        entity.setStartedAt(startedAt);
        entity.setCompletedAt(completedAt);
        entity.setErrorSummary(null);
        entity.setWarnings(null);
        entity.setReindexTaskId("task-xyz");

        ImportJobResponse response = ImportJobResponse.from(entity);

        assertEquals(id, response.id());
        assertEquals(ImportJobStatus.COMPLETED, response.status());
        assertEquals("uid-abc", response.submittedBy());
        assertEquals(submittedAt, response.submittedAt());
        assertEquals(startedAt, response.startedAt());
        assertEquals(completedAt, response.completedAt());
        assertNull(response.errorSummary());
        assertEquals("task-xyz", response.reindexTaskId());
    }

    private ImportJobEntity buildEntity() {
        ImportJobEntity entity = new ImportJobEntity();
        entity.setId(UUID.randomUUID());
        entity.setStatus(ImportJobStatus.COMPLETED);
        entity.setSubmittedBy("uid-test");
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setStartedAt(LocalDateTime.now());
        entity.setCompletedAt(LocalDateTime.now());
        return entity;
    }
}
