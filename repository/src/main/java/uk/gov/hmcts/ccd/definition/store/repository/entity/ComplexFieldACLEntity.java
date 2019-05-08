package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "complex_field_acl")
@Entity
public class ComplexFieldACLEntity extends Authorisation implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complex_field_id", nullable = false)
    private ComplexFieldEntity complexField;

    public ComplexFieldEntity getComplexField() {
        return complexField;
    }

    public void setComplexField(final ComplexFieldEntity complexField) {
        this.complexField = complexField;
    }

}
