package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

@Service
@ConditionalOnProperty(name = "azure.storage.definition-upload-enabled")
public class FileStorageService {

    private final AzureBlobStorageClient blobStorageClient;

    @Autowired
    public FileStorageService(AzureBlobStorageClient blobStorageClient) {
        this.blobStorageClient = blobStorageClient;
    }

    public void uploadFile(MultipartFile file, DefinitionFileUploadMetadata metadata) {
        blobStorageClient.uploadFile(file, metadata);
    }
}
