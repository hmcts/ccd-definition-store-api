package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Table(name = "role")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("USERROLE")
public class AccessProfileEntity implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = SEQUENCE, generator = "role_id_seq")
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "security_classification")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SecurityClassification securityClassification;

    public Integer getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(final SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }
}
