package uk.gov.hmcts.net.ccd.definition.store.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


/**
 * End-to-end integration test for Case Type Snapshot feature.
 * Tests the complete flow:
 * 1. Import definition file
 * 2. Async snapshot creation
 * 3. GET request retrieves case type
 * 4. Snapshot cached in database
 * 5. Subsequent requests use cached snapshot
 */
@Sql(
    statements = "DELETE FROM case_type_snapshot",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(
        transactionMode = SqlConfig.TransactionMode.ISOLATED,
        transactionManager = "transactionManager"
    )
)
class CaseTypeSnapshotIT extends BaseTest {

    private static final String CASE_TYPE_URL = "/api/data/case-type/%s";
    private static final String CASE_TYPE_BY_USER_URL = "/api/data/caseworkers/%s/jurisdictions/%s/case-types/%s";
    private static final String TEST_CASE_TYPE = "TestAddressBookCase";
    private static final String TEST_JURISDICTION = "TEST";
    private static final String TEST_USER_ID = "user1@hmcts.net";

    @Test
    void shouldCreateSnapshotAfterImportAndServeFromCache() throws Exception {
        // STEP 1: Import definition file
        try (final InputStream inputStream = getClass().getResourceAsStream(EXCEL_FILE_CCD_DEFINITION)) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            MvcResult importResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(importResult, HttpStatus.SC_CREATED);
        }

