package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

public interface FileStorageClient {

    void uploadFile(MultipartFile file, DefinitionFileUploadMetadata metadata);
}
