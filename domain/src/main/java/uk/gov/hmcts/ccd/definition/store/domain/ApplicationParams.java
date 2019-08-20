package uk.gov.hmcts.ccd.definition.store.domain;

import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import java.util.List;

@Named
@Singleton
public class ApplicationParams {

    @Value("${ccd.user-profile.host}")
    private String userProfileHost;

    @Value("${auth.idam.client.baseUrl}")
    private String idamHost;

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

    public String userProfilePutURL() {
        return userProfileHost + "/user-profile/users";
    }

    public String idamUserProfileURL() {
        return idamHost + "/details";
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

    @PostConstruct
    public void init() {
        new AmSwitchValidator().validateAmPersistenceSwitchesIn(this);
    }
}
