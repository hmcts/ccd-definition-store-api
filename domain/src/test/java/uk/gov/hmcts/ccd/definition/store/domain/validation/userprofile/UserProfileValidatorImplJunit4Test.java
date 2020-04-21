package uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Need to investigate.
 * @deprecated I am here to get sonar to pass.  Needs investigation on why test coverage is picking up JUnit5 cases selectively.
 */
@Deprecated
public class UserProfileValidatorImplJunit4Test {

    public static final String JURISDICTION_REFERENCE = "Heman TH";
    public static final String CASE_TYPE_REFERENCE = "HOLIDAY";
    public static final String STATE_REFERENCE = "GoingToHeaven";
    private UserProfileValidatorImpl validator;
    private JurisdictionEntity jurisdiction;
    private CaseTypeEntity caseTypeEntity;

    @Before
    public void init() {
        validator = new UserProfileValidatorImpl();
        jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(JURISDICTION_REFERENCE);
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_REFERENCE);
        final StateEntity state = new StateEntity();
        state.setReference(STATE_REFERENCE);
        caseTypeEntity.addState(state);
    }

    @Test
    public void emptyDataToValidate() {
        final ValidationResult result = validator.validate(Collections.emptyList(), null, Collections.emptyList());
        assertTrue(result.isValid());
    }

    @Test
    public void validWorkbasketDefaults() {
        WorkBasketUserDefault userDefault = buildWorkBasketUserDefault("Nayab.The.Royal.Highness.ssssh@hmcts.net",
                                                                       "QA",
                                                                       CASE_TYPE_REFERENCE,
                                                                       STATE_REFERENCE);
        final ValidationResult result = validator.validate(Arrays.asList(userDefault),
                                                           jurisdiction,
                                                           Arrays.asList(caseTypeEntity));
        assertFalse(result.isValid());
        assertThat(result.getValidationErrors(), hasSize(equalTo(1)));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                   is("Invalid jurisdiction in workbasket user default; user: " + //
                          "'Nayab.The.Royal.Highness.ssssh@hmcts.net', " + //
                          "jurisdiction: 'QA', case type: 'HOLIDAY', state: 'GoingToHeaven'"));

    }

    @Test
    public void invalidWorkbasketDefaultsUndefinedJurisdiction() {
        WorkBasketUserDefault userDefault = buildWorkBasketUserDefault("Nayab.The.Royal.Highness.ssssh@hmcts.net",
                                                                       JURISDICTION_REFERENCE,
                                                                       CASE_TYPE_REFERENCE,
                                                                       STATE_REFERENCE);
        final ValidationResult result = validator.validate(Arrays.asList(userDefault),
                                                           jurisdiction,
                                                           Arrays.asList(caseTypeEntity));
        assertTrue(result.isValid());

    }

    private WorkBasketUserDefault buildWorkBasketUserDefault(final String idamId,
                                                             final String jurisdiction,
                                                             final String caseType,
                                                             final String state) {
        WorkBasketUserDefault userDefault = new WorkBasketUserDefault();
        userDefault.setUserIdamId(idamId);
        userDefault.setWorkBasketDefaultJurisdiction(jurisdiction);
        userDefault.setWorkBasketDefaultCaseType(caseType);
        userDefault.setWorkBasketDefaultState(state);
        return userDefault;
    }

    @Test
    public void invalidEmailAddress() {
        final WorkBasketUserDefault userDefault = buildWorkBasketUserDefault("BAYAN-The-Royal-Highnes",
                                                                             JURISDICTION_REFERENCE,
                                                                             CASE_TYPE_REFERENCE,
                                                                             STATE_REFERENCE);
        final ValidationResult result = validator.validate(Arrays.asList(userDefault),
                                                           jurisdiction,
                                                           Arrays.asList(caseTypeEntity));
        assertFalse(result.isValid());
        assertThat(result.getValidationErrors(), hasSize(equalTo(1)));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                   is("Invalid email in workbasket user default; user: 'BAYAN-The-Royal-Highnes', " + "jurisdiction: "
                       + "'" + JURISDICTION_REFERENCE + "', case type: '" + CASE_TYPE_REFERENCE + "', state: '"
                       + STATE_REFERENCE + "'"));
    }

}
