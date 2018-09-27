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
import org.springframework.context.annotation.PropertySource;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@ConditionalOnProperty(value = "azureStorageConfig", havingValue = "enabled")
@Configuration
@PropertySource("classpath:application.properties")
public class AzureStorageConfiguration {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.blob-container-reference}")
    private String containerReference;

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
}
