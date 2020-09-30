package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class EventEntityCrudValidatorImplTest {

    private EventEntityCrudValidatorImpl validator;

    private CaseTypeEntity caseType;

    private EventEntity event;

    private EventACLEntity eventUserRole;

    private EventEntityValidationContext context;

    @Before
    public void setup() {

        validator = new EventEntityCrudValidatorImpl();

        eventUserRole = new EventACLEntity();

        caseType = new CaseTypeEntity();
        caseType.setReference("case type");

        event = new EventEntity();
        event.setReference("case event");
        event.addEventACL(eventUserRole);

        context = new EventEntityValidationContext(caseType);
    }

    @Test
    public void goodCrud() {
        eventUserRole.setCrudAsString("Cr Du");
        final ValidationResult result = validator.validate(event, context);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void crudTooLong() {

        eventUserRole.setCrudAsString(" CRUD   DD ");

        final ValidationResult result = validator.validate(event, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(EventEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value ' CRUD   DD ' for case type 'case type', event 'case event'"));
    }

    @Test
    public void blankCrud() {

        final ValidationResult result = validator.validate(event, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(EventEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value '' for case type 'case type', event 'case event'"));
    }

    @Test
    public void invalidCrud() {

        eventUserRole.setCrudAsString("X");

        final ValidationResult result = validator.validate(event, context);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(EventEntityInvalidCrudValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid CRUD value 'X' for case type 'case type', event 'case event'"));
    }
}
