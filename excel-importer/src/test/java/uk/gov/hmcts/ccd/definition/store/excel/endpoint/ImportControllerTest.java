package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.FileStorageService;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.service.ImportServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.IMPORT_WARNINGS_HEADER;
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.URI_IMPORT;

class ImportControllerTest {

    @Mock
    private ImportServiceImpl importService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AzureStorageConfiguration azureStorageConfiguration;

    @InjectMocks
    private ImportController controller;

    private MockMvc mockMvc;

    private MockMultipartFile file;

    private DefinitionFileUploadMetadata metadata;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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
        mockMvc.perform(multipart(URI_IMPORT).file(file))
            .andExpect(status().isCreated())
            .andExpect(content().string("Case Definition data successfully imported"));
        verify(fileStorageService).uploadFile(file, metadata);
    }

    @DisplayName("Upload - Green path, Azure disabled")
    @Test
    void validUploadAzureDisabled() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);
        mockMvc.perform(multipart(URI_IMPORT).file(file))
            .andExpect(status().isCreated())
            .andExpect(content().string("Case Definition data successfully imported"));
        verify(fileStorageService, never()).uploadFile(file, metadata);
    }

    @DisplayName("Upload - non-Green path")
    @Test
    void invalidUpload() throws Exception {

        willThrow(new IOException("boo")).given(importService).importFormDefinitions(any());

        final IOException
            exception =
            assertThrows(IOException.class, () -> mockMvc.perform(multipart(URI_IMPORT).file(file)));
        assertThat(exception.getMessage(), is("boo"));
    }

    @DisplayName("Upload - warnings during import, Azure disabled")
    @Test
    void validUploadWithWarnings() throws Exception {
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(false);
        final String firstWarning = "First warning";
        final String secondWarning = "Second warning";
        when(importService.getImportWarnings()).thenReturn(Arrays.asList(firstWarning, secondWarning));
        mockMvc.perform(multipart(URI_IMPORT).file(file))
            .andExpect(status().isCreated())
            .andExpect(content().string("Case Definition data successfully imported"))
            .andExpect(header().stringValues(IMPORT_WARNINGS_HEADER, firstWarning, secondWarning));
    }
}
