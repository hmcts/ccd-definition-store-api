package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;

@Component
public class FieldTypeValidationContextFactory {


    private final FieldTypeRepository typeRepository;

    @Autowired
    public FieldTypeValidationContextFactory(FieldTypeRepository typeRepository) {

        this.typeRepository = typeRepository;
    }

    public FieldTypeValidationContext create() {
        return new FieldTypeValidationContext(typeRepository.findCurrentBaseTypes());
    }
}
