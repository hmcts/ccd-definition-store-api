package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.FileStorageService;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportWorkServiceTest {

    @Mock
    private ImportServiceImpl importService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AzureStorageConfiguration azureStorageConfiguration;

    private DefinitionFileUploadMetadata metadata;

    private MockMultipartFile file;

    private final byte[] fileBytes = "spreadsheet-content".getBytes();
    private final UUID jobId = UUID.fromString("dd729a93-e1f8-4e90-b29a-93e1f80e90b2");

    @BeforeEach
    void setUp() {
        metadata = new DefinitionFileUploadMetadata();
        metadata.setJurisdiction("TEST");
        metadata.addCaseType("TestCaseType");
        metadata.setTaskId("task-001");
        file = new MockMultipartFile("file", fileBytes);
    }

    @DisplayName("doImport calls importFormDefinitions and returns metadata + warnings snapshot")
    @Test
    void happyPath() throws Exception {
        when(importService.importFormDefinitions(any(InputStream.class), anyBoolean(), anyBoolean()))
            .thenReturn(metadata);
        when(importService.getImportWarnings()).thenReturn(Arrays.asList("w1", "w2"));
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
        ImportWorkService classUnderTest =
            new ImportWorkService(importService, fileStorageService, azureStorageConfiguration);

        ImportWorkResult result = classUnderTest.doImport(fileBytes, file, false, false, jobId);

        verify(importService).importFormDefinitions(any(ByteArrayInputStream.class), eq(false), eq(false));
        verify(fileStorageService).uploadFile(file, metadata);
        assertEquals(metadata, result.metadata());
        assertEquals(Arrays.asList("w1", "w2"), result.warnings());
    }

    @DisplayName("warnings list returned by doImport is a defensive copy")
    @Test
    void warningsAreDefensiveCopy() throws Exception {
        List<String> live = new ArrayList<>(Arrays.asList("w1", "w2"));
        when(importService.importFormDefinitions(any(InputStream.class), anyBoolean(), anyBoolean()))
            .thenReturn(metadata);
        when(importService.getImportWarnings()).thenReturn(live);
        ImportWorkService classUnderTest = new ImportWorkService(importService, fileStorageService, null);

        ImportWorkResult result = classUnderTest.doImport(fileBytes, file, false, false, jobId);

        assertNotSame(live, result.warnings());
        live.add("w3");
        assertThat(result.warnings(), is(Arrays.asList("w1", "w2")));
    }

    @DisplayName("Azure upload skipped when AzureStorageConfiguration is null")
    @Test
    void azureSkippedWhenConfigurationNull() throws Exception {
        when(importService.importFormDefinitions(any(InputStream.class), anyBoolean(), anyBoolean()))
            .thenReturn(metadata);
        when(importService.getImportWarnings()).thenReturn(Collections.emptyList());
        ImportWorkService classUnderTest = new ImportWorkService(importService, fileStorageService, null);

        classUnderTest.doImport(fileBytes, file, false, false, jobId);

        verify(fileStorageService, never()).uploadFile(any(), any());
    }

    @DisplayName("Azure upload skipped when upload disabled")
    @Test
    void azureSkippedWhenDisabled() throws Exception {
        when(importService.importFormDefinitions(any(InputStream.class), anyBoolean(), anyBoolean()))
            .thenReturn(metadata);
        when(importService.getImportWarnings()).thenReturn(Collections.emptyList());
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);
        ImportWorkService classUnderTest =
            new ImportWorkService(importService, fileStorageService, azureStorageConfiguration);

        classUnderTest.doImport(fileBytes, file, false, false, jobId);

        verify(fileStorageService, never()).uploadFile(any(), any());
    }

    @DisplayName("Azure upload skipped when FileStorageService is null even with upload enabled")
    @Test
    void azureSkippedWhenFileStorageServiceNull() throws Exception {
        when(importService.importFormDefinitions(any(InputStream.class), anyBoolean(), anyBoolean()))
            .thenReturn(metadata);
        when(importService.getImportWarnings()).thenReturn(Collections.emptyList());
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
        ImportWorkService classUnderTest = new ImportWorkService(importService, null, azureStorageConfiguration);

        ImportWorkResult result = classUnderTest.doImport(fileBytes, file, false, false, jobId);

        assertEquals(metadata, result.metadata());
    }
}
