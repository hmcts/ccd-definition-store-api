package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

@Table(name = "role")
@Entity
@TypeDef(
    name = "pgsql_securityclassification_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name = "type", value = "uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification")

)

public class UserRoleEntity extends Role implements Serializable {

    @Column(name = "security_classification", nullable = false)
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_securityclassification_enum")
    private SecurityClassification securityClassification;

    public SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(final SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }
}
