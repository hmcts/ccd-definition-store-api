package uk.gov.hmcts.net.ccd.definition.store.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;
import java.util.Map;

/**
 * POJO representation of the display_items database table.
 *
 * @author Daniel Lam (A533913)
 */
public class DisplayItemsData {

    private String caseTypeId;
    private Date version;
    private Integer reference;
    private Map<String, JsonNode> displayObject;
    private Integer displayItemVersion;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
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

    public Map<String, JsonNode> getDisplayObject() {
        return displayObject;
    }

    public void setDisplayObject(Map<String, JsonNode> displayObject) {
        this.displayObject = displayObject;
    }

    public Integer getDisplayItemVersion() {
        return displayItemVersion;
    }

    public void setDisplayItemVersion(Integer displayItemVersion) {
        this.displayItemVersion = displayItemVersion;
    }

}
