package uk.gov.hmcts.ccd.definition.store.domain.service.cache;

public interface CaseDefinitionCacheEvictor {

    void evictCaseTypeDefinition(String caseTypeId);

}
