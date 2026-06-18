package uk.gov.hmcts.net.ccd.definition.store.excel;

import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController;
import uk.gov.hmcts.ccd.definition.store.repository.ImportJobRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobStatus;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Integration tests for import job tracking behavior (CCD-7698).
 * Critical focus: verifying that the three-transaction model (TX1 PENDING, TX2 import, TX3 COMPLETED/FAILED)
 * produces the correct import_jobs state even when TX2 rolls back.
 * Tests use @Transactional(NOT_SUPPORTED) wherever a cross-transaction state must be observable.
 * The @AfterEach cleanup always runs in its own committed REQUIRES_NEW transaction so that
 * import_jobs rows persisted by REQUIRES_NEW service transactions are always swept up.
 */
@TestPropertySource(properties = {"ccd.authorised.services=ccd_data"})
class ImportJobIntegrationTest extends BaseTest {

    // Missing-sheet file produces a MapperException (400), ensuring TX2 always rolls back
    private static final String INVALID_XLSX = "/ccd_testdefinition-missing-WorkBasketResultFields.xlsx";
    private static final String GET_JOB_URL = "/import-jobs/";

    // uid returned by the WireMock stub in mappings/_idam_default.json
    private static final String MOCKED_USER_UID = "445";

    @Inject
    private ImportJobRepository importJobRepository;

    @Inject
    private PlatformTransactionManager transactionManager;

