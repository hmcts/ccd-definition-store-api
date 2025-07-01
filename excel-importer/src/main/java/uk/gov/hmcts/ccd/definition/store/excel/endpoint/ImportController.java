package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

    @RequestMapping(value = URI_IMPORT, method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    @ApiResponses(value = {
        @ApiResponse(code = 201, message = ProcessUploadService.SUCCESSFULLY_CREATED),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 422, message = "Unprocessable Entity"),
        @ApiResponse(code = 400, message = "A definition must contain exactly one Jurisdiction \t\n "
            + "A definition must contain at least one Case Type \t\n "
            + "A definition must contain a Case Field worksheet \t\n "
            + "A definition must contain a Complex Types worksheet \t\n "
            + "A definition must contain a Fixed List worksheet \t\n "
            + "Display context parameter {} has been incorrectly configured or is "
            + "invalid for field {} on tab {} \t\n "
            + "Error processing sheet {} Invalid Case Definition sheet - no Definition "
            + "name found in Cell A1"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })

    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "file",
                value = "File to upload",
                required = true,
                dataType = "file",
                paramType = "form"
                ),
    })
    public ResponseEntity<String> processUpload(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "reindex", required = false, defaultValue = "false") Boolean reindex,
        @RequestParam(value = "deleteOldIndex", required = false, defaultValue = "false") Boolean deleteOldIndex
    ) throws IOException {
        return processUploadServiceImpl.processUpload(file, reindex, deleteOldIndex);

    }
}
