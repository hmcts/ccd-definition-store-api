package uk.gov.hmcts.ccd.definition.store.rest.service;

import com.google.common.collect.Lists;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.rest.model.ImportAudit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.sort;

@Service
@ConditionalOnProperty(name = "azure.storage.definition-upload-enabled")
public class AzureImportAuditsClient {

    public static final String USER_ID = "UserID";
    public static final String CASE_TYPES = "CaseTypes";

    private static final String DATE_PATTERN = "yyyyMMdd";

    private static final boolean FLAT_BLOB_LISTING = true;
    private static final ResultContinuation NO_CONTINUATION_TOKEN = null;
    private static final BlobRequestOptions NO_OPTIONS = null;
    private static final OperationContext NO_OP_CONTEXT = null;
    private static final EnumSet<BlobListingDetails> ONLY_COMMITTED_BLOBS = EnumSet.noneOf(BlobListingDetails.class);

    private final CloudBlobContainer cloudBlobContainer;
    private final ApplicationParams applicationParams;

    @Autowired
    public AzureImportAuditsClient(CloudBlobContainer cloudBlobContainer, ApplicationParams applicationParams) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.applicationParams = applicationParams;
    }

    /**
     * Fetches configurable size of import audits.
     *
     * @return import audits in reverse chronological order based on AZURE_STORAGE_IMPORT_AUDITS_GET_LIMIT
     * @throws StorageException Exception thrown when trying to connect to Azure Blob store
     */

    public List<ImportAudit> fetchLatestImportAudits() throws StorageException {

        List<ImportAudit> audits = new ArrayList<>();

        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("UTC"));
        String currentDateTime;
        Integer azureImportAuditsGetLimit = applicationParams.getAzureImportAuditsGetLimit();

        while (audits.size() < azureImportAuditsGetLimit) {
            currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
            ResultSegment<ListBlobItem> blobsPage = cloudBlobContainer.listBlobsSegmented(currentDateTime,
                FLAT_BLOB_LISTING,
                ONLY_COMMITTED_BLOBS,
                Integer.MAX_VALUE,
                NO_CONTINUATION_TOKEN,
                NO_OPTIONS,
                NO_OP_CONTEXT);
            localDateTime = localDateTime.minus(1, ChronoUnit.DAYS);
            List<ImportAudit> auditsLastBatch = populateListOfAudits(blobsPage);

            audits.addAll(auditsLastBatch);
        }
        sort(audits, (o1, o2) -> o2.getOrder().compareTo(o1.getOrder()));
        return audits.stream().limit(azureImportAuditsGetLimit).collect(Collectors.toList());
    }

    private List<ImportAudit> populateListOfAudits(ResultSegment<ListBlobItem> blobsPage) throws StorageException {
        List<ImportAudit> auditsLastBatch = Lists.newArrayList();

        for (ListBlobItem lbi : blobsPage.getResults()) {
            if (lbi instanceof CloudBlockBlob) {
                final CloudBlockBlob cbb = (CloudBlockBlob) lbi;
                cbb.downloadAttributes();
                final ImportAudit audit = new ImportAudit();
                final BlobProperties properties = cbb.getProperties();
                final HashMap<String, String> metadata = cbb.getMetadata();
                final Date createdTime = properties.getCreatedTime();
                audit.setDateImported(Instant.ofEpochMilli(createdTime.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());
                audit.setWhoImported(metadata.get(USER_ID));
                audit.setCaseType(metadata.get(CASE_TYPES));
                audit.setFilename(cbb.getName());

                audit.setUri(cbb.getUri());
                audit.setOrder(createdTime);
                auditsLastBatch.add(audit);
            }
        }
        return auditsLastBatch;
    }

}
