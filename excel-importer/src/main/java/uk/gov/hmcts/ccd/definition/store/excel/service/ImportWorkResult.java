package uk.gov.hmcts.ccd.definition.store.excel.service;

import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;

import java.util.List;

public record ImportWorkResult(DefinitionFileUploadMetadata metadata, List<String> warnings) {
}
