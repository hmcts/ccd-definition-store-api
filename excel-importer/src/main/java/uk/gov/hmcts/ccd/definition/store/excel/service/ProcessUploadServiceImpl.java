package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.FileStorageService;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ProcessUploadServiceImpl implements ProcessUploadService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessUploadServiceImpl.class);
    private final ImportServiceImpl importService;
    private final FileStorageService fileStorageService;
    private final AzureStorageConfiguration azureStorageConfiguration;

    @Value("${ccd.delete-old-index:false}")
    private boolean deleteOldIndexDefault;

    @Autowired
    public ProcessUploadServiceImpl(ImportServiceImpl importService,
                                    @Autowired(required = false) FileStorageService fileStorageService,
                                    @Autowired(required = false) AzureStorageConfiguration azureStorageConfiguration) {
        this.importService = importService;
        this.fileStorageService = fileStorageService;
        this.azureStorageConfiguration = azureStorageConfiguration;
    }

    @Transactional
    @Override
    public ResponseEntity<String> processUpload(MultipartFile file, boolean reindex, boolean deleteOldIndex)
        throws IOException {

        if (file == null || file.getSize() == 0) {
            throw new IOException(IMPORT_FILE_ERROR);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (final InputStream inputStream = file.getInputStream()) {
                IOUtils.copy(inputStream, baos);
            }
            byte[] bytes = baos.toByteArray();
            LOG.info("Importing Definition file...");

            //deleteOldIndex functionality disabled by default
            deleteOldIndex = this.deleteOldIndexDefault;
            final DefinitionFileUploadMetadata metadata =
                importService.importFormDefinitions(new ByteArrayInputStream(bytes), reindex, deleteOldIndex);

            if (azureStorageConfiguration != null
                && azureStorageConfiguration.isAzureUploadEnabled()
                && fileStorageService != null) {
                LOG.info("Uploading Definition file to Azure Storage...");
                fileStorageService.uploadFile(file, metadata);
            }

            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.CREATED);

            if (!importService.getImportWarnings().isEmpty()) {
                for (String warning : importService.getImportWarnings()) {
                    LOG.warn(warning);
                }
                responseBuilder.header(IMPORT_WARNINGS_HEADER,
                    importService.getImportWarnings().toArray(new String[0]));
            }

            if (reindex) {
                responseBuilder.header("Elasticsearch-Reindex-Task", metadata.getTaskId());
            }

            return responseBuilder.body(SUCCESSFULLY_CREATED);
        }
    }
}
