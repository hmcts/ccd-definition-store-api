package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import uk.gov.hmcts.ccd.definition.store.excel.service.ProcessUploadServiceImpl;

import java.io.IOException;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.URI_IMPORT;

/**
 * Controller that exposes an HTTP POST endpoint for the importer, for uploading Core Case Definition data as a
 * spreadsheet to be imported.
 */
@RestController
@Api(value = URI_IMPORT)
public class ImportController {

    public static final String URI_IMPORT = "/import";
    public static final String IMPORT_WARNINGS_HEADER = "Definition-Import-Warnings";

    private ProcessUploadServiceImpl processUploadServiceImpl;

    @Autowired
    public ImportController(ProcessUploadServiceImpl processUploadServiceImpl) {
        this.processUploadServiceImpl = processUploadServiceImpl;
    }

    @RequestMapping(value = URI_IMPORT, method = RequestMethod.POST)
    public ResponseEntity<String> processUpload(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "reindex", required = false, defaultValue = "false") Boolean reindex,
        @RequestParam(value = "deleteOldIndex", required = false, defaultValue = "false") Boolean deleteOldIndex
    ) throws IOException {
        return processUploadServiceImpl.processUpload(file, reindex, deleteOldIndex);
    }
}
