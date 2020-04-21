package uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserProfileValidatorImplTest {

    private static final String JURISDICTION_REFERENCE = "Heman TH";
    private static final String CASE_TYPE_REFERENCE = "HOLIDAY";
    private static final String STATE_REFERENCE = "GoingToHeaven";
    private static final String VALID_EMAIL_ADDRESS = "Ngitb.pro.re.nata@hmcts.net";
    private UserProfileValidatorImpl validator;
    private JurisdictionEntity jurisdiction;
    private CaseTypeEntity caseTypeEntity;

    @BeforeEach
    void init() {
        validator = new UserProfileValidatorImpl();
        jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(JURISDICTION_REFERENCE);
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_REFERENCE);
        final StateEntity state = new StateEntity();
        state.setReference(STATE_REFERENCE);
        caseTypeEntity.addState(state);
    }

    @DisplayName("Empty list of WorkBasketUserDefaults")
    @Test
    void emptyDataToValidate() {
        final ValidationResult result = validator.validate(Collections.emptyList(), null, Collections.emptyList());
        assertTrue(result.isValid());
    }

    @DisplayName("Valid Workbasket defaults")
    @Test
    void invalidWorkbasketDefaultsUndefinedJurisdiction() {
        final WorkBasketUserDefault userDefault = buildWorkBasketUserDefault(VALID_EMAIL_ADDRESS,
            JURISDICTION_REFERENCE,
            CASE_TYPE_REFERENCE,
            STATE_REFERENCE);
        final ValidationResult result = validator.validate(Arrays.asList(userDefault),
            jurisdiction,
            Arrays.asList(caseTypeEntity));
        assertTrue(result.isValid());

    }

    @DisplayName("Invalid Workbasket defaults jurisdiction")
    @Test
    void validWorkbasketDefaults() {
        final WorkBasketUserDefault userDefault = buildWorkBasketUserDefault(VALID_EMAIL_ADDRESS,
            "QA",
            CASE_TYPE_REFERENCE,
            STATE_REFERENCE);
        final ValidationResult result = validator.validate(Arrays.asList(userDefault),
            jurisdiction,
            Arrays.asList(caseTypeEntity));
        assertAll(() -> assertFalse(result.isValid()),
            () -> assertThat(result.getValidationErrors(), hasSize(equalTo(1))),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Invalid jurisdiction in workbasket user default; user: " + "'"
                    + VALID_EMAIL_ADDRESS + "', " + "jurisdiction: 'QA', case type: '"
                    + CASE_TYPE_REFERENCE + "', state: '" + STATE_REFERENCE + "'")));
    }

    @DisplayName("Invalid Workbasket defaults case type")
    @Test
    void invalidWorkbasketDefaultsUndefinedCaseType() {
        final WorkBasketUserDefault userDefault = buildWorkBasketUserDefault(VALID_EMAIL_ADDRESS,
            JURISDICTION_REFERENCE,
            "croissants-poi-dejeuner",
            STATE_REFERENCE);
        final ValidationResult result = validator.validate(Arrays.asList(userDefault),
            jurisdiction,
            Arrays.asList(caseTypeEntity));
        assertAll(() -> assertFalse(result.isValid()),
            () -> assertThat(result.getValidationErrors(), hasSize(equalTo(1))),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Invalid case type in workbasket user default; user: '" + VALID_EMAIL_ADDRESS
                    + "', " + "jurisdiction: '" + JURISDICTION_REFERENCE + "', case type: "
                    + "'croissants-poi-dejeuner', state: '" + STATE_REFERENCE + "'")));
    }

    @DisplayName("Invalid Workbasket defaults state")
    @Test
    void invalidWorkbasketDefaultsUndefinedState() {
        final WorkBasketUserDefault userDefault = buildWorkBasketUserDefault(VALID_EMAIL_ADDRESS,
            JURISDICTION_REFERENCE,
            CASE_TYPE_REFERENCE,
            "PF");
        final ValidationResult result = validator.validate(Arrays.asList(userDefault),
            jurisdiction,
            Arrays.asList(caseTypeEntity));
        assertAll(() -> assertFalse(result.isValid()),
            () -> assertThat(result.getValidationErrors(), hasSize(equalTo(1))),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Invalid state in workbasket user default; user: '" + VALID_EMAIL_ADDRESS + "',"
                    + "" + "" + "" + "" + "" + "" + " " + "jurisdiction: '"
                    + JURISDICTION_REFERENCE + "', " + "case " + "type:" + " '"
                    + CASE_TYPE_REFERENCE + "', state: 'PF'")));

    }

    @DisplayName("Invalid email address")
    @Test
    void invalidEmailAddress() {
        final WorkBasketUserDefault userDefault = buildWorkBasketUserDefault("BAYAN-The-Royal-Highnes",
            JURISDICTION_REFERENCE,
            CASE_TYPE_REFERENCE,
            STATE_REFERENCE);
        final ValidationResult result = validator.validate(Arrays.asList(userDefault),
            jurisdiction,
            Arrays.asList(caseTypeEntity));
        assertAll(() -> assertFalse(result.isValid()),
            () -> assertThat(result.getValidationErrors(), hasSize(equalTo(1))),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Invalid email in workbasket user default; user: 'BAYAN-The-Royal-Highnes', "
                    + "jurisdiction: '" + JURISDICTION_REFERENCE + "', case type: '"
                    + CASE_TYPE_REFERENCE + "', state: '" + STATE_REFERENCE + "'")));
    }

    @DisplayName("Validate all items in List<WorkBasketUserDefault>")
    @Test
    void validateAllListItems() {
        final WorkBasketUserDefault userDefault1 = buildWorkBasketUserDefault("BAYAN-The-Royal-Highnes",
            "BAYAN",
            CASE_TYPE_REFERENCE,
            STATE_REFERENCE);
        final WorkBasketUserDefault userDefault2 = buildWorkBasketUserDefault(VALID_EMAIL_ADDRESS,
            JURISDICTION_REFERENCE,
            CASE_TYPE_REFERENCE,
            "PF");
        final ValidationResult result = validator.validate(Arrays.asList(userDefault1, userDefault2),
            jurisdiction,
            Arrays.asList(caseTypeEntity));
        assertAll(() -> assertFalse(result.isValid()),
            () -> assertThat(result.getValidationErrors(), hasSize(equalTo(3))),
            () -> {
                final List<String> errorMessages = result.getValidationErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.toList());
                assertThat(errorMessages,
                    hasItems("Invalid email in workbasket user default; user: "
                            + "'BAYAN-The-Royal-Highnes', jurisdiction: 'BAYAN', case type: '"
                            + CASE_TYPE_REFERENCE + "', state: '" + STATE_REFERENCE + "'",
                        "Invalid jurisdiction in workbasket user default; user: "
                            + "'BAYAN-The-Royal-Highnes', jurisdiction: 'BAYAN', case type: '"
                            + CASE_TYPE_REFERENCE + "', state: '" + STATE_REFERENCE + "'",
                        "Invalid state in workbasket user default; user: '" + VALID_EMAIL_ADDRESS
                            + "', " + "jurisdiction: '" + JURISDICTION_REFERENCE + "', case type: '"
                            + CASE_TYPE_REFERENCE + "', state: 'PF'"));
            });
    }

    @DisplayName("Checks content in ValidationError createMessage")
    @Test
    void createMessage() {
        final WorkBasketUserDefault userDefault = buildWorkBasketUserDefault(VALID_EMAIL_ADDRESS,
            JURISDICTION_REFERENCE,
            CASE_TYPE_REFERENCE,
            STATE_REFERENCE);
        new UserProfileValidatorImpl.ValidationError("test case", userDefault);
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
}
