package uk.gov.hmcts.ccd.definition.store.domain.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultCaseDefinitionCacheEvictor implements CaseDefinitionCacheEvictor {

    @Override
    @Caching(evict = {
        @CacheEvict(value = "caseTypeDefinitionCache", key = "#caseTypeId"),
        @CacheEvict(value = "caseTypeVersionCache", key = "#caseTypeId")})
    public void evictCaseTypeDefinition(String caseTypeId) {
        log.info("Cleared cache for case type: {}", caseTypeId);
    }

}
