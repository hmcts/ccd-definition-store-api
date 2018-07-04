package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.util.List;

public interface MetadataFieldService {

    List<CaseField> getCaseMetadataFields(CaseType caseType);

}
