package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ChallengeQuestionDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ChallengeQuestionValidatorTest {

    private ParseContext parseContext;
    private DefinitionDataItem definitionDataItem;
    private String questionId = "questionId";

    private static final String CASE_TYPE = "Case_Type";
    private static final String FIELD_TYPE = "Text";
    private static final String QUESTION_TEXT = "What's the name of the party you wish to represent?";
    private static final String DISPLAY_CONTEXT_PARAMETER_1 = "#DATETIMEENTRY(dd-MM-yyyy)";
    private static final String DISPLAY_CONTEXT_PARAMETER_2 = "#DATETIMEENTRY(dd-MM-yyyy)";
    private static final String QUESTION_ID = "NoCChallenge";
    private static final String ANSWERD = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[CLAIMANT]";
    private ChallengeQuestionValidator challengeQuestionValidator;

    @Mock
    ChallengeQuestionDisplayContextParameterValidator challengeQuestionDisplayContextParameterValidator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        parseContext = new ParseContext();
        definitionDataItem = new DefinitionDataItem(SheetName.CHALLENGE_QUESTION_TAB.toString());
        ChallengeQuestionTabEntity entity = new ChallengeQuestionTabEntity();
        entity.setQuestionId("questionId");
        Optional<ChallengeQuestionTabEntity> mockResult = Optional.of(entity);
        when(challengeQuestionDisplayContextParameterValidator.validate(any(),any())).thenReturn(new ValidationResult());
        buildParseContext();
        challengeQuestionValidator = new ChallengeQuestionValidator(challengeQuestionDisplayContextParameterValidator);
    }

    @Test
    public void testAnswerFormatForLongExpression() {
        String answer = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[CLAIMANT]," +
            "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
        buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId");
        ChallengeQuestionTabEntity challengeQuestionTabEntity = challengeQuestionValidator.validate(parseContext, definitionDataItem);
        assertValues(answer, challengeQuestionTabEntity, DISPLAY_CONTEXT_PARAMETER_1);
    }

    @Test
    public void testAnswerFormatForSortExpression() {
        String answer = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[CLAIMANT]";
        buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_2, QUESTION_ID, answer,"questionId");
        ChallengeQuestionTabEntity challengeQuestionTabEntity = challengeQuestionValidator.validate(parseContext, definitionDataItem);
        assertValues(answer, challengeQuestionTabEntity, DISPLAY_CONTEXT_PARAMETER_2);
    }

    @Test
    public void testAnswerFormatForMinimunExpression() {
        String answer = "${OrganisationField}:[CLAIMANT]";
        buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_2, QUESTION_ID, answer,"questionId");
        ChallengeQuestionTabEntity challengeQuestionTabEntity = challengeQuestionValidator.validate(parseContext, definitionDataItem);
        assertValues(answer, challengeQuestionTabEntity, DISPLAY_CONTEXT_PARAMETER_2);
    }

    @Test
    public void testAnswerFormatForSmallestExpression() {
        String answer = "${OrganisationField.OrganisationID}:[CLAIMANT]";
        buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId");
        ChallengeQuestionTabEntity challengeQuestionTabEntity = challengeQuestionValidator.validate(parseContext, definitionDataItem);
        assertValues(answer, challengeQuestionTabEntity, DISPLAY_CONTEXT_PARAMETER_1);
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatForSmallestExpression() {
        try {
            String answer = "${OrganisationField}:[CCCCCC]";
            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: ${OrganisationField}:[CCCCCC] " +
                "is not a valid Answer value. Please check the expression format and the roles."));
            throw exception;

        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatForSmallestForRoleExpression() {
        try {
            String answer = "${XXXX}:[CLAIMANT]";
            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: ${XXXX}:[CLAIMANT] is not a valid Answer, " +
                "Please check the expression format and the roles."));
            throw exception;

        }
    }

    private void assertValues(String answer, ChallengeQuestionTabEntity challengeQuestionTabEntity, String displayContext) {

        assertThat(challengeQuestionTabEntity.getAnswerField(), is(answer));
        assertThat(challengeQuestionTabEntity.getCaseType().getReference(), is(CASE_TYPE));
        assertThat(challengeQuestionTabEntity.getAnswerFieldType().getReference(), is(FIELD_TYPE));
        assertThat(challengeQuestionTabEntity.getChallengeQuestionId(), is(QUESTION_ID));
        assertThat(challengeQuestionTabEntity.getQuestionText(), is(QUESTION_TEXT));
        assertThat(challengeQuestionTabEntity.getDisplayContextParameter(), is(displayContext));
    }

    @Test(expected = InvalidImportException.class)
    public void failForCaseTypeValidation() {
        try {
            buildDefinitionDataItem("incorrectCaseType", FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD,"questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid Case Type value: incorrectCaseType. It cannot be found in the spreadsheet."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failForFieldTypeValidation() {
        try {
            buildDefinitionDataItem(CASE_TYPE, "fiedType-CCC", "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD,"questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab InvalidField Type value: fiedType-CCC cannot be found as a valid Field type."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failForDisplayOrderValidation() {
        try {
            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "TTTT", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD,"questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: TTTT is not a valid DisplayOrder."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failForQuestionTextValidation() {
        try {
            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", null, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD,"questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: QuestionText cannot be null."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failForIDValidation() {
        try {
            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, null, ANSWERD,"questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: ID cannot be null."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatDueToInvalidRole() {
        try {
            String answer = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[NO]," +
                "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: " +
                "${OrganisationField.OrganisationID}:[NO] is not a valid Answer value. " +
                "Please check the expression format and the roles."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatDueToInvalidPathInExpressionAttribute() {
        try {
            String answer = "${OrganisationField.XXXX}|${OrganisationField.OrganisationID}:[DEFENDANT]," +
                "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: XXXX is not a valid Answer " +
                "value, The expression dot notation values should be valid caseTypes fields."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatDueToInvalidPathInExpression() {
        try {
            String answer = "${XXXXX.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]," +
                "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: ${XXXXX.OrganisationName} is " +
                "not a valid Answer value. The expression dot notation values should be valid caseTypes fields."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatDueToInvalidFormat() {
        try {
            String answer = "CXFSEKEOE";
            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId");
            challengeQuestionValidator.validate(parseContext, definitionDataItem);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: CXFSEKEOE is not a valid Answer, " +
                "Please check the expression format and the roles."));
            throw exception;
        }
    }

    private void buildDefinitionDataItem(String caseType, String filedType, String displayOder, String questionText,
                                         String displayContextParameter, String id, String answer, String questionId) {

        definitionDataItem.addAttribute(ColumnName.CASE_TYPE_ID, caseType);
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD_TYPE, filedType);
        definitionDataItem.addAttribute(ColumnName.DISPLAY_ORDER, displayOder);
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_TEXT, questionText);
        definitionDataItem.addAttribute(ColumnName.DISPLAY_CONTEXT_PARAMETER, displayContextParameter);
        definitionDataItem.addAttribute(ColumnName.ID, id);
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD, answer);
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_QUESTION_ID, questionId);

    }

    private void buildParseContext() {

        final List<CaseRoleEntity> caseRoleEntities = new ArrayList<>();

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE);
        parseContext.registerCaseType(caseTypeEntity);

        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(FIELD_TYPE);
        parseContext.addToAllTypes(fieldTypeEntity);

        final CaseRoleEntity caseRoleEntity1 = new CaseRoleEntity();
        caseRoleEntity1.setReference("[DEFENDANT]");
        caseRoleEntity1.setCaseType(caseTypeEntity);
        final CaseRoleEntity caseRoleEntity2 = new CaseRoleEntity();
        caseRoleEntity2.setReference("[CLAIMANT]");
        caseRoleEntity2.setCaseType(caseTypeEntity);

        caseRoleEntities.add(caseRoleEntity1);
        caseRoleEntities.add(caseRoleEntity2);
        parseContext.registerCaseRoles(caseRoleEntities);

        //OrganisationField
        FieldTypeEntity fieldTypeEntityOrganisationFiled = new FieldTypeEntity();
        fieldTypeEntityOrganisationFiled.setReference("Organisation");
        ComplexFieldEntity organisationName = new ComplexFieldEntity();
        organisationName.setReference("OrganisationName");
        ComplexFieldEntity organisationID = new ComplexFieldEntity();
        organisationID.setReference("OrganisationID");
        fieldTypeEntityOrganisationFiled.addComplexFields(Arrays.asList(organisationName, organisationID));
        parseContext.registerCaseFieldType(CASE_TYPE, "OrganisationField", fieldTypeEntityOrganisationFiled);

        // OrganisationPolicyField
        FieldTypeEntity fieldTypeEntityOrganisation = new FieldTypeEntity();
        fieldTypeEntityOrganisation.setReference("OrganisationPolicy");

        ComplexFieldEntity organisation = new ComplexFieldEntity();
        organisation.setReference("Organisation");
        ComplexFieldEntity orgPolicyCaseAssignedRole = new ComplexFieldEntity();
        orgPolicyCaseAssignedRole.setReference("OrgPolicyCaseAssignedRole");

        ComplexFieldEntity orgPolicyReference = new ComplexFieldEntity();
        orgPolicyReference.setReference("OrgPolicyReference");
        fieldTypeEntityOrganisation.addComplexFields(Arrays.asList(organisation, orgPolicyCaseAssignedRole, orgPolicyReference));
        parseContext.registerCaseFieldType(CASE_TYPE, "OrganisationPolicyField", fieldTypeEntityOrganisation);
    }
}
