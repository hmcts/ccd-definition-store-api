package uk.gov.hmcts.ccd.definition.store.domain;

import com.hazelcast.config.EvictionPolicy;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

@Named
@Singleton
public class ApplicationParams {

    @Value("${ccd.user-profile.host}")
    private String userProfileHost;

    @Value("#{'${ccd.am.write.to_ccd_only}'.split(',')}")
    private List<String> caseTypesWithAmWrittenOnlyToCcd;

    @Value("#{'${ccd.am.write.to_am_only}'.split(',')}")
    private List<String> caseTypesWithAmWrittenOnlyToAm;

    @Value("#{'${ccd.am.write.to_both}'.split(',')}")
    private List<String> caseTypesWithAmWrittenToBoth;

    @Value("#{'${ccd.am.read.from_ccd}'.split(',')}")
    private List<String> caseTypesWithAmReadFromCcd;

    @Value("#{'${ccd.am.read.from_am}'.split(',')}")
    private List<String> caseTypesWithAmReadFromAm;

    @Value("${azure.storage.import_audits.get-limit}")
    private String azureImportAuditsGetLimit;

    @Value("${definition.cache.max.size}")
    private Integer definitionCacheMaxSize;

    @Value("${definition.cache.eviction.policy}")
    private EvictionPolicy definitionCacheEvictionPolicy;

    @Value("${user.cache.ttl.secs}")
    private Integer userCacheTTLSecs;

    public String userProfilePutURL() {
        return userProfileHost + "/user-profile/users";
    }

    public List<String> getCaseTypesWithAmWrittenOnlyToCcd() {
        return caseTypesWithAmWrittenOnlyToCcd;
    }

    public List<String> getCaseTypesWithAmWrittenOnlyToAm() {
        return caseTypesWithAmWrittenOnlyToAm;
    }

    public List<String> getCaseTypesWithAmWrittenToBoth() {
        return caseTypesWithAmWrittenToBoth;
    }

    public List<String> getCaseTypesWithAmReadFromCcd() {
        return caseTypesWithAmReadFromCcd;
    }

    public List<String> getCaseTypesWithAmReadFromAm() {
        return caseTypesWithAmReadFromAm;
    }

    public Integer getAzureImportAuditsGetLimit() {
        return Integer.valueOf(azureImportAuditsGetLimit);
    }

    @PostConstruct
    public void init() {
        new AmSwitchValidator().validateAmPersistenceSwitchesIn(this);
    }

    public Integer getUserCacheTTLSecs() {
        return userCacheTTLSecs;
    }

    public EvictionPolicy getDefinitionCacheEvictionPolicy() {
        return definitionCacheEvictionPolicy;
    }

    public Integer getDefinitionCacheMaxSize() {
        return definitionCacheMaxSize;
    }
}
