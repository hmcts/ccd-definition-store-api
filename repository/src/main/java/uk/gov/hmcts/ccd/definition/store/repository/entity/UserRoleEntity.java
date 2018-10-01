package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

@Table(name = "role")
@Entity
@TypeDef(
    name = "pgsql_securityclassification_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @org.hibernate.annotations.Parameter(name = "type", value = "uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification")
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="dtype", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("USERROLE")
public class UserRoleEntity implements Serializable {
    //public class UserRoleEntity extends RoleEntity implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
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
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_securityclassification_enum")
    private SecurityClassification securityClassification;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
