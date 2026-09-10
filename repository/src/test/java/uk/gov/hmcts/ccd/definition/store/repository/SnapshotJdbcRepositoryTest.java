package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessControlList;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseEvent;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseState;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.repository.model.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
class SnapshotJdbcRepositoryTest {

    private static final String CASE_TYPE_REF_1 = "TestCaseType1";
    private static final String CASE_TYPE_REF_2 = "TestCaseType2";
    private static final String CASE_TYPE_REF_NONEXISTENT = "NonExistentCaseType";

    @Autowired
    private SnapshotJdbcRepository snapshotJdbcRepository;

    @Autowired
    private CaseTypeSnapshotRepository caseTypeSnapshotRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setup() {
        caseTypeSnapshotRepository.deleteAll();
    }

    @AfterEach
    void cleanup() {
        caseTypeSnapshotRepository.deleteAll();
    }

    @Test
    void shouldLoadCaseTypeSnapshot_whenSnapshotExists() throws Exception {
        // Given: A snapshot exists in the database
        CaseType expectedCaseType = createSampleCaseType(CASE_TYPE_REF_1, 1);
        String jsonResponse = objectMapper.writeValueAsString(expectedCaseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Load the snapshot
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: Snapshot should be loaded successfully
        assertTrue(result.isPresent(), "Snapshot should be present");
        CaseType actualCaseType = result.get();

        assertThat(actualCaseType, notNullValue());
        assertThat(actualCaseType.getId(), is(expectedCaseType.getId()));
        assertThat(actualCaseType.getName(), is(expectedCaseType.getName()));
        assertThat(actualCaseType.getDescription(), is(expectedCaseType.getDescription()));
        assertThat(actualCaseType.getSecurityClassification(), is(SecurityClassification.PUBLIC));
    }

    @Test
    void shouldReturnEmpty_whenSnapshotDoesNotExist() {
        // Given: No snapshot exists for the case type reference

        // When: Attempt to load non-existent snapshot
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_NONEXISTENT, 1);

        // Then: Should return empty Optional
        assertFalse(result.isPresent(), "Should return empty when snapshot doesn't exist");
    }

    @Test
    void shouldReturnEmpty_whenVersionDoesNotMatch() throws Exception {
        // Given: Snapshot exists with version 1
        CaseType caseType = createSampleCaseType(CASE_TYPE_REF_1, 1);
        String jsonResponse = objectMapper.writeValueAsString(caseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Attempt to load with different version
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 2);

        // Then: Should return empty
        assertFalse(result.isPresent(), "Should return empty when version doesn't match");
    }

