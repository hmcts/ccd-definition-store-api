package uk.gov.hmcts.net.ccd.definition.store.rest;

import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Integration test for synchronous snapshot creation when async is disabled.
 * Tests that when case-type.snapshot.enabled=false:
 * 1. Import does NOT create snapshots
 * 2. First GET request creates snapshot synchronously
 * 3. Subsequent GET requests use cached snapshot
 */
@TestPropertySource(
    locations = "classpath:test.properties",
    properties = {
        "case-type.snapshot.enabled=false",  // Disable async snapshot creation
        "spring.datasource.hikari.maximum-pool-size=25",
        "spring.datasource.hikari.minimum-idle=5",
        "spring.datasource.hikari.connection-timeout=30000",
        "spring.datasource.hikari.idle-timeout=600000"
    }
)
@Sql(
    statements = "DELETE FROM case_type_snapshot",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(
        transactionMode = SqlConfig.TransactionMode.ISOLATED,
        transactionManager = "transactionManager"
    )
)
class CaseTypeSnapshotSynchronousCreationIT extends BaseTest {

    private static final String CASE_TYPE_URL = "/api/data/case-type/%s";
    private static final String TEST_CASE_TYPE = "TestAddressBookCase";

    @Test
    void shouldCreateSnapshotSynchronouslyWhenAsyncIsDisabled() throws Exception {
        // STEP 1: Import definition file
        try (final InputStream inputStream =
                 new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            MvcResult importResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(importResult, HttpStatus.SC_CREATED);
        }

        // STEP 2: Wait and verify NO snapshot was created (async is disabled)
        await()
            .atMost(2, TimeUnit.SECONDS)
            .pollDelay(500, TimeUnit.MILLISECONDS) // Wait at least 500ms to give async a chance to fail
            .untilAsserted(() -> {
                Integer snapshotCount = getSnapshotCountByCaseType(TEST_CASE_TYPE);
                assertEquals(0, snapshotCount,
                    "Snapshot should NOT exist before GET request when async snapshot creation is disabled");
            });

        // STEP 3: Make GET request for case type (should create snapshot synchronously)
        final String caseTypeUrl = String.format(CASE_TYPE_URL, TEST_CASE_TYPE);

        MvcResult getResult = mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_CASE_TYPE))
            .andReturn();

        final CaseType caseType = mapper.readValue(
            getResult.getResponse().getContentAsString(),
            TypeFactory.defaultInstance().constructType(CaseType.class)
        );

        assertNotNull(caseType);
        assertEquals(TEST_CASE_TYPE, caseType.getId());
        assertNotNull(caseType.getEvents());
        assertFalse(caseType.getEvents().isEmpty(), "Case type should have events");

        // STEP 4: Verify snapshot was created DURING the GET request
        Integer snapshotCountAfterGet = getSnapshotCountByCaseType(TEST_CASE_TYPE);

        assertEquals(1, snapshotCountAfterGet,
            "Snapshot should exist immediately after GET request (created synchronously)");

        Integer actualCaseTypeVersion = getCaseTypeVersion();

        // STEP 5: Verify snapshot has correct version and data
        jdbcTemplate.query(
            "SELECT case_type_reference, version_id, precomputed_response, created_at, last_modified "
                + "FROM case_type_snapshot WHERE case_type_reference = ?",
            (ResultSet rs) -> {
                assertEquals(TEST_CASE_TYPE, rs.getString("case_type_reference"));
                assertEquals(actualCaseTypeVersion, rs.getInt("version_id"), "Version should be 1");

                String snapshotJson = rs.getString("precomputed_response");
                assertNotNull(snapshotJson, "Snapshot JSON should not be null");
                assertTrue(snapshotJson.contains(TEST_CASE_TYPE),
                    "Snapshot should contain case type reference");
                assertTrue(snapshotJson.replaceAll("\\s+", "")
                        .contains("\"id\":\"" + TEST_CASE_TYPE + "\""),
                    "Snapshot JSON should be valid case type JSON");

                assertNotNull(rs.getTimestamp("created_at"), "created_at should not be null");
                assertNotNull(rs.getTimestamp("last_modified"), "last_modified should not be null");
            },
            TEST_CASE_TYPE
        );

        // STEP 6: Make second GET request to verify it uses the cached snapshot
        MvcResult secondGetResult = mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_CASE_TYPE))
            .andReturn();

        assertEquals(
            getResult.getResponse().getContentAsString(),
            secondGetResult.getResponse().getContentAsString(),
            "Second request should return same data from cached snapshot"
        );

        // STEP 7: Verify still only one snapshot exists (no duplicates from concurrent access)
        Integer finalSnapshotCount = getSnapshotCountByCaseType(TEST_CASE_TYPE);

        assertEquals(1, finalSnapshotCount,
            "Should still have exactly one snapshot after multiple requests");
    }

    @Test
    void shouldHandleConcurrentRequestsWithoutDuplicatingSnapshots() throws Exception {
        // STEP 1: Import definition
        try (final InputStream inputStream =
                 new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();
        }

        await()
            .atMost(2, TimeUnit.SECONDS)
            .pollDelay(500, TimeUnit.MILLISECONDS) // Wait at least 500ms to give async a chance to fail
            .untilAsserted(() -> {
                Integer snapshotCount = getSnapshotCountByCaseType(TEST_CASE_TYPE);
                assertEquals(0, snapshotCount,
                    "Snapshot should NOT exist before GET request when async snapshot creation is disabled");
            });

        Integer initialCount = getSnapshotCountByCaseType(TEST_CASE_TYPE);
        assertEquals(0, initialCount);

        // STEP 3: Make multiple TRULY CONCURRENT requests using CompletableFuture
        final String caseTypeUrl = String.format(CASE_TYPE_URL, TEST_CASE_TYPE);
        final int numberOfConcurrentRequests = 6;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfConcurrentRequests);

        List<CompletableFuture<MvcResult>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfConcurrentRequests; i++) {
            CompletableFuture<MvcResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    startLatch.await();

                    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                            .header(AUTHORIZATION, "Bearer testUser"))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn();

                    completionLatch.countDown();
                    return result;
                } catch (Exception e) {
                    completionLatch.countDown();
                    throw new RuntimeException("Request failed", e);
                }
            });

            futures.add(future);
        }

        startLatch.countDown();

        boolean allCompleted = completionLatch.await(30, TimeUnit.SECONDS);
        assertTrue(allCompleted, "All concurrent requests should complete within 30 seconds");

        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<MvcResult> future = futures.get(i);
            assertTrue(future.isDone(), "Request " + i + " should be complete");
            assertFalse(future.isCompletedExceptionally(), "Request " + i + " should not have failed");

            MvcResult result = future.get();
            assertNotNull(result, "Request " + i + " should have a result");
            assertEquals(200, result.getResponse().getStatus(),
                "Request " + i + " should return 200 OK");
        }

        // STEP 4: Verify only ONE snapshot was created despite concurrent requests
        Integer finalCount = getSnapshotCountByCaseType(TEST_CASE_TYPE);

        assertEquals(1, finalCount,
            "Should only have one snapshot even with " + numberOfConcurrentRequests + " concurrent requests");

        // STEP 5: Verify snapshot has correct data
        Integer actualCaseTypeVersion = getCaseTypeVersion();

        jdbcTemplate.query(
            "SELECT case_type_reference, version_id FROM case_type_snapshot WHERE case_type_reference = ?",
            (ResultSet rs) -> {
                assertEquals(TEST_CASE_TYPE, rs.getString("case_type_reference"));
                assertEquals(actualCaseTypeVersion, rs.getInt("version_id"));
            },
            TEST_CASE_TYPE
        );
    }

    private @Nullable Integer getCaseTypeVersion() {
        return jdbcTemplate.queryForObject(
            "SELECT MAX(version) FROM case_type WHERE reference = ?",
            Integer.class,
            TEST_CASE_TYPE
        );
    }

    @Test
    void shouldCreateSnapshotForMultipleCaseTypes() throws Exception {
        // STEP 1: Import definition with multiple case types
        try (final InputStream inputStream =
                 new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();
        }

        // STEP 2: Verify no snapshots exist
        await()
            .atMost(2, TimeUnit.SECONDS)
            .pollDelay(500, TimeUnit.MILLISECONDS) // Wait at least 500ms to give async a chance to fail
            .untilAsserted(() -> {
                Integer snapshotCount = getSnapshotTotalCount();
                assertEquals(0, snapshotCount,
                    "No snapshots should exist initially with async disabled");
            });

        // STEP 3: Query first case type
        mockMvc.perform(MockMvcRequestBuilders.get(String.format(CASE_TYPE_URL, TEST_CASE_TYPE))
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk());

        // STEP 4: Query second case type
        String secondCaseType = "TestComplexAddressBookCase";
        mockMvc.perform(MockMvcRequestBuilders.get(String.format(CASE_TYPE_URL, secondCaseType))
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk());

        // STEP 5: Verify snapshots created for both case types
        Integer testCaseTypeCount = getSnapshotCountByCaseType(TEST_CASE_TYPE);
        assertEquals(1, testCaseTypeCount);

        Integer secondCaseTypeCount = getSnapshotCountByCaseType(secondCaseType);
        assertEquals(1, secondCaseTypeCount);

        // STEP 6: Verify total snapshot count
        Integer totalCount = getSnapshotTotalCount();
        assertEquals(2, totalCount, "Should have exactly 2 snapshots, one per case type");
    }

    private @Nullable Integer getSnapshotTotalCount() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM case_type_snapshot",
            Integer.class
        );
    }

    private @Nullable Integer getSnapshotCountByCaseType(String secondCaseType) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM case_type_snapshot WHERE case_type_reference = ?",
            Integer.class,
            secondCaseType
        );
    }
}
