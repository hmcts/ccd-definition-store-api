package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
    private static final String SAMPLE_JSON_V2 = "{\"caseType\":\"TestCaseType1\",\"version\":2,\"fields\":[{\"id\":\"field1\"}]}";
    private static final String SAMPLE_JSON_V3 = "{\"caseType\":\"TestCaseType1\",\"version\":3,\"fields\":[{\"id\":\"field1\"},{\"id\":\"field2\"}]}";

    @Autowired
    private CaseTypeSnapshotRepository caseTypeSnapshotRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        caseTypeSnapshotRepository.deleteAll();
        caseTypeSnapshotRepository.flush();
    }

    @AfterEach
    void cleanup() {
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
    void shouldUpdateExistingSnapshot_whenNewVersionIsHigher() throws Exception {
        // Given: Existing snapshot with version 1
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, SAMPLE_JSON_V1);

        Optional<CaseTypeSnapshotEntity> initial = findByCaseTypeReference(CASE_TYPE_REF_1);
        assertTrue(initial.isPresent());
        LocalDateTime initialLastModified = initial.get().getLastModified();
        Integer initialId = initial.get().getId();

        // Small delay to ensure timestamp difference
        Thread.sleep(100);

        // When: Update with version 2
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);

        // Then: Snapshot should be updated
        Optional<CaseTypeSnapshotEntity> updated = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(updated.isPresent());
        CaseTypeSnapshotEntity snapshot = updated.get();

        assertThat(snapshot.getId(), is(initialId)); // Same record
        assertThat(snapshot.getCaseTypeReference(), is(CASE_TYPE_REF_1));
        assertThat(snapshot.getVersionId(), is(2));
        assertJsonEquals(SAMPLE_JSON_V2, snapshot.getPrecomputedResponse());
        assertThat(snapshot.getLastModified(), greaterThan(initialLastModified));
    }

    @Test
    void shouldNotUpdateExistingSnapshot_whenNewVersionIsLower() throws Exception {
        // Given: Existing snapshot with version 3
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 3, SAMPLE_JSON_V3);

        Optional<CaseTypeSnapshotEntity> initial = findByCaseTypeReference(CASE_TYPE_REF_1);
        assertTrue(initial.isPresent());
        LocalDateTime initialLastModified = initial.get().getLastModified();

        // When: Attempt to update with version 2 (lower)
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);

        // Then: Snapshot should NOT be updated
        Optional<CaseTypeSnapshotEntity> unchanged = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(unchanged.isPresent());
        CaseTypeSnapshotEntity snapshot = unchanged.get();

        assertThat(snapshot.getVersionId(), is(3)); // Still version 3
        assertJsonEquals(SAMPLE_JSON_V3, snapshot.getPrecomputedResponse()); // Still old JSON
        assertThat(snapshot.getLastModified(), is(initialLastModified)); // Timestamp unchanged
    }

    @Test
    void shouldNotUpdateExistingSnapshot_whenNewVersionIsEqual() throws Exception {
        // Given: Existing snapshot with version 2
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, SAMPLE_JSON_V2);

        Optional<CaseTypeSnapshotEntity> initial = findByCaseTypeReference(CASE_TYPE_REF_1);
        assertTrue(initial.isPresent());
        LocalDateTime initialLastModified = initial.get().getLastModified();
        String initialJson = initial.get().getPrecomputedResponse();

        // When: Attempt to update with same version 2
        String newJson = "{\"caseType\":\"TestCaseType1\",\"version\":2,\"fields\":[{\"id\":\"newField\"}]}";
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 2, newJson);

        // Then: Snapshot should NOT be updated
        Optional<CaseTypeSnapshotEntity> unchanged = findByCaseTypeReference(CASE_TYPE_REF_1);

        assertTrue(unchanged.isPresent());
        CaseTypeSnapshotEntity snapshot = unchanged.get();

        assertThat(snapshot.getVersionId(), is(2));
        assertJsonEquals(initialJson, snapshot.getPrecomputedResponse()); // Old JSON preserved
        assertThat(snapshot.getLastModified(), is(initialLastModified)); // Timestamp unchanged
    }

    @Test
    void shouldHandleMultipleCaseTypes_independently() throws Exception {
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
    void shouldMaintainCreatedAtTimestamp_acrossUpdates() throws Exception {
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

    /**
     * Helper method to find snapshot by case type reference
     */
    private Optional<CaseTypeSnapshotEntity> findByCaseTypeReference(String caseTypeReference) {
        return caseTypeSnapshotRepository.findAll()
            .stream()
            .filter(snapshot -> snapshot.getCaseTypeReference().equals(caseTypeReference))
            .findFirst();
    }

    /**
     * Compare JSON semantically rather than string comparison
     * PostgreSQL jsonb normalizes JSON (removes whitespace, reorders keys)
     */
    private void assertJsonEquals(String expectedJson, String actualJson) throws Exception {
        JsonNode expectedNode = objectMapper.readTree(expectedJson);
        JsonNode actualNode = objectMapper.readTree(actualJson);
        assertEquals(expectedNode, actualNode,
            "JSON objects should be semantically equal despite formatting differences");
    }
}
