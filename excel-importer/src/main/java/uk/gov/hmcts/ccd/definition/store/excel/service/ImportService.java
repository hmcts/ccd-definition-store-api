package uk.gov.hmcts.ccd.definition.store.excel.service;

import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ImportService {

    DefinitionFileUploadMetadata importFormDefinitions(InputStream inputStream) throws IOException;

    List<String> getImportWarnings();
}
