package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.service.ProcessUploadServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController.URI_IMPORT;

/**
 * Controller that exposes an HTTP POST endpoint for the importer, for uploading Core Case Definition data as a
 * spreadsheet to be imported.
 */
@RestController
@Api(value = URI_IMPORT)
public class ImportController {

    private static final Logger LOG = LoggerFactory.getLogger(ImportController.class);

    public static final String URI_IMPORT = "/import";
    public static final String IMPORT_WARNINGS_HEADER = "Definition-Import-Warnings";

    private ProcessUploadServiceImpl processUploadServiceImpl;

    @Autowired
    public ImportController(ProcessUploadServiceImpl processUploadServiceImpl) {
        this.processUploadServiceImpl = processUploadServiceImpl;
    }

    private void jclog(final String message) {
        LOG.info("JCDEBUG: info: ImportController: {}", message);
    }

    /*
     * Logging added to classes / methods :-
     * ProcessUploadServiceImpl.java    processUpload()
     * ImportServiceImpl.java           importFormDefinitions()
     * AzureBlobStorageClient.java      uploadFile()
     */
    @RequestMapping(value = URI_IMPORT, method = RequestMethod.POST)
    public ResponseEntity processUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        jclog("processUpload() #1");
        jclog("processUpload() #2  ,  " + request.getRequestURI() + "  ,  " + request.getRequestURL());
        ResponseEntity responseEntity = processUploadServiceImpl.processUpload(file);
        jclog("processUpload() #3 (OK)");
        return responseEntity;
    }
}
