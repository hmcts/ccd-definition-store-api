package uk.gov.hmcts.ccd.definition.store.rest.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.rest.model.ImportAudit;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.sort;

@Service
@ConditionalOnProperty(name = "azure.storage.definition-upload-enabled")
public class AzureImportAuditsClient {

    public static final String USER_ID = "UserID";
    public static final String CASE_TYPES = "CaseTypes";

    private final CloudBlobContainer cloudBlobContainer;

    @Autowired
    public AzureImportAuditsClient(CloudBlobContainer cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    /**
     * Fetches All import audits.
     * @return import audits in reverse chronological order
     * @throws StorageException
     */
    public List<ImportAudit> fetchAllImportAudits() throws StorageException {
        List<ImportAudit> audits = new ArrayList<>();
        for (ListBlobItem lbi : cloudBlobContainer.listBlobs()) {
            if (lbi instanceof CloudBlockBlob) {
                final CloudBlockBlob cbb = (CloudBlockBlob)lbi;
                cbb.downloadAttributes();
                final ImportAudit audit = new ImportAudit();
                final BlobProperties properties = cbb.getProperties();
                final HashMap<String, String> metadata = cbb.getMetadata();
                audit.setDateImported(Instant.ofEpochMilli(properties.getCreatedTime().getTime())
                                             .atZone(ZoneId.systemDefault())
                                             .toLocalDate());
                audit.setWhoImported(metadata.get(USER_ID));
                audit.setCaseType(metadata.get(CASE_TYPES));
                audit.setFilename(cbb.getName());
                audit.setUri(cbb.getUri());
                audit.setOrder(properties.getCreatedTime());
                audits.add(audit);
            }
        }
        sort(audits, (o1, o2) -> o1.getOrder().compareTo(o2.getOrder()));
        return audits;
    }
}
