package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProcessUploadService {
    String SUCCESSFULLY_CREATED = "Case Definition data successfully imported";
    String IMPORT_WARNINGS_HEADER = "Definition-Import-Warnings";
    ResponseEntity processUpload(MultipartFile file) throws IOException;
}
