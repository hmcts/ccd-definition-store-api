package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.service.ProcessUploadService;
import uk.gov.hmcts.ccd.definition.store.excel.service.ProcessUploadServiceImpl;

import java.io.IOException;

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
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = ProcessUploadService.SUCCESSFULLY_CREATED),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 422, message = "Unprocessable Entity")
    })

    public ResponseEntity processUpload(@RequestParam("file") MultipartFile file) throws IOException {
        return processUploadServiceImpl.processUpload(file);
    }
}
