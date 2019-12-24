package uk.gov.hmcts.ccd.definition.store.rest.service;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.rest.model.ImportAudit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloudBlobContainer.class, CloudBlockBlob.class, BlobProperties.class})
public class AzureImportAuditsClientTest {

    public static final int IMPORT_AUDITS_GET_LIMIT = 20;
    private AzureImportAuditsClient subject;

    private CloudBlockBlob b1;

    private CloudBlockBlob b2;

    private BlobProperties p1;

    private BlobProperties p2;

    private ResultSegment blobsPage;

    @Before
    public void setUp() throws StorageException {
        final CloudBlobContainer cloudBlobContainer = mock(CloudBlobContainer.class);
        final ApplicationParams applicationParams = mock(ApplicationParams.class);
        b1 = mock(CloudBlockBlob.class);
        b2 = mock(CloudBlockBlob.class);
        p1 = mock(BlobProperties.class);
        p2 = mock(BlobProperties.class);
        blobsPage = mock(ResultSegment.class);

        MockitoAnnotations.initMocks(this);

        when(applicationParams.getAzureImportAuditsGetLimit()).thenReturn(IMPORT_AUDITS_GET_LIMIT);
        subject = new AzureImportAuditsClient(cloudBlobContainer, applicationParams);
        when(cloudBlobContainer.listBlobsSegmented(eq(null),
                                                   eq(true),
                                                   any(EnumSet.class),
                                                   eq(IMPORT_AUDITS_GET_LIMIT),
                                                   eq(null),
                                                   eq(null),
                                                   eq(null))).thenReturn(blobsPage);
        when(blobsPage.getResults()).thenReturn(Lists.newArrayList(b1, b2));
        when(b1.getProperties()).thenReturn(p1);
        when(b2.getProperties()).thenReturn(p2);

        when(b1.getMetadata()).thenReturn(new HashMap<>());
        when(b2.getMetadata()).thenReturn(new HashMap<>());

        when(b1.getName()).thenReturn("b1");
        when(b2.getName()).thenReturn("b2");

        final Date d1 = new Date();
        when(p1.getCreatedTime()).thenReturn(d1);

        final Date d2 = new Date(d1.getTime() + 1000);
        when(p2.getCreatedTime()).thenReturn(d2);

        assertTrue(d2.after(d1));
    }

    @Test
    public void shouldFetchAllImportAudits() throws Exception {
        final List<ImportAudit> audits = subject.fetchAllImportAudits();
        assertThat(audits.size(), is(2));
        assertThat(audits.get(0).getFilename(), is("b2"));
        assertThat(audits.get(1).getFilename(), is("b1"));

        verifyCloudBlockBlobBehaviour(b1, p1);
        verifyCloudBlockBlobBehaviour(b2, p2);
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
