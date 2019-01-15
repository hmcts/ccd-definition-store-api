package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
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

@Service(value = "azureBlobStorageClient")
@ConditionalOnProperty(name = "azure.storage.definition-upload-enabled")
public class AzureBlobStorageClient implements FileStorageClient {

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

    @Override
    public void uploadFile(MultipartFile multipartFile, DefinitionFileUploadMetadata metadata) {
        try (final InputStream inputStream = multipartFile.getInputStream()) {
            final CloudBlockBlob blob = getCloudFile(dateTimeStringGenerator.generateCurrentDateTime()
                + "_" + multipartFile.getOriginalFilename());
            blob.setMetadata(createMetadataMap(metadata));
            blob.upload(inputStream, multipartFile.getSize());

        } catch (URISyntaxException | StorageException | IOException e) {
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
