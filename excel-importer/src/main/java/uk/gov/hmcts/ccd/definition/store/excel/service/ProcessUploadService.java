package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface ProcessUploadService {
    String SUCCESSFULLY_CREATED = "Case Definition data successfully imported";
    String IMPORT_WARNINGS_HEADER = "Definition-Import-Warnings";
    String IMPORT_FILE_ERROR = "No file present or file has zero length";

    ProcessUploadResult processUpload(MultipartFile file, boolean reindex, boolean deleteOldIndex, UUID providedJobId)
        throws IOException;

}
