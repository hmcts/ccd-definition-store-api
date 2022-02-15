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
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.FileStorageService;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private ProcessUploadServiceImpl processUploadService;

    private MockMultipartFile file;

    private DefinitionFileUploadMetadata metadata;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        file =
            new MockMultipartFile("file",
                Files.readAllBytes(new File("src/test/resources/CCD_TestDefinition.xlsx")
                    .toPath()));
        metadata = new DefinitionFileUploadMetadata();
        metadata.setJurisdiction("TEST");
        metadata.addCaseType("TestCaseType");
        metadata.setUserId("user@hmcts.net");
        when(importService.importFormDefinitions(any())).thenReturn(metadata);
        when(importService.getImportWarnings()).thenReturn(Collections.emptyList());
    }

    @DisplayName("Upload - Green path, Azure enabled")
    @Test
    void validUploadAzureEnabled() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
        val result = processUploadService.processUpload(file);
        verify(fileStorageService).uploadFile(file, metadata);
        assertEquals(result.getStatusCode(), HttpStatus.CREATED);
        assertEquals(result.getBody(), processUploadService.SUCCESSFULLY_CREATED);
    }

    @DisplayName("Upload - Green non-path, Azure enabled")
    @Test
    void invalidUploadAzureEnabled() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
        final IOException
            exception =
            assertThrows(IOException.class, () -> processUploadService.processUpload(null));
        assertThat(exception.getMessage(), is(IMPORT_FILE_ERROR));
    }

    @DisplayName("Upload - Green non-path due to file zero, Azure enabled")
    @Test
    void invalidUploadAzureEnabledDueToFileZero() {
        String str = "";
        byte[] bytes = str.getBytes();
        val fileTest = new MockMultipartFile("name",bytes);
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
        final IOException
            exception =
            assertThrows(IOException.class, () -> processUploadService.processUpload(fileTest));
        assertThat(exception.getMessage(), is(IMPORT_FILE_ERROR));
    }

    @DisplayName("Upload - Green path, Azure disabled")
    @Test
    void validUploadAzureDisabled() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);
        val result = processUploadService.processUpload(file);
        verify(fileStorageService, never()).uploadFile(file, metadata);
        assertEquals(result.getStatusCode(), HttpStatus.CREATED);
        assertEquals(result.getBody(), processUploadService.SUCCESSFULLY_CREATED);
    }

    @DisplayName("Upload - non-Green path")
    @Test
    void invalidUpload() throws Exception {

        willThrow(new IOException("boo")).given(importService).importFormDefinitions(any());

        final IOException
            exception =
            assertThrows(IOException.class, () -> processUploadService.processUpload(file));
        assertThat(exception.getMessage(), is("boo"));
    }

    @DisplayName("Upload - warnings during import, Azure disabled")
    @Test
    void validUploadWithWarnings() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);
        final String firstWarning = "First warning";
        final String secondWarning = "Second warning";
        when(importService.getImportWarnings()).thenReturn(Arrays.asList(firstWarning, secondWarning));
        val result = processUploadService.processUpload(file);
        assertEquals(result.getStatusCode(), HttpStatus.CREATED);
        assertEquals(result.getBody(), processUploadService.SUCCESSFULLY_CREATED);
        assertEquals(result.getHeaders().get(processUploadService.IMPORT_WARNINGS_HEADER),
            Arrays.asList(firstWarning, secondWarning));
    }

    @DisplayName("Upload - No Green path due to null values")
    @Test
    void invalidUploadDueToNullValues() throws Exception {
        val processUploadServiceTest =
            new ProcessUploadServiceImpl(importService, null, null);
        val result = processUploadServiceTest.processUpload(file);
        assertEquals(result.getStatusCode(), HttpStatus.CREATED);
        assertEquals(result.getBody(), processUploadService.SUCCESSFULLY_CREATED);
    }
}
