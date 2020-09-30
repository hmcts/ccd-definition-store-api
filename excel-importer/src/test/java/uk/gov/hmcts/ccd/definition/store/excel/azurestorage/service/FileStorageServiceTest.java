package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FileStorageServiceTest {

    @Mock
    private AzureBlobStorageClient azureBlobStorageClient;

    @InjectMocks
    private FileStorageService serviceUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Should call the Azure Blob Storage Client to upload the file")
    void shouldUploadFile() throws Exception {
        MultipartFile file = new MockMultipartFile("x", "x".getBytes("UTF-8"));
        DefinitionFileUploadMetadata metadata = mock(DefinitionFileUploadMetadata.class);
        serviceUnderTest.uploadFile(file, metadata);
        verify(azureBlobStorageClient).uploadFile(file, metadata);
    }
}
