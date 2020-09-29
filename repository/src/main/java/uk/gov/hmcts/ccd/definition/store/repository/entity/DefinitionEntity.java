package uk.gov.hmcts.ccd.definition.store.repository.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "definition_designer")
@Entity
@TypeDefs({
    @TypeDef(
        name = "jsonb-node",
        typeClass = JsonNodeBinaryType.class
    ),
    @TypeDef(
        name = "pgsql_definitionstatus_enum",
        typeClass = PostgreSQLEnumType.class,
        parameters = @Parameter(name = "type",
            value = "uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus")
    )
})
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
    @Type(type = "pgsql_definitionstatus_enum")
    private DefinitionStatus status;

    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    @Type(type = "jsonb-node")
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
