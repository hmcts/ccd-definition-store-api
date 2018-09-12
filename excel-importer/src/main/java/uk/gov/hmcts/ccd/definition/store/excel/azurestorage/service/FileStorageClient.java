package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

import java.io.OutputStream;
import java.util.UUID;

public interface FileStorageClient {

    void uploadFile(MultipartFile file, DefinitionFileUploadMetadata metadata);
}