        // STEP 2: Wait for async snapshot creation to complete
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Integer snapshotCount = getSnapshotCount();
                assertNotNull(snapshotCount);
                assertTrue(snapshotCount > 0, "Snapshot should be created for " + TEST_CASE_TYPE);
            });

        // STEP 3: Verify snapshot was created with correct version
        Integer actualCaseTypeVersion = getActualCaseTypeVersion();
        Integer snapshotVersion = getSnapshotVersion();

        assertNotNull(snapshotVersion, "Version ID should not be null");
        assertEquals(actualCaseTypeVersion, snapshotVersion,
            "Snapshot version should match the actual case type version");

        // STEP 4: First GET request (should use snapshot)
        final String caseTypeUrl = String.format(CASE_TYPE_URL, TEST_CASE_TYPE);
        MvcResult firstGetResult = mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_CASE_TYPE))
            .andReturn();

        final CaseType caseType = getCaseType(firstGetResult);

        assertNotNull(caseType);
        assertEquals(TEST_CASE_TYPE, caseType.getId());
        assertNotNull(caseType.getEvents());
        assertFalse(caseType.getEvents().isEmpty());

        // STEP 5: Second GET request (should also use snapshot, potentially faster)
        MvcResult secondGetResult = mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_CASE_TYPE))
            .andReturn();

        assertEquals(
            firstGetResult.getResponse().getContentAsString(),
            secondGetResult.getResponse().getContentAsString(),
            "Both responses should be identical"
        );

        // STEP 6: Verify snapshot JSON contains expected data
        String snapshotJson = jdbcTemplate.queryForObject(
            "SELECT precomputed_response FROM case_type_snapshot WHERE case_type_reference = ?",
            String.class,
            TEST_CASE_TYPE
        );

        assertNotNull(snapshotJson);
        assertTrue(snapshotJson.contains(TEST_CASE_TYPE), "Snapshot should contain case type reference");
        assertTrue(snapshotJson.replaceAll("\\s+", "").contains("\"id\":\"" + TEST_CASE_TYPE + "\""),
            "Snapshot JSON should contain case type ID");

        // STEP 7: Verify timestamps are set
        jdbcTemplate.query(
            "SELECT created_at, last_modified FROM case_type_snapshot WHERE case_type_reference = ?",
            (ResultSet rs) -> {
                assertNotNull(rs.getTimestamp("created_at"), "created_at should not be null");
                assertNotNull(rs.getTimestamp("last_modified"), "last_modified should not be null");
            },
            TEST_CASE_TYPE
        );
    }

    @Test
    void shouldServeFromCacheViaCaseworkerEndpoint() throws Exception {
        // STEP 1: Import definition
        try (final InputStream inputStream = getClass().getResourceAsStream(EXCEL_FILE_CCD_DEFINITION)) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            MvcResult importResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(importResult, HttpStatus.SC_CREATED);
        }

        // STEP 2: Wait for snapshot creation
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Integer count = getSnapshotCount();
                assertNotNull(count);
                assertTrue(count > 0);
            });

        // STEP 3: GET via caseworker endpoint
        final String caseworkerUrl = String.format(
            CASE_TYPE_BY_USER_URL,
            TEST_USER_ID,
            TEST_JURISDICTION,
            TEST_CASE_TYPE
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(caseworkerUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_CASE_TYPE))
            .andReturn();

        final CaseType caseType = getCaseType(result);

        assertNotNull(caseType);
        assertEquals(TEST_CASE_TYPE, caseType.getId());
    }

    @Test
    void shouldCreateMultipleSnapshotsForDifferentCaseTypes() throws Exception {
        // STEP 1: Import definition with multiple case types
        try (final InputStream inputStream = getClass().getResourceAsStream(EXCEL_FILE_CCD_DEFINITION)) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            MvcResult importResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(importResult, HttpStatus.SC_CREATED);
        }

        // STEP 2: Wait for all snapshots to be created
        await()
            .atMost(15, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM case_type_snapshot",
                    Integer.class
                );

                assertNotNull(count);
                assertTrue(count >= 2, "Should have snapshots for multiple case types");
            });

        // STEP 3: Query all created snapshots
        assertEquals(2, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM case_type_snapshot",
            Integer.class));

        jdbcTemplate.query(
            "SELECT case_type_reference, version_id FROM case_type_snapshot ORDER BY case_type_reference",
            (ResultSet rs) -> {
                String caseTypeRef = rs.getString("case_type_reference");
                Integer versionId = rs.getInt("version_id");

                assertNotNull(caseTypeRef, "Case type reference should not be null");
                assertEquals(1, versionId, "Initial version should be 1 for " + caseTypeRef);
            }
        );
    }

    @Test
    void shouldHandleGetRequestBeforeSnapshotCreation() throws Exception {
        // STEP 1: Import definition
        try (final InputStream inputStream = getClass().getResourceAsStream(EXCEL_FILE_CCD_DEFINITION)) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            MvcResult importResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(importResult, HttpStatus.SC_CREATED);
        }

        // STEP 2: Immediately query (before async snapshot completes)
        final String caseTypeUrl = String.format(CASE_TYPE_URL, TEST_CASE_TYPE);

        MvcResult immediateResult = mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_CASE_TYPE))
            .andReturn();

        final CaseType caseType = getCaseType(immediateResult);

        assertNotNull(caseType);
        assertEquals(TEST_CASE_TYPE, caseType.getId());

        // STEP 3: Wait for snapshot creation
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Integer count = getSnapshotCount();
                assertTrue(count > 0);
            });

        // STEP 4: Query again (now should use snapshot)
        MvcResult cachedResult = mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_CASE_TYPE))
            .andReturn();

        assertEquals(
            immediateResult.getResponse().getContentAsString(),
            cachedResult.getResponse().getContentAsString(),
            "Response before and after snapshot creation should be identical"
        );
    }

    @Test
    void shouldUpdateSnapshotWhenNewVersionImported() throws Exception {
        // STEP 1: First import (version 1)
        try (final InputStream inputStream = getClass().getResourceAsStream(EXCEL_FILE_CCD_DEFINITION)) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            MvcResult importResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(importResult, HttpStatus.SC_CREATED);
        }

        // STEP 2: Wait for snapshot v1
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Integer actualCaseTypeVersion = getActualCaseTypeVersion();
                Integer snapshotVersion = getSnapshotVersion();

                assertNotNull(snapshotVersion, "Version ID should not be null");
                assertEquals(actualCaseTypeVersion, snapshotVersion,
                    "Snapshot version should match the actual case type version");
            });

        // STEP 3: Second import (version 2)
        try (final InputStream inputStream = getClass().getResourceAsStream(EXCEL_FILE_CCD_DEFINITION)) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            MvcResult importResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(importResult, HttpStatus.SC_CREATED);
        }

        // STEP 4: Wait for snapshot v2 update
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Integer actualCaseTypeVersion = getActualCaseTypeVersion();

                Integer snapshotVersion = getSnapshotVersion();

                assertNotNull(snapshotVersion, "Version ID should not be null");
                assertEquals(actualCaseTypeVersion, snapshotVersion,
                    "Snapshot version should match the actual case type version");
            });

        // STEP 5: Verify only one snapshot exists (updated, not duplicated)
        Integer snapshotCount = getSnapshotCount();
        assertEquals(1, snapshotCount, "Should only have one snapshot per case type");
    }

    @Test
    void shouldVerifySnapshotPerformanceBenefit() throws Exception {
        // STEP 1: Import definition
        try (final InputStream inputStream = getClass().getResourceAsStream(EXCEL_FILE_CCD_DEFINITION)) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            MvcResult importResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(importResult, HttpStatus.SC_CREATED);
        }

        // STEP 2: Wait for snapshot
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Integer count = getSnapshotCount();
                assertNotNull(count, "Count should not be null");
                assertTrue(count > 0);
            });

        // STEP 3: Delete snapshot to force DB load
        int deletedRows = jdbcTemplate.update(
            "DELETE FROM case_type_snapshot WHERE case_type_reference = ?",
            TEST_CASE_TYPE
        );

        assertTrue(deletedRows > 0, "Deleted rows should be greater than 0");

        // STEP 4: Query without snapshot (baseline)
        final String caseTypeUrl = String.format(CASE_TYPE_URL, TEST_CASE_TYPE);
        long startTime = System.currentTimeMillis();

        mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk());

        long withoutSnapshotTime = System.currentTimeMillis() - startTime;

        // STEP 5: Wait for new snapshot to be created
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Integer count = getSnapshotCount();
                assertNotNull(count, "Count should not be null");
                assertTrue(count > 0);
            });

        // STEP 6: Query with snapshot multiple times to get average
        long totalWithSnapshotTime = 0;
        int iterations = 5;

        for (int i = 0; i < iterations; i++) {
            startTime = System.currentTimeMillis();
            mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andExpect(MockMvcResultMatchers.status().isOk());
            totalWithSnapshotTime += System.currentTimeMillis() - startTime;
        }

        long avgWithSnapshotTime = totalWithSnapshotTime / iterations;
        double improvementPercentage =
            ((double)(withoutSnapshotTime - avgWithSnapshotTime) / withoutSnapshotTime) * 100;

        assertThat(avgWithSnapshotTime)
            .as("Snapshot query should be significantly faster than non-snapshot query")
            .isLessThan(withoutSnapshotTime);

        assertThat(improvementPercentage)
            .as("Snapshot should provide at least 30% performance improvement")
            .isGreaterThan(30.0);
    }

    private @Nullable Integer getSnapshotCount() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM case_type_snapshot WHERE case_type_reference = ?",
            Integer.class,
            TEST_CASE_TYPE
        );
    }

    private @Nullable Integer getActualCaseTypeVersion() {
        return jdbcTemplate.queryForObject(
            "SELECT MAX(version) FROM case_type WHERE reference = ?",
            Integer.class,
            TEST_CASE_TYPE
        );
    }

    private @Nullable Integer getSnapshotVersion() {
        return jdbcTemplate.queryForObject(
            "SELECT version_id FROM case_type_snapshot WHERE case_type_reference = ?",
            Integer.class,
            TEST_CASE_TYPE
        );
    }

    private CaseType getCaseType(MvcResult result) throws JsonProcessingException,
        UnsupportedEncodingException {
        return mapper.readValue(
            result.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(CaseType.class)
        );
    }
}