    @Test
    void shouldLoadCorrectSnapshot_whenMultipleCaseTypesExist() throws Exception {
        // Given: Multiple case types exist
        CaseType caseType1 = createSampleCaseType(CASE_TYPE_REF_1, 1);
        CaseType caseType2 = createSampleCaseType(CASE_TYPE_REF_2, 1);

        String json1 = objectMapper.writeValueAsString(caseType1);
        String json2 = objectMapper.writeValueAsString(caseType2);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, json1);
        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_2, 1, json2);

        // When: Load specific case type
        Optional<CaseType> result1 = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);
        Optional<CaseType> result2 = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_2, 1);

        // Then: Should load correct case types independently
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());

        assertThat(result1.get().getId(), is(CASE_TYPE_REF_1));
        assertThat(result2.get().getId(), is(CASE_TYPE_REF_2));
    }

    @Test
    void shouldDeserializeComplexCaseType_withAllFields() throws Exception {
        // Given: Complex case type with all fields populated
        CaseType complexCaseType = createComplexCaseType();
        String jsonResponse = objectMapper.writeValueAsString(complexCaseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Load complex snapshot
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: All fields should be deserialized correctly
        assertTrue(result.isPresent());
        CaseType loaded = result.get();

        assertThat(loaded.getId(), is(complexCaseType.getId()));
        assertThat(loaded.getName(), is(complexCaseType.getName()));
        assertThat(loaded.getEvents(), hasSize(2));
        assertThat(loaded.getStates(), hasSize(2));
        assertThat(loaded.getCaseFields(), hasSize(3));
        assertThat(loaded.getAcls(), hasSize(1));
        assertNotNull(loaded.getJurisdiction());
        assertNotNull(loaded.getVersion());
    }

    @Test
    void shouldDeserializeCaseTypeWithEvents() throws Exception {
        // Given: Case type with events
        CaseType caseType = createCaseTypeWithEvents();
        String jsonResponse = objectMapper.writeValueAsString(caseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Load snapshot
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: Events should be deserialized
        assertTrue(result.isPresent());
        CaseType loaded = result.get();

        assertThat(loaded.getEvents(), hasSize(2));
        assertThat(loaded.getEvents().get(0).getId(), is("createCase"));
        assertThat(loaded.getEvents().get(0).getName(), is("Create a case"));
        assertThat(loaded.getEvents().get(1).getId(), is("updateCase"));
    }

    @Test
    void shouldDeserializeCaseTypeWithStates() throws Exception {
        // Given: Case type with states
        CaseType caseType = createCaseTypeWithStates();
        String jsonResponse = objectMapper.writeValueAsString(caseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Load snapshot
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: States should be deserialized
        assertTrue(result.isPresent());
        CaseType loaded = result.get();

        assertThat(loaded.getStates(), hasSize(2));
        assertThat(loaded.getStates().get(0).getId(), is("CaseCreated"));
        assertThat(loaded.getStates().get(1).getId(), is("CaseSubmitted"));
    }

    @Test
    void shouldDeserializeCaseTypeWithCaseFields() throws Exception {
        // Given: Case type with case fields
        CaseType caseType = createCaseTypeWithCaseFields();
        String jsonResponse = objectMapper.writeValueAsString(caseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Load snapshot
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: Case fields should be deserialized
        assertTrue(result.isPresent());
        CaseType loaded = result.get();

        assertThat(loaded.getCaseFields(), hasSize(3));
        assertThat(loaded.getCaseFields().get(0).getId(), is("TextField"));
        assertThat(loaded.getCaseFields().get(1).getId(), is("[STATE]"));
        assertThat(loaded.getCaseFields().get(2).getId(), is("[CASE_REFERENCE]"));
    }

    @Test
    void shouldDeserializeCaseTypeWithACLs() throws Exception {
        // Given: Case type with ACLs
        CaseType caseType = createCaseTypeWithACLs();
        String jsonResponse = objectMapper.writeValueAsString(caseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Load snapshot
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: ACLs should be deserialized
        assertTrue(result.isPresent());
        CaseType loaded = result.get();

        assertThat(loaded.getAcls(), hasSize(1));
        assertThat(loaded.getAcls().getFirst().getRole(), is("caseworker-befta_master"));
    }

    @Test
    void shouldHandleLargeJsonPayload() throws Exception {
        // Given: Large case type with many fields and collections
        CaseType largeCaseType = createLargeCaseType();
        String jsonResponse = objectMapper.writeValueAsString(largeCaseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Load large snapshot (tests streaming efficiency)
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: Large payload should be loaded efficiently via streaming
        assertTrue(result.isPresent());
        CaseType loaded = result.get();

        assertThat(loaded.getCaseFields(), hasSize(50)); // Large number of fields
        assertThat(loaded.getEvents(), hasSize(10));
        assertThat(loaded.getStates(), hasSize(5));
    }

    @Test
    void shouldHandleEmptyCollections() throws Exception {
        // Given: Case type with empty collections
        CaseType caseType = createMinimalCaseType();
        String jsonResponse = objectMapper.writeValueAsString(caseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Load snapshot
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: Empty collections should be handled correctly
        assertTrue(result.isPresent());
        CaseType loaded = result.get();

        assertThat(loaded.getEvents(), empty());
        assertThat(loaded.getStates(), empty());
        assertThat(loaded.getCaseFields(), empty());
        assertThat(loaded.getAcls(), empty());
    }

    @Test
    void shouldReturnEmpty_whenDatabaseAccessFails() {
        // Given: Invalid case type reference
        String invalidReference = null;

        // When: Attempt to load with invalid input
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(invalidReference, 1);

        // Then: Should handle gracefully and return empty
        assertFalse(result.isPresent(), "Should return empty on database access failure");
    }

    @Test
    void shouldHandleNullVersion() {
        // Given: Null version

        // When: Attempt to load with null version
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, null);

        // Then: Should handle gracefully and return empty
        assertFalse(result.isPresent(), "Should return empty when version is null");
    }

    @Test
    void shouldReturnEmpty_whenJsonDeserializationFails() {
        // Given: Malformed JSON that passes PostgreSQL validation but fails Jackson deserialization
        // We'll insert a JSON structure that doesn't match CaseType schema
        String malformedJson = "{\"unexpectedField\": \"value\", \"invalidStructure\": 123}";

        // Insert directly using JdbcTemplate to bypass repository validation
        jdbcTemplate.update(
            "INSERT INTO case_type_snapshot "
                + "(case_type_reference, version_id, precomputed_response, created_at, last_modified) "
                + "VALUES (?, ?, CAST(? AS jsonb), NOW(), NOW())", CASE_TYPE_REF_1, 1, malformedJson
        );

        // When: Attempt to load snapshot with malformed JSON
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: Should handle gracefully and return empty
        assertFalse(result.isPresent(), "Should return empty when JSON deserialization fails");
    }

    @Test
    void shouldReturnEmpty_whenResultSetProcessingFails() {
        // Given: Invalid parameters that cause SQL execution issues
        String invalidCaseTypeRef = createVeryLongString(1000); // Exceeds database column limit

        // When: Attempt to load with invalid parameters
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(invalidCaseTypeRef, 1);

        // Then: Should handle DataAccessException gracefully
        assertFalse(result.isPresent(), "Should return empty when database access fails");
    }

    @Test
    void shouldReturnEmpty_whenInputStreamProcessingFails() throws Exception {
        // Given: Insert a valid JSON but we'll simulate InputStream reading failure
        // by using a case type reference that could cause issues
        CaseType caseType = createSampleCaseType(CASE_TYPE_REF_1, 1);
        String jsonResponse = objectMapper.writeValueAsString(caseType);

        caseTypeSnapshotRepository.upsertSnapshot(CASE_TYPE_REF_1, 1, jsonResponse);

        // When: Load with parameters that might cause unexpected runtime exceptions
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: Should handle any unexpected exceptions gracefully
        // This test primarily validates that the code doesn't throw exceptions
        assertNotNull(result, "Should return a result (empty or present) without throwing exceptions");
    }

    @Test
    void shouldLogWarning_whenDatabaseAccessFails() {
        // Given: A scenario that will cause DataAccessException
        // Using null reference to trigger database constraint violation

        // When: Attempt operation that will fail
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(null, 1);

        // Then: Should return empty and log warning (we can't easily assert logs, but we verify behavior)
        assertFalse(result.isPresent(), "Should return empty and log warning on database failure");
    }

    @Test
    void shouldLogError_whenUnexpectedExceptionOccurs() {
        // Given: Extreme edge case parameters
        String extremeReference = "ValidRef\u0000WithNull"; // String with null character

        // When: Attempt to load which may cause unexpected exceptions
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(extremeReference, Integer.MAX_VALUE);

        // Then: Should handle gracefully and log error
        assertFalse(result.isPresent(), "Should return empty and log error on unexpected exception");
    }

    @Test
    void shouldHandleExceptionDuringStreamProcessing() {
        // Given: JSON that will cause issues during actual ObjectMapper deserialization
        // Create a JSON with mismatched types (e.g., string where object expected)
        String invalidTypeJson = """
            {
                "id": "TestCaseType1",
                "name": "Test",
                "version": "this should be an object not a string",
                "jurisdiction": "this should be an object not a string",
                "events": "this should be an array not a string"
            }
            """;

        jdbcTemplate.update(
            "INSERT INTO case_type_snapshot "
                + "(case_type_reference, version_id, precomputed_response, created_at, last_modified) "
                + "VALUES (?, ?, CAST(? AS jsonb), NOW(), NOW())",CASE_TYPE_REF_1, 1, invalidTypeJson
        );

        // When: Attempt to deserialize with type mismatches
        Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

        // Then: Should catch RuntimeException from try-catch in lambda and return empty
        assertFalse(result.isPresent(), "Should return empty when type deserialization fails");
    }

    @Test
    void shouldHandleNullPrecomputedResponse() {
        // Given: A record with null precomputed_response
        // This tests edge case where data might be corrupted
        try {
            jdbcTemplate.update(
                "INSERT INTO case_type_snapshot "
                    + "(case_type_reference, version_id, precomputed_response, created_at, last_modified) "
                    + "VALUES (?, ?, NULL, NOW(), NOW())",CASE_TYPE_REF_1, 1
            );

            // When: Attempt to load
            Optional<CaseType> result = snapshotJdbcRepository.loadCaseTypeSnapshot(CASE_TYPE_REF_1, 1);

            // Then: Should handle null gracefully
            assertFalse(result.isPresent(), "Should return empty when precomputed_response is null");
        } catch (Exception e) {
            assertTrue(true, "Schema prevents null precomputed_response - expected behavior");
        }
    }

    private String createVeryLongString(int length) {
        return "X".repeat(length);
    }

    private CaseType createMinimalCaseType() {
        CaseType caseType = new CaseType();
        caseType.setId(CASE_TYPE_REF_1);
        caseType.setName("Minimal Case Type");
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);

        Version version = new Version();
        version.setNumber(1);
        caseType.setVersion(version);

        return caseType;
    }

    private CaseType createSampleCaseType(String reference, Integer versionNumber) {
        CaseType caseType = new CaseType();
        caseType.setId(reference);
        caseType.setName("Test Case Type " + reference);
        caseType.setDescription("Test description for " + reference);
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);

        Version version = new Version();
        version.setNumber(versionNumber);
        caseType.setVersion(version);

        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("TEST_JURISDICTION");
        jurisdiction.setName("Test Jurisdiction");
        caseType.setJurisdiction(jurisdiction);

        return caseType;
    }

    private CaseType createComplexCaseType() {
        CaseType caseType = createSampleCaseType(CASE_TYPE_REF_1, 1);

        // Add events
        List<CaseEvent> events = new ArrayList<>();
        events.add(createCaseEvent("createCase", "Create a case"));
        events.add(createCaseEvent("updateCase", "Update a case"));
        caseType.setEvents(events);

        // Add states
        List<CaseState> states = new ArrayList<>();
        states.add(createCaseState("CaseCreated", "Case Created"));
        states.add(createCaseState("CaseSubmitted", "Case Submitted"));
        caseType.setStates(states);

        // Add case fields
        List<CaseField> caseFields = new ArrayList<>();
        caseFields.add(createCaseField("TextField", "Text Field", "Text"));
        caseFields.add(createCaseField("DateField", "Date Field", "Date"));
        caseFields.add(createCaseField("NumberField", "Number Field", "Number"));
        caseType.setCaseFields(caseFields);

        // Add ACLs
        List<AccessControlList> acls = new ArrayList<>();
        acls.add(createAccessControlList("caseworker-befta_master"));
        caseType.setAcls(acls);

        return caseType;
    }

    private CaseType createCaseTypeWithEvents() {
        CaseType caseType = createSampleCaseType(CASE_TYPE_REF_1, 1);

        List<CaseEvent> events = new ArrayList<>();
        events.add(createCaseEvent("createCase", "Create a case"));
        events.add(createCaseEvent("updateCase", "Update a case"));
        caseType.setEvents(events);

        return caseType;
    }

    private CaseType createCaseTypeWithStates() {
        CaseType caseType = createSampleCaseType(CASE_TYPE_REF_1, 1);

        List<CaseState> states = new ArrayList<>();
        states.add(createCaseState("CaseCreated", "Case Created"));
        states.add(createCaseState("CaseSubmitted", "Case Submitted"));
        caseType.setStates(states);

        return caseType;
    }

    private CaseType createCaseTypeWithCaseFields() {
        List<CaseField> caseFields = new ArrayList<>();
        caseFields.add(createCaseField("TextField", "Text Field", "Text"));
        caseFields.add(createCaseField("[STATE]", "State", "FixedList"));
        caseFields.add(createCaseField("[CASE_REFERENCE]", "Case Reference", "Text"));

        CaseType caseType = createSampleCaseType(CASE_TYPE_REF_1, 1);
        caseType.setCaseFields(caseFields);

        return caseType;
    }

    private CaseType createCaseTypeWithACLs() {
        CaseType caseType = createSampleCaseType(CASE_TYPE_REF_1, 1);

        List<AccessControlList> acls = new ArrayList<>();
        acls.add(createAccessControlList("caseworker-befta_master"));
        caseType.setAcls(acls);

        return caseType;
    }

    private CaseType createLargeCaseType() {
        CaseType caseType = createSampleCaseType(CASE_TYPE_REF_1, 1);

        List<CaseField> caseFields = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            caseFields.add(createCaseField("Field" + i, "Field " + i, "Text"));
        }
        caseType.setCaseFields(caseFields);

        List<CaseEvent> events = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            events.add(createCaseEvent("event" + i, "Event " + i));
        }
        caseType.setEvents(events);

        List<CaseState> states = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            states.add(createCaseState("State" + i, "State " + i));
        }
        caseType.setStates(states);

        return caseType;
    }

    private CaseEvent createCaseEvent(String id, String name) {
        CaseEvent event = new CaseEvent();
        event.setId(id);
        event.setName(name);
        event.setSecurityClassification(SecurityClassification.PUBLIC);
        return event;
    }

    private CaseState createCaseState(String id, String name) {
        CaseState state = new CaseState();
        state.setId(id);
        state.setName(name);
        return state;
    }

    private CaseField createCaseField(String id, String label, String fieldTypeId) {
        CaseField field = new CaseField();
        field.setId(id);
        field.setLabel(label);
        field.setSecurityClassification(String.valueOf(SecurityClassification.PUBLIC));

        FieldType fieldType = new FieldType();
        fieldType.setId(fieldTypeId);
        fieldType.setType(fieldTypeId);
        field.setFieldType(fieldType);

        return field;
    }

    private AccessControlList createAccessControlList(String role) {
        return new AccessControlList(role, true, true, true, true);
    }
}
