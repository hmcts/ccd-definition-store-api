package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus;

import java.time.LocalDateTime;
import java.util.Map;

public class Definition {
    private Jurisdiction jurisdiction;
    @JsonProperty("case_types")
    private String caseTypes;
    private String description;
    private Integer version;
    @JsonProperty("status")
    private DefinitionStatus definitionStatus;
    private Map<String, JsonNode> data;
    private String author;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("last_modified")
    private LocalDateTime lastModified;
    @JsonProperty("deleted")
    private Boolean deleted;

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getCaseTypes() {
        return caseTypes;
    }

    public void setCaseTypes(String caseTypes) {
        this.caseTypes = caseTypes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public DefinitionStatus getStatus() {
        return definitionStatus;
    }

    public void setStatus(DefinitionStatus definitionStatus) {
        this.definitionStatus = definitionStatus;
    }

    public Map<String, JsonNode> getData() {
        return data;
    }

    public void setData(Map<String, JsonNode> data) {
        this.data = data;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
