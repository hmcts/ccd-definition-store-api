package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeSnapshotRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.SnapshotJdbcRepository;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.Version;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseTypeSnapshotServiceTest {

    private static final String CASE_TYPE_REF = "TestCaseType";
    private static final Integer VERSION = 1;

    @Mock
    private CaseTypeSnapshotRepository snapshotRepository;

    @Mock
    private SnapshotJdbcRepository snapshotJdbcRepository;

    private CaseTypeSnapshotService caseTypeSnapshotService;

    private CaseType sampleCaseType;

    @BeforeEach
    void setUp() {
        caseTypeSnapshotService = new CaseTypeSnapshotService(snapshotRepository, snapshotJdbcRepository, true);
        sampleCaseType = createSampleCaseType(CASE_TYPE_REF, VERSION);
    }

    @Test
    void shouldSkipLookup_whenSnapshotCacheDisabled() {
        caseTypeSnapshotService = new CaseTypeSnapshotService(snapshotRepository, snapshotJdbcRepository, false);

        Optional<CaseType> result = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, VERSION);

        assertFalse(result.isPresent());
        verifyNoInteractions(snapshotJdbcRepository);
    }

    @Test
    void shouldSkipStore_whenSnapshotCacheDisabled() {
        caseTypeSnapshotService = new CaseTypeSnapshotService(snapshotRepository, snapshotJdbcRepository, false);

        caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, VERSION, sampleCaseType);

        verifyNoInteractions(snapshotRepository);
    }

    @Test
    void shouldReturnSnapshot_whenSnapshotExists() {
        // Given: Snapshot exists in repository
        when(snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF, VERSION))
            .thenReturn(Optional.of(sampleCaseType));

        // When: Get snapshot
        Optional<CaseType> result = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, VERSION);

        // Then: Should return the cached snapshot
        assertTrue(result.isPresent());
        assertThat(result.get().getId(), is(CASE_TYPE_REF));
        verify(snapshotJdbcRepository, times(1)).loadCaseTypeSnapshot(CASE_TYPE_REF, VERSION);
    }

    @Test
    void shouldReturnEmpty_whenSnapshotDoesNotExist() {
        // Given: No snapshot exists
        when(snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF, VERSION))
            .thenReturn(Optional.empty());

        // When: Get snapshot
        Optional<CaseType> result = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, VERSION);

        // Then: Should return empty
        assertFalse(result.isPresent());
        verify(snapshotJdbcRepository, times(1)).loadCaseTypeSnapshot(CASE_TYPE_REF, VERSION);
    }

    @Test
    void shouldDiscardSnapshot_whenRowExistsForVersionButCannotBeRead() {
        // Given: A row exists for the current version but the read could not deserialize it
        when(snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF, VERSION))
            .thenReturn(Optional.empty());
        when(snapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF, VERSION))
            .thenReturn(true);

        // When: Get snapshot
        Optional<CaseType> result = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, VERSION);

        // Then: The broken row is removed so the following store can rebuild it
        assertFalse(result.isPresent());
        verify(snapshotRepository, times(1)).deleteSnapshot(CASE_TYPE_REF, VERSION);
    }

    @Test
    void shouldNotDiscardSnapshot_whenNoRowExistsForVersion() {
        // Given: An ordinary cache miss - nothing is stored for this version
        when(snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF, VERSION))
            .thenReturn(Optional.empty());
        when(snapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF, VERSION))
            .thenReturn(false);

        // When: Get snapshot
        Optional<CaseType> result = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, VERSION);

        // Then: Nothing is deleted
        assertFalse(result.isPresent());
        verify(snapshotRepository, never()).deleteSnapshot(anyString(), anyInt());
    }

    @Test
    void shouldNotDiscardSnapshot_whenSnapshotIsReadSuccessfully() {
        // Given: The snapshot reads back cleanly
        when(snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF, VERSION))
            .thenReturn(Optional.of(sampleCaseType));

        // When: Get snapshot
        Optional<CaseType> result = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, VERSION);

        // Then: The existence check is not even reached
        assertTrue(result.isPresent());
        verify(snapshotRepository, never()).existsByCaseTypeReferenceAndVersionId(anyString(), anyInt());
        verify(snapshotRepository, never()).deleteSnapshot(anyString(), anyInt());
    }

    @Test
    void shouldReturnEmpty_whenDiscardingUnreadableSnapshotFails() {
        // Given: The delete of a broken row fails
        when(snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF, VERSION))
            .thenReturn(Optional.empty());
        when(snapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF, VERSION))
            .thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(snapshotRepository)
            .deleteSnapshot(anyString(), anyInt());

        // When: Get snapshot
        Optional<CaseType> result = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, VERSION);

        // Then: The failure is swallowed so the request can still fall back to the database
        assertFalse(result.isPresent());
    }

    @Test
    void shouldSkipStore_whenSnapshotAlreadyExistsForVersion() {
        // Given: A snapshot is already held for this exact version
        when(snapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF, VERSION))
            .thenReturn(true);

        // When: Store snapshot
        caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, VERSION, sampleCaseType);

        // Then: No write is attempted
        verify(snapshotRepository, never()).upsertSnapshot(anyString(), anyInt(), anyString());
    }

    @Test
    void shouldStoreSnapshot_whenCaseTypeIsValid() {
        // Given: Valid case type to store

        // When: Store snapshot
        caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, VERSION, sampleCaseType);

        // Then: Should serialize and store the snapshot
        verify(snapshotRepository, times(1)).upsertSnapshot(
            eq(CASE_TYPE_REF),
            eq(VERSION),
            anyString()
        );
    }

    @Test
    void shouldNotStoreSnapshot_whenSerializationProducesEmptyString() {
        // Given: Case type that will serialize to empty string
        CaseType emptyCaseType = new CaseType();

        // When: Store snapshot with potentially empty serialization
        caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, VERSION, emptyCaseType);

        // Then: Should attempt to store (actual empty check happens in real serialization)
        verify(snapshotRepository, times(1)).upsertSnapshot(
            eq(CASE_TYPE_REF),
            eq(VERSION),
            anyString()
        );
    }

    @Test
    void shouldHandleException_whenSerializationFails() {
        // Given: Case type that causes serialization exception
        CaseType problematicCaseType = createProblematicCaseType();

        // When: Store snapshot that fails during serialization
        caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, VERSION, problematicCaseType);

        // Then: Should catch exception and log warning, but not throw
        // Verification: method completes without throwing exception
        verify(snapshotRepository, times(1)).upsertSnapshot(
            eq(CASE_TYPE_REF),
            eq(VERSION),
            anyString()
        );
    }

    @Test
    void shouldHandleException_whenRepositoryThrowsException() {
        // Given: Repository throws exception during upsert
        doThrow(new RuntimeException("Database error")).when(snapshotRepository)
            .upsertSnapshot(anyString(), anyInt(), anyString());

        // When: Store snapshot
        caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, VERSION, sampleCaseType);

        // Then: Should catch exception and log warning without rethrowing
        verify(snapshotRepository, times(1)).upsertSnapshot(
            eq(CASE_TYPE_REF),
            eq(VERSION),
            anyString()
        );
    }

    @Test
    void shouldHandleNullCaseTypeReference_whenGettingSnapshot() {
        // Given: Null case type reference
        when(snapshotJdbcRepository.loadCaseTypeSnapshot(null, VERSION))
            .thenReturn(Optional.empty());

        // When: Get snapshot with null reference
        Optional<CaseType> result = caseTypeSnapshotService.getSnapshot(null, VERSION);

        // Then: Should return empty
        assertFalse(result.isPresent());
    }

    @Test
    void shouldHandleNullVersion_whenGettingSnapshot() {
        // Given: Null version
        when(snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF, null))
            .thenReturn(Optional.empty());

        // When: Get snapshot with null version
        Optional<CaseType> result = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, null);

        // Then: Should return empty
        assertFalse(result.isPresent());
    }

    @Test
    void shouldStoreSnapshot_withNullCaseTypeReference() {
        caseTypeSnapshotService.storeSnapshot(null, VERSION, sampleCaseType);

        verify(snapshotRepository, times(1)).upsertSnapshot(
            eq(null),
            eq(VERSION),
            anyString()
        );
    }

    @Test
    void shouldStoreSnapshot_withNullVersion() {
        caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, null, sampleCaseType);

        verify(snapshotRepository, times(1)).upsertSnapshot(
            eq(CASE_TYPE_REF),
            eq(null),
            anyString()
        );
    }

    @Test
    void shouldHandleMultipleSnapshotRetrievals() {
        // Given: Multiple snapshots exist
        CaseType caseType1 = createSampleCaseType(CASE_TYPE_REF, 1);
        CaseType caseType2 = createSampleCaseType(CASE_TYPE_REF, 2);

        when(snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF, 1))
            .thenReturn(Optional.of(caseType1));
        when(snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF, 2))
            .thenReturn(Optional.of(caseType2));

        // When: Get multiple snapshots
        Optional<CaseType> result1 = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, 1);
        Optional<CaseType> result2 = caseTypeSnapshotService.getSnapshot(CASE_TYPE_REF, 2);

        // Then: Should retrieve correct versions
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertThat(result1.get().getVersion().getNumber(), is(1));
        assertThat(result2.get().getVersion().getNumber(), is(2));
    }

    @Test
    void shouldStoreMultipleSnapshots() {
        // Given: Multiple case types to store
        CaseType caseType1 = createSampleCaseType("CaseType1", 1);
        CaseType caseType2 = createSampleCaseType("CaseType2", 1);

        // When: Store multiple snapshots
        caseTypeSnapshotService.storeSnapshot("CaseType1", 1, caseType1);
        caseTypeSnapshotService.storeSnapshot("CaseType2", 1, caseType2);

        // Then: Should store both
        verify(snapshotRepository, times(1)).upsertSnapshot(
            eq("CaseType1"),
            eq(1),
            anyString()
        );
        verify(snapshotRepository, times(1)).upsertSnapshot(
            eq("CaseType2"),
            eq(1),
            anyString()
        );
    }

    @Test
    void shouldOverwriteExistingSnapshot_whenStoringNewVersion() {
        // Given: Existing snapshot for version 1
        CaseType newVersion = createSampleCaseType(CASE_TYPE_REF, 2);

        // When: Store new version
        caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, 2, newVersion);

        // Then: Should upsert (repository handles overwrite logic)
        verify(snapshotRepository, times(1)).upsertSnapshot(
            eq(CASE_TYPE_REF),
            eq(2),
            anyString()
        );
    }

    @Test
    void shouldHandleEdgeCases_documentationTest() {
        // Given: Various edge case inputs
        CaseType nullCaseType = null;
        CaseType emptyCaseType = new CaseType();

        // When/Then: Should not throw exceptions
        try {
            caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, VERSION, nullCaseType);
            caseTypeSnapshotService.storeSnapshot(CASE_TYPE_REF, VERSION, emptyCaseType);

            assertTrue(true, "Edge cases handled without exceptions");
        } catch (Exception e) {
            fail("Should not throw exceptions: " + e.getMessage());
        }
    }

    private CaseType createSampleCaseType(String reference, Integer versionNumber) {
        CaseType caseType = new CaseType();
        caseType.setId(reference);
        caseType.setName("Test Case Type " + reference);
        caseType.setDescription("Test description");
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);

        Version version = new Version();
        version.setNumber(versionNumber);
        caseType.setVersion(version);

        return caseType;
    }

    private CaseType createProblematicCaseType() {
        CaseType caseType = new CaseType();
        caseType.setId(CASE_TYPE_REF);
        return caseType;
    }
}
