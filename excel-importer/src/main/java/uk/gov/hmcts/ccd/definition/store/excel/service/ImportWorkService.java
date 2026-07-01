package uk.gov.hmcts.ccd.definition.store.excel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.FileStorageService;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
public class ImportWorkService {
    private final ImportServiceImpl importService;
    private final FileStorageService fileStorageService;
    private final AzureStorageConfiguration azureStorageConfiguration;

    public ImportWorkService(ImportServiceImpl importService,
                             @Autowired(required = false) FileStorageService fileStorageService,
                             @Autowired(required = false) AzureStorageConfiguration azureStorageConfiguration) {
        this.importService = importService;
        this.fileStorageService = fileStorageService;
        this.azureStorageConfiguration = azureStorageConfiguration;
    }

    @Transactional
    public ImportWorkResult doImport(byte[] fileBytes, MultipartFile file, boolean reindex, boolean deleteOldIndex,
                                     UUID jobId)
        throws IOException {
        log.info("Starting definition import. jobId={}", jobId);
        final DefinitionFileUploadMetadata metadata =
            importService.importFormDefinitions(new ByteArrayInputStream(fileBytes), reindex, deleteOldIndex);

        if (azureStorageConfiguration != null
            && azureStorageConfiguration.isAzureUploadEnabled()
            && fileStorageService != null) {
            log.info("Uploading Definition file to Azure Storage...");
            fileStorageService.uploadFile(file, metadata);
        }

        return new ImportWorkResult(metadata, new ArrayList<>(importService.getImportWarnings()));
    }
}
