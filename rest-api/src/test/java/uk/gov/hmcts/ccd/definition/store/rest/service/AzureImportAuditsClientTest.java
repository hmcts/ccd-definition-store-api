package uk.gov.hmcts.ccd.definition.store.rest.service;

import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.rest.model.ImportAudit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AzureImportAuditsClientTest {

    public static final int IMPORT_AUDITS_GET_LIMIT = 5;

    public static final String SSL_CONTEXT_PROTOCOL_1_DOT_2 = "TLSv1.2";
    public static final String SSL_CONTEXT_PROTOCOL_TLS = "TLS";

    private AzureImportAuditsClient subject;

    private CloudBlockBlob b11;
    private CloudBlockBlob b12;
    private CloudBlockBlob b21;
    private CloudBlockBlob b22;
    private CloudBlockBlob b31;
    private CloudBlockBlob b32;

    private BlobProperties p11;
    private BlobProperties p12;
    private BlobProperties p21;
    private BlobProperties p22;
    private BlobProperties p31;
    private BlobProperties p32;

    private ResultSegment blobsPage1;
    private ResultSegment blobsPage2;
    private ResultSegment blobsPage3;

    private ResultSegment blobsPage;

    final CloudBlobContainer cloudBlobContainer = mock(CloudBlobContainer.class);

    private SSLContext context;

    private MockedStatic<SSLContext> sslContextMockedStatic;
    private SSLSocketFactory socketFactory;

    @BeforeEach
    void setUp() throws StorageException, NoSuchAlgorithmException {
        final ApplicationParams applicationParams = mock(ApplicationParams.class);

        b11 = mock(CloudBlockBlob.class);
        b12 = mock(CloudBlockBlob.class);
        b21 = mock(CloudBlockBlob.class);
        b22 = mock(CloudBlockBlob.class);
        b31 = mock(CloudBlockBlob.class);
        b32 = mock(CloudBlockBlob.class);
        p11 = mock(BlobProperties.class);
        p12 = mock(BlobProperties.class);
        p21 = mock(BlobProperties.class);
        p22 = mock(BlobProperties.class);
        p31 = mock(BlobProperties.class);
        p32 = mock(BlobProperties.class);
        blobsPage1 = mock(ResultSegment.class);
        blobsPage2 = mock(ResultSegment.class);
        blobsPage3 = mock(ResultSegment.class);

        MockitoAnnotations.openMocks(this);

        // create the mock to return by getInstance()
        context = mock(SSLContext.class);
        SSLContext.setDefault(context);

        // mock the static method getInstance() to return above created mock context
        sslContextMockedStatic = mockStatic(SSLContext.class);
        when(SSLContext.getInstance(SSL_CONTEXT_PROTOCOL_1_DOT_2)).thenReturn(context);
        when(SSLContext.getInstance(SSL_CONTEXT_PROTOCOL_TLS)).thenReturn(context);

        //socketFactor mock required otherwise test hangs
        socketFactory = mock(SSLSocketFactory.class);
        when(context.getSocketFactory()).thenReturn(socketFactory);

        when(applicationParams.getAzureImportAuditsGetLimit()).thenReturn(IMPORT_AUDITS_GET_LIMIT);
        subject = new AzureImportAuditsClient(cloudBlobContainer, applicationParams);

        when(cloudBlobContainer.listBlobsSegmented(anyString(),
            eq(true),
            any(EnumSet.class),
            eq(Integer.MAX_VALUE),
            eq(null),
            eq(null),
            eq(null)))
            .thenReturn(blobsPage1)
            .thenReturn(blobsPage2)
            .thenReturn(blobsPage3);

        when(blobsPage1.getResults()).thenReturn(newArrayList(b11, b12));
        when(blobsPage2.getResults()).thenReturn(newArrayList(b21, b22));
        when(blobsPage3.getResults()).thenReturn(newArrayList(b31, b32));
        when(b11.getProperties()).thenReturn(p11);
        when(b12.getProperties()).thenReturn(p12);
        when(b21.getProperties()).thenReturn(p21);
        when(b22.getProperties()).thenReturn(p22);
        when(b31.getProperties()).thenReturn(p31);
        when(b32.getProperties()).thenReturn(p32);

        newArrayList(b11, b12, b21, b22, b31, b32).forEach(
            blob -> when(blob.getMetadata()).thenReturn(new HashMap<>()));

        when(b11.getName()).thenReturn("b11");
        when(b12.getName()).thenReturn("b12");
        when(b21.getName()).thenReturn("b21");
        when(b22.getName()).thenReturn("b22");
        when(b31.getName()).thenReturn("b31");
        when(b32.getName()).thenReturn("b32");

        final Date currentDate = new Date();
        ArrayList<BlobProperties> blobProperties = newArrayList(p11, p12, p21, p22, p31, p32);
        for (int i = 0, millisec = 0; i < blobProperties.size(); i += 2, millisec += 2000) {
            BlobProperties props = blobProperties.get(i);
            BlobProperties propsAfter = blobProperties.get(i + 1);
            final Date firstDate = new Date(currentDate.getTime() + millisec);
            final Date secondDate = new Date(currentDate.getTime() + millisec + 1000);
            when(props.getCreatedTime()).thenReturn(firstDate);
            when(propsAfter.getCreatedTime()).thenReturn(secondDate);
            assertTrue(secondDate.after(firstDate));
        }
    }

    @AfterEach
    void tearDown() {
        sslContextMockedStatic.close();
    }

    @Test
    void shouldFetchAllImportAuditsInCorrectDescOrder() throws Exception {
        final List<ImportAudit> audits = subject.fetchLatestImportAudits();
        assertThat(audits.size(), is(5));
        assertThat(audits.get(0).getFilename(), is("b32"));
        assertThat(audits.get(1).getFilename(), is("b31"));
        assertThat(audits.get(2).getFilename(), is("b22"));
        assertThat(audits.get(3).getFilename(), is("b21"));
        assertThat(audits.get(4).getFilename(), is("b12"));

        verifyCloudBlockBlobBehaviour(b32, p32);
        verifyCloudBlockBlobBehaviour(b31, p31);
        verifyCloudBlockBlobBehaviour(b22, p22);
        verifyCloudBlockBlobBehaviour(b21, p21);
        verifyCloudBlockBlobBehaviour(b12, p12);
        verifyCloudBlockBlobBehaviour(b11, p11);
    }

    @Test
    void shouldFetchNoImportAuditsWhenNoPrefixFound() throws Exception {

        int maxDaysToCheck = 10 + IMPORT_AUDITS_GET_LIMIT * 5;
        //maxDaysToCheck starts at 0
        maxDaysToCheck = maxDaysToCheck + 1;

        when(blobsPage1.getResults()).thenReturn(newArrayList());
        when(blobsPage2.getResults()).thenReturn(newArrayList());
        when(blobsPage3.getResults()).thenReturn(newArrayList());

        final List<ImportAudit> audits = subject.fetchLatestImportAudits();
        assertThat(audits.size(), is(0));
        verify(cloudBlobContainer, times(maxDaysToCheck))
            .listBlobsSegmented(anyString(),
                eq(true),
                any(EnumSet.class),
                eq(Integer.MAX_VALUE),
                eq(null),
                eq(null),
                eq(null));
    }

    private void verifyCloudBlockBlobBehaviour(CloudBlockBlob blob, BlobProperties properties) throws Exception {
        verify(blob).downloadAttributes();
        verify(blob).getProperties();
        verify(blob).getMetadata();
        verify(blob).getName();
        verify(blob).getUri();
        verify(properties).getCreatedTime();
    }
}
