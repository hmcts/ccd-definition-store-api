package uk.gov.hmcts.ccd.definition.store.utils;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class CaseFieldEntityBuilder {

    private String reference;
    private String fieldTypeReference;

    public CaseFieldEntityBuilder() {}

    public CaseFieldEntityBuilder withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public CaseFieldEntity build() {
        CaseFieldEntity field = new CaseFieldEntity();
        field.setReference(this.reference);
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(fieldTypeReference);
        field.setFieldType(fieldType);
        return field;
    }

    public CaseFieldEntityBuilder withBaseType(String reference) {
        this.fieldTypeReference = reference;
        return this;
    }
}
