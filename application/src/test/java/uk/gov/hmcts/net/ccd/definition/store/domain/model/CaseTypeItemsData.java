package uk.gov.hmcts.net.ccd.definition.store.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;
import java.util.Map;

/**
 * POJO representation of the case_type_items database table.
 *
 * @author Daniel Lam (A533913)
 */
public class CaseTypeItemsData {

    private String id;
    private String jurisdictionId;
    private Integer caseTypeVersion;
    private Date version;
    private Integer reference;
    private Map<String, JsonNode> caseType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public Integer getCaseTypeVersion() {
        return caseTypeVersion;
    }

    public void setCaseTypeVersion(Integer caseTypeVersion) {
        this.caseTypeVersion = caseTypeVersion;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public Integer getReference() {
        return reference;
    }

    public void setReference(Integer reference) {
        this.reference = reference;
    }

    public Map<String, JsonNode> getCaseType() {
        return caseType;
    }

    public void setCaseType(Map<String, JsonNode> caseType) {
        this.caseType = caseType;
    }

}