    /**
     * Deletes all import_jobs rows in a freshly committed REQUIRES_NEW transaction.
     * This runs even inside a @Transactional test method and the REQUIRES_NEW transaction commits
     * the DELETE independently, so Spring Test's subsequent rollback of the outer transaction
     * does not un-delete the rows.
     */
    @AfterEach
    void cleanImportJobs() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            jdbcTemplate.update("DELETE FROM import_jobs");
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new RuntimeException("import_jobs cleanup failed", e);
        }
    }

    /**
     * A successful import commits TX1 (PENDING) and TX3 (COMPLETED) independently via REQUIRES_NEW.
     * The test uses @Transactional so that TX2's import data is rolled back, keeping the DB clean,
     * while the COMPLETED row, committed before the test transaction, rolls back and remains visible.
     */
    @Test
    @Transactional
    void successfulImportCreatesCompletedRow() throws Exception {
        try (InputStream in = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", in);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            assertResponseCode(result, 201);

            String jobIdStr = result.getResponse().getHeader(ImportController.IMPORT_JOB_ID_HEADER);
            assertNotNull(jobIdStr, "X-Import-Job-Id response header must be present");
            UUID jobId = UUID.fromString(jobIdStr);

            // COMPLETED row is visible here because TX3 (REQUIRES_NEW) committed it independently
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM import_jobs WHERE id = ?", jobId);
            assertThat(rows, hasSize(1));
            Map<String, Object> row = rows.getFirst();
            assertThat(row.get("status").toString(), is("COMPLETED"));
            assertThat(row.get("submitted_at"), notNullValue());
            assertThat(row.get("completed_at"), notNullValue());
            assertThat(row.get("submitted_by").toString(), is(MOCKED_USER_UID));
        }
    }

    /**
     * Proves that TX1 and TX3 commit independently of TX2.
     * Uses NOT_SUPPORTED so there is no outer test transaction wrapping the controller call.
     * The import fails (TX2 rolls back), but the FAILED row written by TX3 survives. The
     * case_type table is empty, confirming TX2's rollback.
     * A client-supplied job id is used so the id is known before the request; the exception
     * that fails TX2 also prevents the controller from echoing the header on the error response.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void failedImportLeavesFailedRow_andRollsBackImportData() throws Exception {
        UUID suppliedJobId = UUID.randomUUID();

        try (InputStream in = new ClassPathResource(INVALID_XLSX, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", in);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser")
                    .header(ImportController.IMPORT_JOB_ID_HEADER, suppliedJobId.toString()))
                .andReturn();

            int httpStatus = result.getResponse().getStatus();
            assertTrue(httpStatus >= 400,
                "Import of invalid file must fail with 4xx, got " + httpStatus);
        }

        // TX1 committed a PENDING row; TX3 flipped it to FAILED
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM import_jobs WHERE id = ?", suppliedJobId);
        assertThat(rows, hasSize(1));
        Map<String, Object> row = rows.getFirst();
        assertThat("import_jobs row must be FAILED after failed import",
            row.get("status").toString(), is("FAILED"));
        assertThat("error_summary must be populated from the exception message",
            row.get("error_summary"), notNullValue());

        // TX2 rolled back: no case_type data was persisted
        Integer caseTypeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM case_type", Integer.class);
        assertThat("TX2 rollback must leave case_type table empty", caseTypeCount, is(0));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void postWithNoJobIdHeader_serverGeneratesId() throws Exception {
        try (InputStream in = new ClassPathResource(INVALID_XLSX, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", in);

            mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM import_jobs");
            assertThat("Server must have created an import_jobs row", rows, hasSize(1));
            String dbId = rows.getFirst().get("id").toString();
            assertDoesNotThrow(() -> UUID.fromString(dbId), "DB row id must be a valid UUID");
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void postWithValidJobIdHeader_usesSuppliedId() throws Exception {
        UUID suppliedId = UUID.randomUUID();

        try (InputStream in = new ClassPathResource(INVALID_XLSX, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", in);

            mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                .file(file)
                .header(AUTHORIZATION, "Bearer testUser")
                .header(ImportController.IMPORT_JOB_ID_HEADER, suppliedId.toString()))
                .andReturn();
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM import_jobs WHERE id = ?", suppliedId);
        assertThat("DB must contain a row with the supplied job id", rows, hasSize(1));
    }

    @Test
    void postWithMalformedJobIdHeader_returns400() throws Exception {
        try (InputStream in = new ClassPathResource(INVALID_XLSX, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", in);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser")
                    .header(ImportController.IMPORT_JOB_ID_HEADER, "not-a-valid-uuid"))
                .andReturn();

            assertResponseCode(result, 400);

            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM import_jobs", Integer.class);
            assertThat("Malformed header must not create any import_jobs row", count, is(0));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void postWithDuplicateJobId_returns409() throws Exception {
        UUID existingId = UUID.randomUUID();
        importJobRepository.save(buildPendingEntity(existingId, LocalDateTime.now()));

        try (InputStream in = new ClassPathResource(INVALID_XLSX, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", in);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser")
                    .header(ImportController.IMPORT_JOB_ID_HEADER, existingId.toString()))
                .andReturn();

            assertResponseCode(result, 409);
        }

        // The existing row must not have been modified
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM import_jobs WHERE id = ?", existingId);
        assertThat(rows, hasSize(1));
        assertThat(rows.getFirst().get("status").toString(), is("PENDING"));
    }

    /**
     * Fires two concurrent POSTs with the same client-supplied X-Import-Job-Id using an
     * invalid file (TX2 always rolls back, leaving no committed case_type/jurisdiction data).
     * The race is at TX1 (createPending INSERT): exactly one thread wins the PK constraint
     * and gets a normal import-failure 400; the other hits the duplicate-key violation and
     * gets 409. Exactly one import_jobs row must survive with status FAILED.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentDuplicateJobId_exactlyOneSucceedsAndOneConflicts() throws Exception {
        UUID sharedJobId = UUID.randomUUID();
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = new DelegatingSecurityContextExecutorService(
            Executors.newFixedThreadPool(2));

        Callable<Integer> post = () -> {
            startLatch.await();
            try (InputStream in = new ClassPathResource(INVALID_XLSX, getClass()).getInputStream()) {
                MockMultipartFile file = new MockMultipartFile("file", in);
                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                        .file(file)
                        .header(AUTHORIZATION, "Bearer testUser")
                        .header(ImportController.IMPORT_JOB_ID_HEADER, sharedJobId.toString()))
                    .andReturn();
                return result.getResponse().getStatus();
            }
        };

        Future<Integer> f1 = executor.submit(post);
        Future<Integer> f2 = executor.submit(post);
        startLatch.countDown();

        List<Integer> codes = List.of(
            f1.get(30, TimeUnit.SECONDS),
            f2.get(30, TimeUnit.SECONDS)
        );
        executor.shutdown();

        List<Integer> sorted = codes.stream().sorted().toList();
        assertThat("Expected one 400 (import failure) and one 409 (duplicate job id)", sorted, is(List.of(400, 409)));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM import_jobs WHERE id = ?", sharedJobId);
        assertThat("Exactly one row must exist for the shared job id", rows, hasSize(1));
        assertThat("The surviving row must be FAILED",
            rows.getFirst().get("status").toString(), is("FAILED"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getForOwnRow_returns200WithCorrectBody() throws Exception {
        UUID id = UUID.randomUUID();
        importJobRepository.save(buildCompletedEntity(id, MOCKED_USER_UID));

        mockMvc.perform(MockMvcRequestBuilders.get(GET_JOB_URL + id)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("COMPLETED"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.submittedBy").value(MOCKED_USER_UID));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getForDifferentUsersRow_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        importJobRepository.save(buildCompletedEntity(id, "some-other-uid"));

        mockMvc.perform(MockMvcRequestBuilders.get(GET_JOB_URL + id)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.submittedBy").value("some-other-uid"));
    }

    @Test
    void getForUnknownId_returns404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(GET_JOB_URL + UUID.randomUUID())
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void getForMalformedId_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(GET_JOB_URL + "not-a-uuid")
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getResponseDeserializesMultilineWarnings() throws Exception {
        UUID id = UUID.randomUUID();
        ImportJobEntity entity = buildCompletedEntity(id, MOCKED_USER_UID);
        entity.setWarnings("Warning A\nWarning B\nWarning C");
        importJobRepository.save(entity);

        mockMvc.perform(MockMvcRequestBuilders.get(GET_JOB_URL + id)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.warnings.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.warnings[0]").value("Warning A"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.warnings[1]").value("Warning B"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.warnings[2]").value("Warning C"));
    }

    /**
     * The sweep runs inside GET before findById, so the response itself shows EXPIRED.
     * Stale threshold is ccd.tx-timeout.default (30s default) + 30s buffer = 60s.
     * A row backdated 200s is well past the threshold.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void sweepOnGet_flipsOldPendingToExpired() throws Exception {
        UUID id = UUID.randomUUID();
        importJobRepository.save(
            buildPendingEntity(id, LocalDateTime.now().minusSeconds(200)));

        mockMvc.perform(MockMvcRequestBuilders.get(GET_JOB_URL + id)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("EXPIRED"));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM import_jobs WHERE id = ?", id);
        assertThat(rows, hasSize(1));
        assertThat("DB row must be EXPIRED after sweep",
            rows.getFirst().get("status").toString(), is("EXPIRED"));
        assertThat("completed_at must be set by the sweep",
            rows.getFirst().get("completed_at"), notNullValue());
    }

    /**
     * The sweep runs inside processUpload before createPending, so the stale row is EXPIRED
     * before the new import's PENDING row is created.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void sweepOnPost_flipsOldPendingRow() throws Exception {
        UUID staleId = UUID.randomUUID();
        importJobRepository.save(
            buildPendingEntity(staleId, LocalDateTime.now().minusSeconds(200)));

        try (InputStream in = new ClassPathResource(INVALID_XLSX, getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", in);

            mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                .file(file)
                .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();

            // The POST also created its own row. DB should have two rows total (stale + new)
            List<Map<String, Object>> allRows = jdbcTemplate.queryForList("SELECT * FROM import_jobs");
            assertThat("POST must create a new row in addition to the pre-existing stale one",
                allRows, hasSize(2));
        }

        List<Map<String, Object>> staleRows = jdbcTemplate.queryForList(
            "SELECT * FROM import_jobs WHERE id = ?", staleId);
        assertThat(staleRows, hasSize(1));
        assertThat("Stale PENDING row must be EXPIRED after sweep triggered by POST",
            staleRows.getFirst().get("status").toString(), is("EXPIRED"));
    }

    /**
     * A row with started_at 30 seconds ago is inside the 60-second threshold and must not be swept.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void sweepDoesNotFlipRowsWithinThreshold() throws Exception {
        UUID id = UUID.randomUUID();
        importJobRepository.save(
            buildPendingEntity(id, LocalDateTime.now().minusSeconds(30)));

        mockMvc.perform(MockMvcRequestBuilders.get(GET_JOB_URL + id)
                .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"));
    }

    private ImportJobEntity buildPendingEntity(UUID id, LocalDateTime startedAt) {
        ImportJobEntity entity = new ImportJobEntity();
        entity.setId(id);
        entity.setStatus(ImportJobStatus.PENDING);
        entity.setSubmittedBy(ImportJobIntegrationTest.MOCKED_USER_UID);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setStartedAt(startedAt);
        return entity;
    }

    private ImportJobEntity buildCompletedEntity(UUID id, String submittedBy) {
        ImportJobEntity entity = new ImportJobEntity();
        entity.setId(id);
        entity.setStatus(ImportJobStatus.COMPLETED);
        entity.setSubmittedBy(submittedBy);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setStartedAt(LocalDateTime.now());
        entity.setCompletedAt(LocalDateTime.now());
        return entity;
    }
}
