package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ImportServiceImpl importService;
    private FileStorageService fileStorageService;
    private AzureStorageConfiguration azureStorageConfiguration;

    @Autowired
    public ProcessUploadServiceImpl(ImportServiceImpl importService,
                                    @Autowired(required = false) FileStorageService fileStorageService,
                                    @Autowired(required = false) AzureStorageConfiguration azureStorageConfiguration) {
        this.importService = importService;
        this.fileStorageService = fileStorageService;
        this.azureStorageConfiguration = azureStorageConfiguration;
    }

    private void jclog(final String message) {
        LOG.info("JCDEBUG: info: ProcessUploadServiceImpl: " + message);
        LOG.warn("JCDEBUG: warn: ProcessUploadServiceImpl: " + message);
        LOG.error("JCDEBUG: error: ProcessUploadServiceImpl: " + message);
        LOG.debug("JCDEBUG: debug: ProcessUploadServiceImpl: " + message);
    }

    @Transactional
    @Override
    public ResponseEntity processUpload(MultipartFile file) throws IOException {

        jclog("processUpload() #1");
        if (file == null || file.getSize() == 0) {
            throw new IOException(IMPORT_FILE_ERROR);
        } else {
            jclog("processUpload() #2");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (final InputStream inputStream = file.getInputStream()) {
                IOUtils.copy(inputStream, baos);
            }
            byte[] bytes = baos.toByteArray();
            LOG.info("Importing Definition file...");
            jclog("processUpload() #3 ---> importService.importFormDefinitions()");
            final DefinitionFileUploadMetadata metadata =
                importService.importFormDefinitions(new ByteArrayInputStream(bytes));
            jclog("processUpload() #4 <--- importService.importFormDefinitions() ");

            if (azureStorageConfiguration != null
                && azureStorageConfiguration.isAzureUploadEnabled()
                && fileStorageService != null) {
                LOG.info("Uploading Definition file to Azure Storage...");
                jclog("processUpload() #5 ---> fileStorageService.uploadFile()");
                fileStorageService.uploadFile(file, metadata);
                jclog("processUpload() #6 <--- fileStorageService.uploadFile()");
            } else {
                jclog("processUpload() #7");
            }

            if (!importService.getImportWarnings().isEmpty()) {
                jclog("processUpload() #8 (" + importService.getImportWarnings().size() + " warnings)");
                for (String warning : importService.getImportWarnings()) {
                    LOG.warn(warning);
                }
                return ResponseEntity.status(HttpStatus.CREATED)
                    .header(IMPORT_WARNINGS_HEADER, importService.getImportWarnings().toArray(new String[0]))
                    .body(SUCCESSFULLY_CREATED);
            }
            jclog("processUpload() #9 (no warnings)");
            return ResponseEntity.status(HttpStatus.CREATED).body(SUCCESSFULLY_CREATED);
        }
    }
}
