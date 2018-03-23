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
import uk.gov.hmcts.ccd.definition.store.excel.service.ImportServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.URI_IMPORT;

class ImportControllerTest {

    @Mock private ImportServiceImpl importService;

    @InjectMocks private ImportController controller;

    private MockMvc mockMvc;

    private MockMultipartFile file;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        file =
            new MockMultipartFile("file",
                                  Files.readAllBytes(new File("src/test/resources/CCD_TestDefinition.xlsx")
                                                         .toPath()));
    }

    @DisplayName("Upload - Green path")
    @Test
    void validUpload() throws Exception {
        mockMvc.perform(fileUpload(URI_IMPORT).file(file))
               .andExpect(status().isCreated())
               .andExpect(content().string("Case Definition data successfully imported"));
    }

    @DisplayName("Upload - non Green path")
    @Test
    void invalidUpload() throws Exception {

        willThrow(new IOException("boo")).given(importService).importFormDefinitions(any());

        final IOException
            exception =
            assertThrows(IOException.class, () -> mockMvc.perform(fileUpload(URI_IMPORT).file(file)));
        assertThat(exception.getMessage(), is("boo"));
    }
}
