package uk.gov.hmcts.ccd.definition.store.excel.service;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.FileStorageService;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.excel.service.ProcessUploadService.IMPORT_FILE_ERROR;

class ProcessUploadServiceTest {

    @Mock
    private ImportServiceImpl importService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AzureStorageConfiguration azureStorageConfiguration;

    @Mock
    private ApplicationParams applicationParams;

    @InjectMocks
    private ProcessUploadServiceImpl processUploadService;

    private MockMultipartFile file;

    private DefinitionFileUploadMetadata metadata;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.openMocks(this);

        file =
            new MockMultipartFile("file",
                Files.readAllBytes(new File("src/test/resources/CCD_TestDefinition.xlsx")
                    .toPath()));
        metadata = new DefinitionFileUploadMetadata();
        metadata.setJurisdiction("TEST");
        metadata.addCaseType("TestCaseType");
        metadata.setUserId("user@hmcts.net");
        when(importService.importFormDefinitions(any(), anyBoolean(), anyBoolean())).thenReturn(metadata);
        when(importService.getImportWarnings()).thenReturn(Collections.emptyList());
    }

    @DisplayName("Upload - Green path, Azure enabled")
    @Test
    void validUploadAzureEnabled() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
        val result = processUploadService.processUpload(file, false, false);
        verify(fileStorageService).uploadFile(file, metadata);
        assertEquals(result.getStatusCode(), HttpStatus.CREATED);
        assertEquals(result.getBody(), ProcessUploadService.SUCCESSFULLY_CREATED);
    }

    @Test
    void validUploadWithReindex() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
        val result = processUploadService.processUpload(file, true, true);
        verify(fileStorageService).uploadFile(file, metadata);
        assertEquals(result.getStatusCode(), HttpStatus.CREATED);
        assertEquals(result.getBody(), ProcessUploadService.SUCCESSFULLY_CREATED);
        assertEquals(result.getHeaders().getFirst("Elasticsearch-Reindex-Task"), metadata.getTaskId());
    }

    @DisplayName("Upload - Green non-path, Azure enabled")
    @Test
    void invalidUploadAzureEnabled() {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
        final IOException
            exception =
            assertThrows(IOException.class, () -> processUploadService.processUpload(null, false, false));
        assertThat(exception.getMessage(), is(IMPORT_FILE_ERROR));
    }

    @DisplayName("Upload - Green non-path due to file zero, Azure enabled")
    @Test
    void invalidUploadAzureEnabledDueToFileZero() {
        String str = "";
        byte[] bytes = str.getBytes();
        val fileTest = new MockMultipartFile("name", bytes);
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
        final IOException
            exception =
            assertThrows(IOException.class, () -> processUploadService.processUpload(fileTest, false, false));
        assertThat(exception.getMessage(), is(IMPORT_FILE_ERROR));
    }

    @DisplayName("Upload - Green path, Azure disabled")
    @Test
    void validUploadAzureDisabled() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);
        val result = processUploadService.processUpload(file, false, false);
        verify(fileStorageService, never()).uploadFile(file, metadata);
        assertEquals(result.getStatusCode(), HttpStatus.CREATED);
        assertEquals(result.getBody(), processUploadService.SUCCESSFULLY_CREATED);
    }

    @DisplayName("Upload - non-Green path")
    @Test
    void invalidUpload() throws Exception {

        willThrow(new IOException("boo")).given(importService).importFormDefinitions(any(), eq(false), eq(false));

        final IOException
            exception =
            assertThrows(IOException.class, () -> processUploadService.processUpload(file, false, false));
        assertThat(exception.getMessage(), is("boo"));
    }

    @DisplayName("Upload - warnings during import, Azure disabled")
    @Test
    void validUploadWithWarnings() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);
        final String firstWarning = "First warning";
        final String secondWarning = "Second warning";
        when(importService.getImportWarnings()).thenReturn(Arrays.asList(firstWarning, secondWarning));
        val result = processUploadService.processUpload(file, false, false);
        assertEquals(result.getStatusCode(), HttpStatus.CREATED);
        assertEquals(result.getBody(), processUploadService.SUCCESSFULLY_CREATED);
        assertEquals(result.getHeaders().get(processUploadService.IMPORT_WARNINGS_HEADER),
            Arrays.asList(firstWarning, secondWarning));
    }

    @DisplayName("Upload - No Green path due to null values")
    @Test
    void invalidUploadDueToNullValues() throws Exception {
        val processUploadServiceTest =
            new ProcessUploadServiceImpl(importService, null, null,
                applicationParams);
        val result = processUploadServiceTest.processUpload(file, false, false);
        assertEquals(result.getStatusCode(), HttpStatus.CREATED);
        assertEquals(result.getBody(), processUploadService.SUCCESSFULLY_CREATED);
    }

    @Test
    void whenDeleteOldIndexEnabledFalseAndDeleteOldIndexTrueOverridesToFalse() throws Exception {
        when(applicationParams.isDeleteOldIndexEnabled()).thenReturn(false);
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);

        //deleteOldIndex passed as true but applicationParams.isDeleteOldIndexEnabled() is false
        when(importService.importFormDefinitions(any(ByteArrayInputStream.class), eq(true), eq(false)))
            .thenReturn(metadata);
        processUploadService.processUpload(file, true, true);

        //verifying that deleteOldIndex is overridden to false
        verify(importService).importFormDefinitions(any(ByteArrayInputStream.class), eq(true), eq(false));
    }

    @Test
    void whenDeleteOldIndexEnabledTrueDoesNotOverrideTrue() throws Exception {
        when(applicationParams.isDeleteOldIndexEnabled()).thenReturn(true);
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);

        //deleteOldIndex passed as true and applicationParams.isDeleteOldIndexEnabled() is true
        when(importService.importFormDefinitions(any(ByteArrayInputStream.class), eq(true), eq(true)))
            .thenReturn(metadata);
        processUploadService.processUpload(file, true, true);

        //verifying that deleteOldIndex remains true
        verify(importService).importFormDefinitions(any(ByteArrayInputStream.class), eq(true), eq(true));
    }

    @Test
    void whenDeleteOldIndexEnabledTrueDoesNotOverrideFalse() throws Exception {
        when(applicationParams.isDeleteOldIndexEnabled()).thenReturn(true);
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);

        //deleteOldIndex passed as false and applicationParams.isDeleteOldIndexEnabled() is true
        when(importService.importFormDefinitions(any(ByteArrayInputStream.class), eq(true), eq(false)))
            .thenReturn(metadata);
        processUploadService.processUpload(file, true, false);

        //verifying that deleteOldIndex remains false
        verify(importService).importFormDefinitions(any(ByteArrayInputStream.class), eq(true), eq(false));
    }
}
