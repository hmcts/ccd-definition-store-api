package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.service.ProcessUploadServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.URI_IMPORT;

class ImportControllerTest {

    @Mock
    private ProcessUploadServiceImpl processUploadServiceImpl;

    @InjectMocks
    private ImportController controller;

    private MockMvc mockMvc;

    private MockMultipartFile file;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        file =
            new MockMultipartFile("file",
                Files.readAllBytes(new File("src/test/resources/CCD_TestDefinition.xlsx")
                    .toPath()));
        DefinitionFileUploadMetadata metadata = new DefinitionFileUploadMetadata();
        metadata.setJurisdiction("TEST");
        metadata.addCaseType("TestCaseType");
        metadata.setUserId("user@hmcts.net");
    }

    @DisplayName("Upload - Green path, Azure enabled")
    @Test
    void validUploadAzureEnabled() throws Exception {
        val expectedResult = "Case Definition data successfully imported";
        val response = ResponseEntity.status(HttpStatus.CREATED).body(expectedResult);
        when(processUploadServiceImpl.processUpload(file, false, false)).thenReturn(response);

        mockMvc.perform(multipart(URI_IMPORT).file(file))
            .andExpect(status().isCreated())
            .andExpect(content().string("Case Definition data successfully imported"));
        verify(processUploadServiceImpl).processUpload(file, false, false);
    }

    @DisplayName("Upload - Green path, Azure disabled")
    @Test
    void validUploadAzureDisabled() throws Exception {
        val expectedResult = "Case Definition data successfully imported";
        val response = ResponseEntity.status(HttpStatus.CREATED).body(expectedResult);
        when(processUploadServiceImpl.processUpload(file, false, false)).thenReturn(response);

        mockMvc.perform(multipart(URI_IMPORT).file(file))
            .andExpect(status().isCreated())
            .andExpect(content().string(expectedResult));
        verify(processUploadServiceImpl).processUpload(file, false, false);
    }
}
