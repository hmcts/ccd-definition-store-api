package uk.gov.hmcts.ccd.definition.store.domain.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.repository.ImportJobRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImportJobServiceTest {

    @Mock
    private ImportJobRepository repository;

    @Mock
    private ApplicationParams applicationParams;

    @Mock
    private EntityManager entityManager;

    @Captor
    private ArgumentCaptor<ImportJobEntity> entityCaptor;

    private ImportJobService classUnderTest;

    @BeforeEach
    void setUp() {
        classUnderTest = new ImportJobService(repository, applicationParams);
        ReflectionTestUtils.setField(classUnderTest, "entityManager", entityManager);
    }

    @DisplayName("createPending with null id generates a UUID and persists with PENDING status")
    @Test
    void createPending_nullId_generatesUuid() {
        UUID returned = classUnderTest.createPending(null, "user-123");

        verify(entityManager).persist(entityCaptor.capture());
        verify(entityManager).flush();
        ImportJobEntity saved = entityCaptor.getValue();
        assertAll(
            () -> assertNotNull(returned),
            () -> assertThat(saved.getId(), is(returned)),
            () -> assertThat(saved.getStatus(), is(ImportJobStatus.PENDING)),
            () -> assertThat(saved.getSubmittedBy(), is("user-123")),
            () -> assertNotNull(saved.getSubmittedAt()),
            () -> assertNotNull(saved.getStartedAt())
        );
    }

    @DisplayName("createPending with supplied id uses that id")
    @Test
    void createPending_suppliedId_usesIt() {
        UUID supplied = UUID.randomUUID();

        UUID returned = classUnderTest.createPending(supplied, "user-456");

        verify(entityManager).persist(entityCaptor.capture());
        assertAll(
            () -> assertThat(returned, is(supplied)),
            () -> assertThat(entityCaptor.getValue().getId(), is(supplied))
        );
    }

    @DisplayName("createPending wraps PersistenceException from flush as DataIntegrityViolationException")
    @Test
    void createPending_propagatesConstraintViolation() {
        doThrow(new PersistenceException("duplicate key")).when(entityManager).flush();
        UUID id = UUID.randomUUID();

        assertThrows(DataIntegrityViolationException.class,
            () -> classUnderTest.createPending(id, "user-789"));
    }

    @DisplayName("markCompleted loads, updates status to COMPLETED and saves")
    @Test
    void markCompleted_updatesEntity() {
        UUID id = UUID.randomUUID();
        ImportJobEntity existing = new ImportJobEntity();
        existing.setId(id);
        doReturn(Optional.of(existing)).when(repository).findById(id);
        String reindexTaskId = "task-001";

        classUnderTest.markCompleted(id, List.of("warn1", "warn2"), reindexTaskId);

        verify(repository).save(entityCaptor.capture());
        ImportJobEntity saved = entityCaptor.getValue();
        assertAll(
            () -> assertThat(saved.getStatus(), is(ImportJobStatus.COMPLETED)),
            () -> assertNotNull(saved.getCompletedAt()),
            () -> assertThat(saved.getWarnings(), is("warn1\nwarn2")),
            () -> assertThat(saved.getReindexTaskId(), is(reindexTaskId))
        );
    }

    @DisplayName("markCompleted serialises null warnings as null")
    @Test
    void markCompleted_nullWarnings_storesNull() {
        UUID id = UUID.randomUUID();
        doReturn(Optional.of(new ImportJobEntity())).when(repository).findById(id);

        classUnderTest.markCompleted(id, null, null);

        verify(repository).save(entityCaptor.capture());
        assertNull(entityCaptor.getValue().getWarnings());
    }

    @DisplayName("markCompleted serialises empty warnings list as null")
    @Test
    void markCompleted_emptyWarnings_storesNull() {
        UUID id = UUID.randomUUID();
        doReturn(Optional.of(new ImportJobEntity())).when(repository).findById(id);

        classUnderTest.markCompleted(id, Collections.emptyList(), null);

        verify(repository).save(entityCaptor.capture());
        assertNull(entityCaptor.getValue().getWarnings());
    }

    @DisplayName("markCompleted returns without saving when entity not found")
    @Test
    void markCompleted_entityNotFound_doesNotSave() {
        UUID id = UUID.randomUUID();
        doReturn(Optional.empty()).when(repository).findById(id);

        classUnderTest.markCompleted(id, null, null);

        verify(repository, never()).save(any());
    }

    @DisplayName("markFailed loads, updates status to FAILED and saves")
    @Test
    void markFailed_updatesEntity() {
        UUID id = UUID.randomUUID();
        ImportJobEntity existing = new ImportJobEntity();
        existing.setId(id);
        doReturn(Optional.of(existing)).when(repository).findById(id);

        classUnderTest.markFailed(id, "Something went wrong");

        verify(repository).save(entityCaptor.capture());
        ImportJobEntity saved = entityCaptor.getValue();
        assertAll(
            () -> assertThat(saved.getStatus(), is(ImportJobStatus.FAILED)),
            () -> assertNotNull(saved.getCompletedAt()),
            () -> assertThat(saved.getErrorSummary(), is("Something went wrong"))
        );
    }

    @DisplayName("markFailed truncates errorSummary to 2000 chars when longer")
    @Test
    void markFailed_truncatesLongErrorSummary() {
        UUID id = UUID.randomUUID();
        doReturn(Optional.of(new ImportJobEntity())).when(repository).findById(id);
        String longSummary = "x".repeat(3000);

        classUnderTest.markFailed(id, longSummary);

        verify(repository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getErrorSummary().length(), is(2000));
    }

    @DisplayName("markFailed returns without saving when entity not found")
    @Test
    void markFailed_entityNotFound_doesNotSave() {
        UUID id = UUID.randomUUID();
        doReturn(Optional.empty()).when(repository).findById(id);

        classUnderTest.markFailed(id, "error");

        verify(repository, never()).save(any());
    }

    @DisplayName("expireStaleJobs reads threshold from ApplicationParams and passes it to repository")
    @Test
    void expireStaleJobs_passesThresholdToRepository() {
        doReturn(150).when(applicationParams).getImportJobStaleThresholdSeconds();
        doReturn(0).when(repository).expireStaleJobs(150);

        classUnderTest.expireStaleJobs();

        verify(repository).expireStaleJobs(150);
    }

    @DisplayName("expireStaleJobs returns the row count from repository")
    @Test
    void expireStaleJobs_returnsCount() {
        doReturn(150).when(applicationParams).getImportJobStaleThresholdSeconds();
        doReturn(3).when(repository).expireStaleJobs(150);

        int count = classUnderTest.expireStaleJobs();

        assertThat(count, is(3));
    }

    @DisplayName("findById delegates to repository")
    @Test
    void findById_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        ImportJobEntity entity = new ImportJobEntity();
        doReturn(Optional.of(entity)).when(repository).findById(id);

        Optional<ImportJobEntity> result = classUnderTest.findById(id);

        assertAll(
            () -> assertThat(result.isPresent(), is(true)),
            () -> assertThat(result.get(), is(entity))
        );
    }

    @DisplayName("markFailed saves null error summary without throwing exception")
    @Test
    void markFailed_nullErrorSummary_storesNull() {
        UUID id = UUID.randomUUID();
        doReturn(Optional.of(new ImportJobEntity())).when(repository).findById(id);

        classUnderTest.markFailed(id, null);

        verify(repository).save(entityCaptor.capture());
        assertNull(entityCaptor.getValue().getErrorSummary());
    }

    @DisplayName("markFailed does not truncate error summary of exactly 2000 chars")
    @Test
    void markFailed_exactLimitErrorSummary_doesNotTruncate() {
        UUID id = UUID.randomUUID();
        doReturn(Optional.of(new ImportJobEntity())).when(repository).findById(id);
        String exactSummary = "x".repeat(2000);

        classUnderTest.markFailed(id, exactSummary);

        verify(repository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getErrorSummary().length(), is(2000));
    }
}
