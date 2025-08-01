package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.exception.FileStorageException;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.util.DateTimeStringGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AzureBlobStorageClientTest {

    private static final String DATE_TIME_PREFIX = "20181004120000";
    private static final String FILENAME = "Definition";
    private CloudBlobContainer cloudBlobContainer;
    private CloudBlockBlob cloudBlockBlob;

    @Mock
    private DateTimeStringGenerator dateTimeStringGenerator;

    @InjectMocks
    private AzureBlobStorageClient clientUnderTest;

    @BeforeEach
    void setUp() {
        cloudBlobContainer = mock(CloudBlobContainer.class);
        cloudBlockBlob = mock(CloudBlockBlob.class);
        MockitoAnnotations.openMocks(this);
        clientUnderTest = new AzureBlobStorageClient(cloudBlobContainer, dateTimeStringGenerator);
    }

    @Test
    void testFileUpload() throws Exception {
        when(cloudBlobContainer.getBlockBlobReference(DATE_TIME_PREFIX + "_" + FILENAME)).thenReturn(cloudBlockBlob);
        when(dateTimeStringGenerator.generateCurrentDateTime()).thenReturn(DATE_TIME_PREFIX);
        clientUnderTest.uploadFile(new MockMultipartFile("x", FILENAME, MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "x".getBytes("UTF-8")), mock(DefinitionFileUploadMetadata.class));
        verify(cloudBlockBlob).setMetadata(any());
        verify(cloudBlockBlob, times(1)).upload(any(InputStream.class), anyLong());
    }

    @Test
    void testFileUploadURISyntaxException() throws Exception {
        when(cloudBlobContainer.getBlockBlobReference(any(String.class)))
                .thenThrow(new URISyntaxException("Test", "Invalid"));
        assertThrows(FileStorageException.class, ()
                -> clientUnderTest.uploadFile(new MockMultipartFile("x", "x".getBytes("UTF-8")),
                        mock(DefinitionFileUploadMetadata.class))
        );
    }

    @Test
    void testFileUploadStorageException() throws Exception {
        when(cloudBlobContainer.getBlockBlobReference(any(String.class)))
                .thenThrow(new StorageException("1", "Storage error", null));
        assertThrows(FileStorageException.class, ()
                -> clientUnderTest.uploadFile(new MockMultipartFile("x", "x".getBytes("UTF-8")),
                        mock(DefinitionFileUploadMetadata.class))
        );
    }

    @Test
    void testFileUploadIOException() throws Exception {
        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenReturn(cloudBlockBlob);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException());
        assertThrows(FileStorageException.class, () -> 
            clientUnderTest.uploadFile(file, mock(DefinitionFileUploadMetadata.class)));
    }

    @Test
    void testInitStorageException() throws Exception {
        when(cloudBlobContainer.createIfNotExists()).thenThrow(new StorageException("1", "Storage error", null));
        assertThrows(StorageException.class, () -> clientUnderTest.init());
    }

    @Test
    void testInit() throws Exception {
        when(cloudBlobContainer.createIfNotExists()).thenReturn(true);
        clientUnderTest.init();
    }
}
