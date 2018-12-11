package uk.gov.hmcts.ccd.definition.store.domain.service;

import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.model.Definition;

public interface DefinitionService {

    ServiceResponse<Definition> createDraftDefinition(Definition definition);

    Definition findLatestByJurisdictionId(String jurisdiction);
}
