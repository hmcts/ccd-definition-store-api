package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.URI_IMPORT;

import io.swagger.annotations.Api;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.FileStorageService;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.service.ImportServiceImpl;

/**
 * Controller that exposes an HTTP POST endpoint for the importer, for uploading Core Case Definition data as a
 * spreadsheet to be imported.
 */
@RestController
@Api(value = URI_IMPORT)
public class ImportController {

    public static final String URI_IMPORT = "/import";
    protected static final String IMPORT_WARNINGS_HEADER = "Definition-Import-Warnings";
    private static final Logger LOG = LoggerFactory.getLogger(ImportController.class);

    private ImportServiceImpl importService;
    private FileStorageService fileStorageService;
    private AzureStorageConfiguration azureStorageConfiguration;

    @Autowired
    public ImportController(ImportServiceImpl importService,
                            @Autowired(required = false) FileStorageService fileStorageService,
                            @Autowired(required = false) AzureStorageConfiguration azureStorageConfiguration) {
        this.importService = importService;
        this.fileStorageService = fileStorageService;
        this.azureStorageConfiguration = azureStorageConfiguration;
    }

    @Transactional
    @RequestMapping(value = URI_IMPORT, method = RequestMethod.POST)
    public ResponseEntity processUpload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.getSize() == 0) {
            throw new IOException("No file present or file has zero length");
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (final InputStream inputStream = file.getInputStream()) {
                IOUtils.copy(inputStream, baos);
            }
            byte[] bytes = baos.toByteArray();
            LOG.info("Importing Definition file...");
            final DefinitionFileUploadMetadata metadata =
                importService.importFormDefinitions(new ByteArrayInputStream(bytes));

            if (azureStorageConfiguration != null && azureStorageConfiguration.isAzureUploadEnabled() && fileStorageService != null) {
                LOG.info("Uploading Definition file to Azure Storage...");
                fileStorageService.uploadFile(file, metadata);
            }

            final String responseBody = "Case Definition data successfully imported";

            if (!importService.getImportWarnings().isEmpty()) {
                for (String warning : importService.getImportWarnings()) {
                    LOG.warn(warning);
                }
                return ResponseEntity.status(HttpStatus.CREATED)
                    .header(IMPORT_WARNINGS_HEADER, importService.getImportWarnings().toArray(new String[0]))
                    .body(responseBody);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        }
    }
}
