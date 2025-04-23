package uk.gov.hmcts.ccd.definition.store.repository.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;


import org.hibernate.annotations.UpdateTimestamp;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Table(name = "definition_designer")
@Entity
public class DefinitionEntity implements Serializable, Versionable {

    @Id
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "jurisdiction_id", nullable = false)
    private JurisdictionEntity jurisdiction;

    @Column(name = "case_types")
    private String caseTypes;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DefinitionStatus status;

    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    @Type(JsonNodeBinaryType.class)
    private JsonNode data;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "last_modified", nullable = false, insertable = false)
    @UpdateTimestamp
    private LocalDateTime lastModified;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Version
    @Column(name = "optimistic_lock", nullable = false)
    private Long optimisticLock;

    public Integer getId() {
        return id;
    }

    public JurisdictionEntity getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(JurisdictionEntity jurisdiction) {
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
        return status;
    }

    public void setStatus(DefinitionStatus status) {
        this.status = status;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
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

    public String getReference() {
        // No-op
        return null;
    }

    public Long getOptimisticLock() {
        return optimisticLock;
    }
}
