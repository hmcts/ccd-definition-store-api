package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.exception.FileStorageException;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.util.DateTimeStringGenerator;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;

import static uk.gov.hmcts.ccd.definition.store.rest.service.AzureImportAuditsClient.CASE_TYPES;
import static uk.gov.hmcts.ccd.definition.store.rest.service.AzureImportAuditsClient.USER_ID;

// Command to run unit tests :-
// ./gradlew excel-importer:test --tests uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.AzureBlobStorageClientTest
// ./gradlew excel-importer:test --tests uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.AzureBlobStorageClientTest.testInit
// ./gradlew excel-importer:test --tests uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.AzureBlobStorageClientTest.testUploadFile

@Service(value = "azureBlobStorageClient")
@ConditionalOnProperty(name = "azure.storage.definition-upload-enabled")
public class AzureBlobStorageClient implements FileStorageClient {

    private static final Logger LOG = AzureBlobStorageClientLogger.getLogger();

    private final CloudBlobContainer cloudBlobContainer;
    private final DateTimeStringGenerator dateTimeStringGenerator;

    @Autowired
    public AzureBlobStorageClient(CloudBlobContainer cloudBlobContainer,
                                  DateTimeStringGenerator dateTimeStringGenerator) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.dateTimeStringGenerator = dateTimeStringGenerator;
    }

    @PostConstruct
    protected void init() throws StorageException {
        cloudBlobContainer.createIfNotExists();
    }

    public void jclog(final String message) {
        LOG.info("JCDEBUG: info: AzureBlobStorageClient: " + message);
        LOG.warn("JCDEBUG: warn: AzureBlobStorageClient: " + message);
        LOG.error("JCDEBUG: error: AzureBlobStorageClient: " + message);
        LOG.debug("JCDEBUG: debug: AzureBlobStorageClient: " + message);
    }

    // See https://azure.github.io/ref-docs/java/com/microsoft/azure/storage/blob/CloudBlockBlob.html
    // See https://azure.github.io/ref-docs/java/com/microsoft/azure/storage/blob/BlobRequestOptions.html
    @Override
    public void uploadFile(MultipartFile multipartFile, DefinitionFileUploadMetadata metadata) {
        jclog("uploadFile() #1");
        try (final InputStream inputStream = multipartFile.getInputStream()) {
            jclog("uploadFile() #2");
            final CloudBlockBlob blob = getCloudFile(dateTimeStringGenerator.generateCurrentDateTime()
                + "_" + multipartFile.getOriginalFilename());
            jclog("uploadFile() #3");
            blob.setMetadata(createMetadataMap(metadata));
            jclog("uploadFile() #4 (size = " + (multipartFile == null ? "NULL" : multipartFile.getSize()) + ")");
            blob.upload(inputStream, multipartFile.getSize());
            jclog("uploadFile() #5 OK");

        } catch (URISyntaxException | StorageException | IOException e) {
            jclog("uploadFile() #6 Exception: " + e.getMessage());
            throw new FileStorageException(e);
        }

    }

    private CloudBlockBlob getCloudFile(String name) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(name);
    }

    private HashMap<String, String> createMetadataMap(DefinitionFileUploadMetadata metadata) {
        final HashMap<String, String> metadataMap = new HashMap<>();
        metadataMap.put("Jurisdiction", metadata.getJurisdiction());
        metadataMap.put(CASE_TYPES, metadata.getCaseTypesAsString());
        metadataMap.put(USER_ID, metadata.getUserId());
        return metadataMap;
    }
}
