package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeSnapshotEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
class CaseTypeSnapshotRepositoryTest {

    private static final String CASE_TYPE_REF_1 = "TestCaseType1";
    private static final String CASE_TYPE_REF_2 = "TestCaseType2";
    private static final String SAMPLE_JSON_V1 = "{\"caseType\":\"TestCaseType1\",\"version\":1,\"fields\":[]}";
    private static final String SAMPLE_JSON_V2 = """
        {
          "caseType": "TestCaseType1",
          "version": 2,
          "fields": [
            {
              "id": "field1"
            }
          ]
        }
        """;

    private static final String SAMPLE_JSON_V3 = """
        {
          "caseType": "TestCaseType1",
          "version": 3,
          "fields": [
            {
              "id": "field1"
            },
            {
              "id": "field2"
            }
          ]
        }
        """;


    @Autowired
    private CaseTypeSnapshotRepository caseTypeSnapshotRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        clearSnapshots();
    }

    private void clearSnapshots() {
        caseTypeSnapshotRepository.deleteAll();
        caseTypeSnapshotRepository.flush();
    }

    @Test
    void shouldInsertNewSnapshot_whenCaseTypeDoesNotExist() throws Exception {
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        Optional<CaseTypeSnapshotEntity> result = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(result.isPresent(), "Snapshot should exist after upsert");
        CaseTypeSnapshotEntity snapshot = result.get();

        assertThat(snapshot.getCaseTypeReference(), is(CASE_TYPE_REF_1));
        assertThat(snapshot.getVersionId(), is(1));
        assertJsonEquals(SAMPLE_JSON_V1, snapshot.getPrecomputedResponse());
        assertThat(snapshot.getCreatedAt(), notNullValue());
        assertThat(snapshot.getLastModified(), notNullValue());
    }

    @Test
    void shouldUpdateExistingSnapshot_whenNewVersionIsHigher() {
        // Given: Existing snapshot with version 1
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        CaseTypeSnapshotEntity initial = findByCaseTypeReference(CASE_TYPE_REF_1)
            .orElseThrow(() -> new AssertionError("Initial snapshot not found"));

        LocalDateTime initialLastModified = initial.getLastModified();
        Integer initialId = initial.getId();

        // When: Update with version 2
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);

        // Then: Use Awaitility to wait for the DB to reflect the change
        await()
            .atMost(2, SECONDS)
            .untilAsserted(() -> {
                CaseTypeSnapshotEntity snapshot = findByCaseTypeReference(CASE_TYPE_REF_1)
                    .orElseThrow(() -> new AssertionError("Updated snapshot not found"));

                assertThat("Snapshot ID should remain the same (upsert)", snapshot.getId(), is(initialId));
                assertThat("Version should be updated", snapshot.getVersionId(), is(2));
                assertThat("Timestamp should have increased",
                    snapshot.getLastModified(), greaterThan(initialLastModified));
                assertJsonEquals(SAMPLE_JSON_V2, snapshot.getPrecomputedResponse());
            });
    }

    @Test
    void shouldNotUpdateExistingSnapshot_whenNewVersionIsLower() throws Exception {
        // Given: Existing snapshot with version 3
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 3, SAMPLE_JSON_V3);

        Optional<CaseTypeSnapshotEntity> initial = findByCaseTypeReference(CASE_TYPE_REF_1);
        assertTrue(initial.isPresent());

        // When: Attempt to update with version 2 (lower)
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);

        // Then: Snapshot should NOT be updated
        Optional<CaseTypeSnapshotEntity> unchanged = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(unchanged.isPresent());
        CaseTypeSnapshotEntity snapshot = unchanged.get();

        assertThat(snapshot.getVersionId(), is(3)); // Still version 3
        assertJsonEquals(SAMPLE_JSON_V3, snapshot.getPrecomputedResponse()); // Still old JSON

        LocalDateTime initialLastModified = initial.get().getLastModified();
        assertThat(snapshot.getLastModified(), is(initialLastModified)); // Timestamp unchanged
    }

    @Test
    void shouldNotUpdateExistingSnapshot_whenNewVersionIsEqual() throws Exception {
        // Given: Existing snapshot with version 2
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);

        Optional<CaseTypeSnapshotEntity> initial = findByCaseTypeReference(CASE_TYPE_REF_1);
        assertTrue(initial.isPresent());

        // When: Attempt to update with same version 2
        String newJson = "{\"caseType\":\"TestCaseType1\",\"version\":2,\"fields\":[{\"id\":\"newField\"}]}";
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, newJson);

        // Then: Snapshot should NOT be updated
        Optional<CaseTypeSnapshotEntity> unchanged = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(unchanged.isPresent());
        CaseTypeSnapshotEntity snapshot = unchanged.get();

        assertThat(snapshot.getVersionId(), is(2));

        LocalDateTime initialLastModified = initial.get().getLastModified();
        String initialJson = initial.get().getPrecomputedResponse();

        assertJsonEquals(initialJson, snapshot.getPrecomputedResponse()); // Old JSON preserved
        assertThat(snapshot.getLastModified(), is(initialLastModified)); // Timestamp unchanged
    }

    @Test
    void shouldHandleMultipleCaseTypes_independently() {
        // Given & When: Insert snapshots for different case types
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_2, 1, SAMPLE_JSON_V1);

        // Then: Both should exist independently
        Optional<CaseTypeSnapshotEntity> snapshot1 = findByCaseTypeReference(CASE_TYPE_REF_1);
        Optional<CaseTypeSnapshotEntity> snapshot2 = findByCaseTypeReference(CASE_TYPE_REF_2);

        assertTrue(snapshot1.isPresent());
        assertTrue(snapshot2.isPresent());

        assertNotEquals(snapshot1.get().getId(), snapshot2.get().getId());
        assertThat(snapshot1.get().getCaseTypeReference(), is(CASE_TYPE_REF_1));
        assertThat(snapshot2.get().getCaseTypeReference(), is(CASE_TYPE_REF_2));
    }

    @Test
    void shouldHandleComplexJsonStructure() throws Exception {
        // Given: Complex JSON with nested structures
        String complexJson = """
            {
                "caseType": "ComplexCase",
                "version": 1,
                "fields": [
                    {
                        "id": "field1",
                        "type": "Text",
                        "label": "Field 1",
                        "validation": {
                            "required": true,
                            "maxLength": 100
                        }
                    },
                    {
                        "id": "field2",
                        "type": "Collection",
                        "items": [
                            {"subField1": "value1"},
                            {"subField2": "value2"}
                        ]
                    }
                ],
                "states": ["Draft", "Submitted", "Completed"]
            }
            """;

        // When: Insert complex JSON
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, complexJson);

        // Then: Complex JSON should be stored and retrieved correctly (semantically equal)
        Optional<CaseTypeSnapshotEntity> result = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(result.isPresent());
        assertJsonEquals(complexJson, result.get().getPrecomputedResponse());
    }

    @Test
    void shouldHandleSequentialVersionUpdates() throws Exception {
        // Given: Initial snapshot
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        // When: Sequential updates with increasing versions
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 3, SAMPLE_JSON_V3);

        // Then: Final version should be persisted
        Optional<CaseTypeSnapshotEntity> result = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(result.isPresent());
        assertThat(result.get().getVersionId(), is(3));
        assertJsonEquals(SAMPLE_JSON_V3, result.get().getPrecomputedResponse());
    }

    @Test
    void shouldHandleEmptyJsonObject() throws Exception {
        // Given: Empty JSON object
        String emptyJson = "{}";

        // When: Insert empty JSON
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, emptyJson);

        // Then: Empty JSON should be stored
        Optional<CaseTypeSnapshotEntity> result = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(result.isPresent());
        assertJsonEquals(emptyJson, result.get().getPrecomputedResponse());
    }

    @Test
    void shouldHandleJsonWithSpecialCharacters() throws Exception {
        // Given: JSON with special characters
        String specialJson = "{\"field\":\"Value with 'quotes' and \\\"escaped\\\" characters\"}";

        // When: Insert JSON with special characters
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, specialJson);

        // Then: Special characters should be preserved
        Optional<CaseTypeSnapshotEntity> result = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(result.isPresent());
        assertJsonEquals(specialJson, result.get().getPrecomputedResponse());
    }

    @Test
    void shouldMaintainCreatedAtTimestamp_acrossUpdates() {
        // Given: Initial snapshot
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        Optional<CaseTypeSnapshotEntity> initial = findByCaseTypeReference(CASE_TYPE_REF_1);
        assertTrue(initial.isPresent());
        LocalDateTime originalCreatedAt = initial.get().getCreatedAt();

        // When: Update to version 2
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);

        // Then: created_at should remain unchanged
        Optional<CaseTypeSnapshotEntity> updated = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(updated.isPresent());
        assertThat(updated.get().getCreatedAt(), is(originalCreatedAt));
    }

    private Optional<CaseTypeSnapshotEntity> findByCaseTypeReference(String caseTypeReference) {
        return caseTypeSnapshotRepository.findAll()
            .stream()
            .filter(snapshot -> snapshot.getCaseTypeReference().equals(caseTypeReference))
            .findFirst();
    }

    private void assertJsonEquals(String expectedJson, String actualJson) throws Exception {
        JsonNode expectedNode = objectMapper.readTree(expectedJson);
        JsonNode actualNode = objectMapper.readTree(actualJson);
        assertEquals(expectedNode, actualNode,
            "JSON objects should be semantically equal despite formatting differences");
    }

    @Test
    void shouldReturnTrue_whenSnapshotExists() {
        // Given: Snapshot exists for case type with version 1
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        // When: Check if snapshot exists
        boolean exists = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1,1);

        // Then: Should return true
        assertTrue(exists, "Should return true when snapshot exists for given case type and version");
    }

    @Test
    void shouldReturnFalse_whenSnapshotDoesNotExist() {
        // Given: No snapshots in database

        // When: Check if snapshot exists
        boolean exists = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 1);

        // Then: Should return false
        assertThat(exists, is(false));
    }

    @Test
    void shouldReturnFalse_whenCaseTypeExistsButVersionDiffers() {
        // Given: Snapshot exists for case type with version 1
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        // When: Check for different version
        boolean exists = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 2);

        // Then: Should return false
        assertThat(exists, is(false));
    }

    @Test
    void shouldReturnFalse_whenVersionExistsButCaseTypeDiffers() {
        // Given: Snapshot exists for CASE_TYPE_REF_1 with version 1
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        // When: Check for different case type with same version
        boolean exists = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_2, 1);

        // Then: Should return false
        assertThat(exists, is(false));
    }

    @Test
    void shouldReturnTrue_whenSnapshotExistsAfterUpdate() {
        // Given: Initial snapshot with version 1
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        // When: Update to version 2
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);

        // Then: Should return true for version 2 (updated version)
        boolean existsV2 = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 2);
        assertTrue(existsV2, "Should return true for updated version");

        // And: Should return false for old version 1 (replaced)
        boolean existsV1 = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 1);
        assertThat(existsV1, is(false));
    }

    @Test
    void shouldReturnTrue_afterFailedUpdateWithLowerVersion() {
        // Given: Snapshot exists with version 3
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 3, SAMPLE_JSON_V3);

        // When: Attempt to update with lower version 2 (should not update)
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);

        // Then: Should return true for version 3 (original version preserved)
        boolean existsV3 = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 3);
        assertTrue(existsV3, "Should return true for original version after failed update");

        // And: Should return false for version 2 (update was rejected)
        boolean existsV2 = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 2);
        assertThat(existsV2, is(false));
    }

    @Test
    void shouldHandleMultipleCaseTypes_existenceChecksIndependently() {
        // Given: Snapshots exist for two different case types
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_2, 2, SAMPLE_JSON_V2);

        // Then: Existence checks should be independent
        assertTrue(caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 1));
        assertTrue(caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_2, 2));

        assertThat(caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 2),
            is(false));
        assertThat(caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_2, 1),
            is(false));
    }

    @Test
    void shouldReturnFalse_forNullCaseTypeReference() {
        // Given: Snapshot exists
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        // When: Check with null case type reference
        boolean exists = caseTypeSnapshotRepository
            .existsByCaseTypeReferenceAndVersionId(null,1);

        // Then: Should return false
        assertThat(exists, is(false));
    }

    @Test
    void shouldReturnFalse_forNullVersion() {
        // Given: Snapshot exists
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        // When: Check with null version
        boolean exists = caseTypeSnapshotRepository
            .existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, null);

        // Then: Should return false
        assertThat(exists, is(false));
    }

    @Test
    void shouldHandleRapidSuccessiveChecks() {
        // Given: Snapshot exists
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        // When: Multiple rapid existence checks
        boolean check1 = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 1);
        boolean check2 = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 1);
        boolean check3 = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 1);

        // Then: All should return true consistently
        assertTrue(check1);
        assertTrue(check2);
        assertTrue(check3);
    }

    @Test
    void shouldReturnCorrectResult_afterDeletion() {
        // Given: Snapshot exists
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        boolean existsBeforeDelete = caseTypeSnapshotRepository
            .existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 1);
        assertTrue(existsBeforeDelete);

        // When: Delete all snapshots
        clearSnapshots();

        // Then: Should return false after deletion
        boolean existsAfterDelete = caseTypeSnapshotRepository
            .existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 1);
        assertThat(existsAfterDelete, is(false));
    }

    @Test
    void shouldHandleSequentialVersionChecks() {
        // Given: Create snapshots with sequential versions
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 3, SAMPLE_JSON_V3);

        // Then: Only the latest version should exist
        assertThat(caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 1),
            is(false));
        assertThat(caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 2),
            is(false));
        assertTrue(caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, 3));
    }

    @Test
    void shouldHandleCaseTypeReferenceWithSpecialCharacters() {
        // Given: Case type reference with special characters
        String specialCaseType = "Test-Case_Type.With#Special$Chars";
        caseTypeSnapshotRepository.upsertSnapshot(specialCaseType, 1, SAMPLE_JSON_V1);

        // When: Check existence
        boolean exists = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(specialCaseType, 1);

        // Then: Should return true
        assertTrue(exists, "Should handle case type references with special characters");
    }

    @Test
    void shouldHandleVeryLargeCaseTypeReference() {
        // Given: Very long case type reference (within database limits)
        String longCaseType = "A".repeat(70);
        caseTypeSnapshotRepository.upsertSnapshot(longCaseType, 1, SAMPLE_JSON_V1);

        // When: Check existence
        boolean exists = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(longCaseType, 1);

        // Then: Should return true
        assertTrue(exists, "Should handle long case type references");
    }

    @Test
    void shouldHandleHighVersionNumbers() {
        // Given: Snapshot with very high version number
        Integer highVersion = Integer.MAX_VALUE;
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, highVersion, SAMPLE_JSON_V1);

        // When: Check existence
        boolean exists = caseTypeSnapshotRepository.existsByCaseTypeReferenceAndVersionId(CASE_TYPE_REF_1, highVersion);

        // Then: Should return true
        assertTrue(exists, "Should handle high version numbers");
    }
}
