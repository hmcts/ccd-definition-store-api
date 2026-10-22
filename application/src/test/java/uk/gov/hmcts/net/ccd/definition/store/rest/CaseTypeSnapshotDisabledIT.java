package uk.gov.hmcts.net.ccd.definition.store.rest;

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
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Integration test for the global case type snapshot kill switch.
 */
@TestPropertySource(
    locations = "classpath:test.properties",
    properties = "case-type.snapshot.enabled=false"
)
@Sql(
    statements = "DELETE FROM case_type_snapshot",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(
        transactionMode = SqlConfig.TransactionMode.ISOLATED,
        transactionManager = "transactionManager"
    )
)
class CaseTypeSnapshotDisabledIT extends BaseTest {

    private static final String CASE_TYPE_URL = "/api/data/case-type/%s";
    private static final String TEST_CASE_TYPE = "TestAddressBookCase";

    @Test
    void shouldBypassReadsAndWritesWhenSnapshotCacheIsDisabled() throws Exception {
        importDefinition();

        await()
            .atMost(2, TimeUnit.SECONDS)
            .pollDelay(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> assertEquals(0, getSnapshotCount(),
                "Snapshot should not be created by eager async path when globally disabled"));

        final String caseTypeUrl = String.format(CASE_TYPE_URL, TEST_CASE_TYPE);

        mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_CASE_TYPE));

        assertEquals(0, getSnapshotCount(), "Snapshot should not be created lazily when globally disabled");

        insertBadSnapshot();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(caseTypeUrl)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.id").value(TEST_CASE_TYPE))
            .andReturn();

        assertNotNull(result.getResponse().getContentAsString());
        assertEquals("WrongSnapshot", getSnapshotId(), "Seeded snapshot should remain untouched");
    }

    private void importDefinition() throws Exception {
        try (final InputStream inputStream =
                 new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            MvcResult importResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(importResult, HttpStatus.SC_CREATED);
        }
    }

    private void insertBadSnapshot() {
        jdbcTemplate.update(
            """
                INSERT INTO case_type_snapshot (case_type_reference, version_id, precomputed_response)
                VALUES (?, ?, CAST(? AS jsonb))
                """,
            TEST_CASE_TYPE,
            getCaseTypeVersion(),
            "{\"id\":\"WrongSnapshot\"}"
        );
    }

    private @Nullable Integer getCaseTypeVersion() {
        return jdbcTemplate.queryForObject(
            "SELECT MAX(version) FROM case_type WHERE reference = ?",
            Integer.class,
            TEST_CASE_TYPE
        );
    }

    private @Nullable Integer getSnapshotCount() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM case_type_snapshot WHERE case_type_reference = ?",
            Integer.class,
            TEST_CASE_TYPE
        );
    }

    private @Nullable String getSnapshotId() {
        return jdbcTemplate.queryForObject(
            "SELECT precomputed_response ->> 'id' FROM case_type_snapshot WHERE case_type_reference = ?",
            String.class,
            TEST_CASE_TYPE
        );
    }
}
