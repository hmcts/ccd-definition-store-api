package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.ImportJobFailedException;
import uk.gov.hmcts.ccd.definition.store.excel.service.ProcessUploadResult;
import uk.gov.hmcts.ccd.definition.store.excel.service.ProcessUploadServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.IMPORT_JOB_ID_HEADER;
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.URI_IMPORT;

class ImportControllerTest {

    @Mock
    private ProcessUploadServiceImpl processUploadServiceImpl;

    @InjectMocks
    private ImportController controller;

    private MockMvc mockMvc;

    private MockMultipartFile file;

    private static final String SUCCESS_BODY = "Case Definition data successfully imported";

    private AutoCloseable closeable;

    @BeforeEach
    void setup() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        file =
            new MockMultipartFile("file",
                Files.readAllBytes(new File("src/test/resources/CCD_TestDefinition.xlsx")
                    .toPath()));
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @DisplayName("POST with no X-Import-Job-Id header → service called with null UUID, response has header from result")
    @Test
    void noJobIdHeader_serviceCalledWithNull_responseHeaderFromResult() throws Exception {
        UUID resultJobId = UUID.randomUUID();
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.CREATED).body(SUCCESS_BODY);
        when(processUploadServiceImpl.processUpload(any(), anyBoolean(), anyBoolean(), isNull()))
            .thenReturn(new ProcessUploadResult(response, resultJobId));

        mockMvc.perform(multipart(URI_IMPORT).file(file))
            .andExpect(status().isCreated())
            .andExpect(content().string(SUCCESS_BODY))
            .andExpect(header().string(IMPORT_JOB_ID_HEADER, resultJobId.toString()));

        verify(processUploadServiceImpl).processUpload(any(), eq(false), eq(false), isNull());
    }

    @DisplayName("POST with a valid X-Import-Job-Id header → service receives the parsed UUID")
    @Test
    void validJobIdHeader_passedToService() throws Exception {
        UUID supplied = UUID.randomUUID();
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.CREATED).body(SUCCESS_BODY);
        when(processUploadServiceImpl.processUpload(any(), anyBoolean(), anyBoolean(), eq(supplied)))
            .thenReturn(new ProcessUploadResult(response, supplied));

        mockMvc.perform(multipart(URI_IMPORT).file(file).header(IMPORT_JOB_ID_HEADER, supplied.toString()))
            .andExpect(status().isCreated())
            .andExpect(header().string(IMPORT_JOB_ID_HEADER, supplied.toString()));

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(processUploadServiceImpl).processUpload(any(), eq(false), eq(false), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(supplied, captor.getValue());
    }

    @DisplayName("POST with a malformed X-Import-Job-Id header → 400, service is not called")
    @Test
    void malformedJobIdHeader_returns400() throws Exception {
        mockMvc.perform(multipart(URI_IMPORT).file(file).header(IMPORT_JOB_ID_HEADER, "not-a-uuid"))
            .andExpect(status().isBadRequest());

        verify(processUploadServiceImpl, never()).processUpload(any(), anyBoolean(), anyBoolean(), any());
    }

    @DisplayName("Service throws DataIntegrityViolationException → controller returns 409")
    @Test
    void duplicateJobId_returns409() throws Exception {
        when(processUploadServiceImpl.processUpload(any(), anyBoolean(), anyBoolean(), any()))
            .thenThrow(new DataIntegrityViolationException("duplicate key"));

        mockMvc.perform(multipart(URI_IMPORT).file(file).header(IMPORT_JOB_ID_HEADER, UUID.randomUUID().toString()))
            .andExpect(status().isConflict());
    }

    @DisplayName("Upload - Green path, Azure enabled")
    @Test
    void validUploadAzureEnabled() throws Exception {
        UUID resultJobId = UUID.randomUUID();
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.CREATED).body(SUCCESS_BODY);
        when(processUploadServiceImpl.processUpload(any(), anyBoolean(), anyBoolean(), isNull()))
            .thenReturn(new ProcessUploadResult(response, resultJobId));

        mockMvc.perform(multipart(URI_IMPORT).file(file))
            .andExpect(status().isCreated())
            .andExpect(content().string(SUCCESS_BODY));
        verify(processUploadServiceImpl).processUpload(any(), eq(false), eq(false), isNull());
    }

    @DisplayName("Service throws ImportJobFailedException → cause re-thrown after setting header")
    @Test
    void importJobFailed_causeIsRethrown() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(processUploadServiceImpl.processUpload(any(), anyBoolean(), anyBoolean(), isNull()))
            .thenThrow(new ImportJobFailedException(jobId, new IOException("import failed")));

        Exception thrown = assertThrows(Exception.class,
            () -> mockMvc.perform(multipart(URI_IMPORT).file(file)));

        assertNotNull(thrown);
    }

    @DisplayName("Upload - Green path, Azure disabled, reindex requested")
    @Test
    void validUploadAzureDisabled() throws Exception {
        UUID resultJobId = UUID.randomUUID();
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.CREATED).body(SUCCESS_BODY);
        when(processUploadServiceImpl.processUpload(any(), anyBoolean(), anyBoolean(), isNull()))
            .thenReturn(new ProcessUploadResult(response, resultJobId));

        mockMvc.perform(multipart(URI_IMPORT).file(file).param("reindex", "true"))
            .andExpect(status().isCreated())
            .andExpect(content().string(SUCCESS_BODY));
        verify(processUploadServiceImpl).processUpload(any(), eq(true), eq(false), isNull());
    }
}
