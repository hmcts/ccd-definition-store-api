package uk.gov.hmcts.ccd.definition.store.excel.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

public interface ImportService {

    DefinitionFileUploadMetadata importFormDefinitions(InputStream inputStream) throws IOException;

    List<String> getImportWarnings();
}
