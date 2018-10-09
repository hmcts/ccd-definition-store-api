package uk.gov.hmcts.ccd.definition.store.excel.azurestorage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.file.CloudFileClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Configuration
@ConditionalOnProperty(name = "azure.storage.definition-upload-enabled")
public class AzureStorageConfiguration {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.blob-container-reference}")
    private String containerReference;

    @Value("${azure.storage.definition-upload-enabled}")
    private Boolean azureUploadEnabled;

    @Bean
    public CloudStorageAccount storageAccount() throws URISyntaxException, InvalidKeyException {
        return CloudStorageAccount.parse(connectionString);
    }

    @Bean
    public CloudFileClient cloudFileClient() throws URISyntaxException, InvalidKeyException {
        return storageAccount().createCloudFileClient();
    }

    @Bean
    public CloudBlobClient cloudBlobClient() throws URISyntaxException, InvalidKeyException {
        return storageAccount().createCloudBlobClient();
    }

    @Bean
    public CloudBlobContainer cloudBlobContainer() throws URISyntaxException, InvalidKeyException, StorageException {
        return cloudBlobClient().getContainerReference(containerReference);
    }

    @Bean
    public Boolean isAzureUploadEnabled() {
        return azureUploadEnabled;
    }
}
