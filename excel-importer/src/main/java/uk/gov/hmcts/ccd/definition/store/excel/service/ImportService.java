package uk.gov.hmcts.ccd.definition.store.excel.service;

import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;

import java.io.IOException;
import java.io.InputStream;

public interface ImportService {

    DefinitionFileUploadMetadata importFormDefinitions(InputStream inputStream) throws IOException, InvalidImportException;
}
