package uk.gov.hmcts.ccd.definition.store.excel.service;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;

public interface ImportService {

    void importFormDefinitions(InputStream inputStream) throws IOException, InvalidImportException;
}
