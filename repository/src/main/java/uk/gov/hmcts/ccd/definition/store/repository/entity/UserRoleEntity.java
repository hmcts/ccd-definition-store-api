package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

@Table(name = "case_role")
@Entity
@TypeDef(
    name = "pgsql_securityclassification_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name="type", value="uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification")

)

public class UserRoleEntity extends Role implements Serializable {
//
//    @Id
//    @Column(name = "id")
//    @GeneratedValue(strategy= IDENTITY)
//    private Integer id;
//
//    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
//    @CreationTimestamp
//    private LocalDateTime createdAt;
//
//    @Column(name = "live_from")
//    private LocalDate liveFrom;
//
//    @Column(name = "live_to")
//    private LocalDate liveTo;
//
//    @Column(name = "role", nullable = false, updatable = false)
//    private String role;
//
    @Column(name = "security_classification")
    @Enumerated(EnumType.STRING)
    @Type( type = "pgsql_securityclassification_enum" )
    private SecurityClassification securityClassification;

//    @Version
//    @Column(name = "jpa_optimistic_lock", nullable = false, insertable = false)
//    private Long jpaOptimisticLock;

//    @VisibleForTesting
//    public Long getJpaOptimisticLock() {
//        return jpaOptimisticLock;
//    }

//    public Integer getId() {
//        return id;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public String getRole() {
//        return role;
//    }
//
//    public void setRole(final String role) {
//        this.role = role;
//    }
//
//    public LocalDate getLiveFrom() {
//        return liveFrom;
//    }
//
//    public void setLiveFrom(final LocalDate liveFrom) {
//        this.liveFrom = liveFrom;
//    }
//
//    public LocalDate getLiveTo() {
//        return liveTo;
//    }
//
//    public void setLiveTo(final LocalDate liveTo) {
//        this.liveTo = liveTo;
//    }

    public SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(final SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }
}
