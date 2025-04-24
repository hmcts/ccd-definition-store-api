package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;


import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.BeforeEach;

public class CaseTypeEntityACLValidatorImplTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private CaseTypeEntityACLValidatorImpl validator;

    private CaseTypeEntity caseType;

    private CaseTypeACLEntity caseTypeAccessProfile;

    @Mock
    private AccessProfileEntity accessProfile;

    @BeforeEach
    public void setup() {

        validator = new CaseTypeEntityACLValidatorImpl();

        caseTypeAccessProfile = new CaseTypeACLEntity();

        caseType = new CaseTypeEntity();
        caseType.setReference("case type");
        caseType.addCaseTypeACL(caseTypeAccessProfile);
    }

    @Test
    public void shouldHaveValidationError_whenAccessProfileNotFound() {

        final ValidationResult result = validator.validate(caseType);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0),
                instanceOf(CaseTypeEntityInvalidAccessProfileValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid AccessProfile is not defined for case type 'case type'"));
    }

    @Test
    public void shouldHaveNoValidationError_whenAccessProfileFound() {

        caseTypeAccessProfile.setAccessProfile(accessProfile);

        final ValidationResult result = validator.validate(caseType);

        assertThat(result.getValidationErrors(), empty());
        assertThat(caseType.getCaseTypeACLEntities().get(0).getAccessProfile(), is(accessProfile));
    }
}
