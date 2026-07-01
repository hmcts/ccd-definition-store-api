package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.junit.jupiter.api.AfterEach;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.service.ImportJobService;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.ImportJobFailedException;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import uk.gov.hmcts.ccd.definition.store.excel.common.TestLoggerUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.excel.service.ProcessUploadService.IMPORT_FILE_ERROR;

class ProcessUploadServiceTest {

    @Mock
    private ImportWorkService importWorkService;

    @Mock
    private ImportJobService importJobService;

    @Mock
    private IdamProfileClient idamProfileClient;

    @Mock
    private ApplicationParams applicationParams;

    @InjectMocks
    private ProcessUploadServiceImpl processUploadService;

    private MockMultipartFile file;

    private DefinitionFileUploadMetadata metadata;

    private static final String SUBMITTER_UID = "uid-123";

    private AutoCloseable closeable;

    @BeforeEach
    void setup() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);

        file =
            new MockMultipartFile("file",
                Files.readAllBytes(new File("src/test/resources/CCD_TestDefinition.xlsx")
                    .toPath()));
        metadata = new DefinitionFileUploadMetadata();
        metadata.setJurisdiction("TEST");
        metadata.addCaseType("TestCaseType");
        metadata.setUserId("user@hmcts.net");
        metadata.setTaskId("task-001");

        IdamProperties idamProperties = new IdamProperties();
        idamProperties.setId(SUBMITTER_UID);
        idamProperties.setEmail("user@hmcts.net");
        when(idamProfileClient.getLoggedInUserDetails()).thenReturn(idamProperties);

        when(importJobService.createPending(any(), any())).thenAnswer(inv -> {
            UUID supplied = inv.getArgument(0);
            return supplied != null ? supplied : UUID.randomUUID();
        });

        when(importWorkService.doImport(any(), any(), anyBoolean(), anyBoolean(), any(UUID.class)))
            .thenReturn(new ImportWorkResult(metadata, Collections.emptyList()));
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @DisplayName("Upload - Green path returns CREATED and includes job id in result")
    @Test
    void validUploadHappyPath() throws Exception {
        ProcessUploadResult result = processUploadService.processUpload(file, false,
            false, null);

        assertEquals(HttpStatus.CREATED, result.response().getStatusCode());
        assertEquals(ProcessUploadService.SUCCESSFULLY_CREATED, result.response().getBody());
        assertNotNull(result.jobId());
    }

    @DisplayName("Upload - reindex true → Elasticsearch-Reindex-Task header set from metadata")
    @Test
    void validUploadWithReindex() throws Exception {
        ProcessUploadResult result = processUploadService.processUpload(file,
            true, true, null);

        assertEquals(HttpStatus.CREATED, result.response().getStatusCode());
        assertEquals(metadata.getTaskId(),
            result.response().getHeaders().getFirst("Elasticsearch-Reindex-Task"));
    }

    @DisplayName("Upload - null file throws IOException")
    @Test
    void invalidUploadNullFile() {
        final IOException exception =
            assertThrows(IOException.class,
                () -> processUploadService.processUpload(null, false, false, null));
        assertThat(exception.getMessage(), is(IMPORT_FILE_ERROR));
    }

    @DisplayName("Upload - empty file throws IOException")
    @Test
    void invalidUploadEmptyFile() {
        byte[] bytes = "".getBytes();
        MockMultipartFile fileTest = new MockMultipartFile("name", bytes);
        final IOException exception =
            assertThrows(IOException.class,
                () -> processUploadService.processUpload(fileTest, false, false, null));
        assertThat(exception.getMessage(), is(IMPORT_FILE_ERROR));
    }

    @DisplayName("Upload - doImport throws → markFailed called with message, exception re-thrown")
    @Test
    void invalidUpload() throws Exception {
        willThrow(new IOException("boo"))
            .given(importWorkService).doImport(any(), any(), eq(false), eq(false), any(UUID.class));

        final ImportJobFailedException exception = assertThrows(ImportJobFailedException.class,
            () -> processUploadService.processUpload(file, false, false, null));

        assertThat(exception.getCause().getMessage(), is("boo"));
        assertNotNull(exception.getJobId());
        verify(importJobService).markFailed(any(UUID.class), eq("boo"));
        verify(importJobService, never()).markCompleted(any(), any(), any());
    }

    @DisplayName("Upload - warnings from work result included in response header (not from ImportService singleton)")
    @Test
    void validUploadWithWarnings() throws Exception {
        final String firstWarning = "First warning";
        final String secondWarning = "Second warning";
        List<String> warningsSnapshot = Arrays.asList(firstWarning, secondWarning);

        when(importWorkService.doImport(any(), any(), anyBoolean(), anyBoolean(), any(UUID.class)))
            .thenReturn(new ImportWorkResult(metadata, warningsSnapshot));

        ProcessUploadResult result = processUploadService.processUpload(file,
            false, false, null);

        assertEquals(HttpStatus.CREATED, result.response().getStatusCode());
        assertEquals(Arrays.asList(firstWarning, secondWarning),
            result.response().getHeaders().get(ProcessUploadService.IMPORT_WARNINGS_HEADER));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> warningsCaptor = ArgumentCaptor.forClass(List.class);
        verify(importJobService).markCompleted(any(UUID.class), warningsCaptor.capture(), eq(metadata.getTaskId()));
        assertEquals(warningsSnapshot, warningsCaptor.getValue());
    }

    @DisplayName("Orchestration order: expireStaleJobs → createPending → doImport → markCompleted")
    @Test
    void orchestrationOrder() throws Exception {
        processUploadService.processUpload(file, false, false, null);

        var inOrder = inOrder(importJobService, importWorkService);
        inOrder.verify(importJobService).expireStaleJobs();
        inOrder.verify(importJobService).createPending(null, SUBMITTER_UID);

        inOrder.verify(importWorkService).doImport(any(), eq(file), eq(false), eq(false), any(UUID.class));
        inOrder.verify(importJobService).markCompleted(any(UUID.class), any(), eq(metadata.getTaskId()));
    }

    @DisplayName("createPending receives submitter UID from idamProfileClient.getId(), not email")
    @Test
    void submitterIsUidNotEmail() throws Exception {
        processUploadService.processUpload(file, false, false, null);

        verify(importJobService).createPending(null, SUBMITTER_UID);
    }

    @DisplayName("createPending receives the providedJobId when one is supplied")
    @Test
    void providedJobIdIsPassedThrough() throws Exception {
        UUID provided = UUID.randomUUID();

        ProcessUploadResult result = processUploadService.processUpload(file, false,
            false, provided);

        verify(importJobService).createPending(provided, SUBMITTER_UID);
        assertEquals(provided, result.jobId());
    }

    @DisplayName("markCompleted throws → success result still returned, exception not propagated, ERROR log produced")
    @Test
    void markCompletedThrows_successReturnedAndErrorLogged() throws Exception {
        ListAppender<ILoggingEvent> logAppender = TestLoggerUtils.setupLogger();
        try {
            willThrow(new RuntimeException("db gone"))
                .given(importJobService).markCompleted(any(), any(), any());

            ProcessUploadResult result = processUploadService.processUpload(file, false,
                false, null);

            assertEquals(HttpStatus.CREATED, result.response().getStatusCode());
            boolean errorLogged = logAppender.list.stream()
                .anyMatch(e -> e.getLevel() == Level.ERROR
                    && e.getFormattedMessage().contains("markCompleted"));
            assertTrue(errorLogged, "Expected ERROR log mentioning markCompleted");
        } finally {
            TestLoggerUtils.teardownLogger();
        }
    }

    @DisplayName("markFailed throws → original doImport exception propagated, ERROR log produced")
    @Test
    void markFailedThrows_originalExceptionPropagated() throws Exception {
        ListAppender<ILoggingEvent> logAppender = TestLoggerUtils.setupLogger();
        try {
            IOException originalException = new IOException("original error");

            willThrow(originalException)
                .given(importWorkService).doImport(any(), any(), anyBoolean(), anyBoolean(), any(UUID.class));
            willThrow(new RuntimeException("marking failed"))
                .given(importJobService).markFailed(any(), any());

            ImportJobFailedException thrown = assertThrows(ImportJobFailedException.class,
                () -> processUploadService.processUpload(file, false, false, null));

            assertEquals("original error", thrown.getCause().getMessage());
            boolean errorLogged = logAppender.list.stream()
                .anyMatch(e -> e.getLevel() == Level.ERROR
                    && e.getFormattedMessage().contains("markFailed"));
            assertTrue(errorLogged, "Expected ERROR log mentioning markFailed");
        } finally {
            TestLoggerUtils.teardownLogger();
        }
    }

    @DisplayName("warnings list passed to markCompleted is the snapshot returned by doImport")
    @Test
    void warningsSnapshotPassedToMarkCompleted() throws Exception {
        List<String> snapshot = new ArrayList<>(Arrays.asList("w1", "w2"));

        when(importWorkService.doImport(any(), any(), anyBoolean(), anyBoolean(), any(UUID.class)))
            .thenReturn(new ImportWorkResult(metadata, snapshot));

        processUploadService.processUpload(file, false, false, null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(importJobService).markCompleted(any(UUID.class), captor.capture(), eq(metadata.getTaskId()));
        assertEquals(snapshot, captor.getValue());
    }

    @DisplayName("deleteOldIndex disabled by config → requested true is overridden to false on doImport")
    @Test
    void whenDeleteOldIndexEnabledFalseAndDeleteOldIndexTrueOverridesToFalse() throws Exception {
        when(applicationParams.isDeleteOldIndexEnabled()).thenReturn(false);

        // deleteOldIndex passed as true but applicationParams.isDeleteOldIndexEnabled() is false
        processUploadService.processUpload(file, true, true, null);

        // verifying that deleteOldIndex is overridden to false
        verify(importWorkService).doImport(any(), eq(file), eq(true), eq(false), any(UUID.class));
    }

    @DisplayName("deleteOldIndex enabled by config → requested true is preserved on doImport")
    @Test
    void whenDeleteOldIndexEnabledTrueDoesNotOverrideTrue() throws Exception {
        when(applicationParams.isDeleteOldIndexEnabled()).thenReturn(true);

        // deleteOldIndex passed as true and applicationParams.isDeleteOldIndexEnabled() is true
        processUploadService.processUpload(file, true, true, null);

        // verifying that deleteOldIndex remains true
        verify(importWorkService).doImport(any(), eq(file), eq(true), eq(true), any(UUID.class));
    }

    @DisplayName("deleteOldIndex enabled by config → requested false stays false on doImport")
    @Test
    void whenDeleteOldIndexEnabledTrueDoesNotOverrideFalse() throws Exception {
        when(applicationParams.isDeleteOldIndexEnabled()).thenReturn(true);

        // deleteOldIndex passed as false and applicationParams.isDeleteOldIndexEnabled() is true
        processUploadService.processUpload(file, true, false, null);

        // verifying that deleteOldIndex remains false
        verify(importWorkService).doImport(any(), eq(file), eq(true), eq(false), any(UUID.class));
    }
}
