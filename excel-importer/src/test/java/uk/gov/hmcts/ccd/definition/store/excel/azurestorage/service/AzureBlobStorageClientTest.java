package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.exception.FileStorageException;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.util.DateTimeStringGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloudBlobContainer.class, CloudBlockBlob.class})
public class AzureBlobStorageClientTest {

    private static final String DATE_TIME_PREFIX = "20181004120000";
    private static final String FILENAME = "Definition";
    private CloudBlobContainer cloudBlobContainer;
    private CloudBlockBlob cloudBlockBlob;

    @Mock
    private DateTimeStringGenerator dateTimeStringGenerator;

    @InjectMocks
    private AzureBlobStorageClient clientUnderTest;

    @Before
    public void setUp() {
        cloudBlobContainer = PowerMockito.mock(CloudBlobContainer.class);
        cloudBlockBlob = PowerMockito.mock(CloudBlockBlob.class);
        MockitoAnnotations.initMocks(this);
        clientUnderTest = new AzureBlobStorageClient(cloudBlobContainer, dateTimeStringGenerator);
    }

    @Test
    public void testFileUpload() throws Exception {
        when(cloudBlobContainer.getBlockBlobReference(DATE_TIME_PREFIX + "_" + FILENAME)).thenReturn(cloudBlockBlob);
        when(dateTimeStringGenerator.generateCurrentDateTime()).thenReturn(DATE_TIME_PREFIX);
        clientUnderTest.uploadFile(new MockMultipartFile("x", FILENAME, MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "x".getBytes("UTF-8")), mock(DefinitionFileUploadMetadata.class));
        verify(cloudBlockBlob).setMetadata(any());
        verify(cloudBlockBlob, times(1)).upload(any(InputStream.class), anyLong());
    }

    @Test(expected = FileStorageException.class)
    public void testFileUploadURISyntaxException() throws Exception {
        when(cloudBlobContainer.getBlockBlobReference(any(String.class)))
            .thenThrow(new URISyntaxException("Test", "Invalid"));
        clientUnderTest.uploadFile(new MockMultipartFile("x", "x".getBytes("UTF-8")),
            mock(DefinitionFileUploadMetadata.class));
    }

    @Test(expected = FileStorageException.class)
    public void testFileUploadStorageException() throws Exception {
        when(cloudBlobContainer.getBlockBlobReference(any(String.class)))
            .thenThrow(new StorageException("1", "Storage error", null));
        clientUnderTest.uploadFile(new MockMultipartFile("x", "x".getBytes("UTF-8")),
            mock(DefinitionFileUploadMetadata.class));
    }

    @Test(expected = FileStorageException.class)
    public void testFileUploadIOException() throws Exception {
        when(cloudBlobContainer.getBlockBlobReference(any(String.class))).thenReturn(cloudBlockBlob);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException());
        clientUnderTest.uploadFile(file, mock(DefinitionFileUploadMetadata.class));
    }

    @Test(expected = StorageException.class)
    public void testInitStorageException() throws Exception {
        when(cloudBlobContainer.createIfNotExists()).thenThrow(new StorageException("1", "Storage error", null));
        clientUnderTest.init();
    }

    @Test
    public void testInit() throws Exception {
        when(cloudBlobContainer.createIfNotExists()).thenReturn(true);
        clientUnderTest.init();
    }
}
