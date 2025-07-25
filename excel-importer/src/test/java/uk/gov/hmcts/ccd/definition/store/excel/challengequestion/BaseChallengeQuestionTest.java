package uk.gov.hmcts.ccd.definition.store.excel.challengequestion;

import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseChallengeQuestionTest {

    protected static final String CASE_TYPE = "Case_Type";
    protected static final String FIELD_TYPE = "Text";
    protected static final String QUESTION_TEXT = "What's the name of the party you wish to represent?";
    protected static final String DISPLAY_CONTEXT_PARAMETER_1 = "#DATETIMEENTRY(dd-MM-yyyy)";
    protected static final String DISPLAY_CONTEXT_PARAMETER_2 = "#DATETIMEENTRY(dd-MM-yyyy)";
    protected static final String QUESTION_ID = "NoCChallenge";
    protected static final String ANSWERD
        = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[CLAIMANT]";

    protected ParseContext buildParseContext() {
        ParseContext parseContext = new ParseContext();
        final List<CaseRoleEntity> caseRoleEntities = new ArrayList<>();

        // register a test case type
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE);
        parseContext.registerCaseType(caseTypeEntity);

        // register basic text field type
        FieldTypeEntity fieldTypeEntityText = new FieldTypeEntity();
        fieldTypeEntityText.setReference(FIELD_TYPE);
        parseContext.addToAllTypes(fieldTypeEntityText);

        final CaseRoleEntity caseRoleEntity1 = new CaseRoleEntity();
        caseRoleEntity1.setReference("[DEFENDANT]");
        caseRoleEntity1.setCaseType(caseTypeEntity);
        final CaseRoleEntity caseRoleEntity2 = new CaseRoleEntity();
        caseRoleEntity2.setReference("[CLAIMANT]");
        caseRoleEntity2.setCaseType(caseTypeEntity);

        caseRoleEntities.add(caseRoleEntity1);
        caseRoleEntities.add(caseRoleEntity2);
        parseContext.registerCaseRoles(caseRoleEntities);

        // OrganisationField
        FieldTypeEntity fieldTypeEntityOrganisationFiled = new FieldTypeEntity();
        fieldTypeEntityOrganisationFiled.setReference("Organisation");
        parseContext.addToAllTypes(fieldTypeEntityOrganisationFiled);

        ComplexFieldEntity organisationName = new ComplexFieldEntity();
        organisationName.setReference("OrganisationName");
        organisationName.setFieldType(fieldTypeEntityText);

        ComplexFieldEntity organisationID = new ComplexFieldEntity();
        organisationID.setReference("OrganisationID");
        organisationID.setFieldType(fieldTypeEntityText);

        fieldTypeEntityOrganisationFiled.addComplexFields(Arrays.asList(organisationName, organisationID));
        parseContext.registerCaseFieldType(CASE_TYPE, "OrganisationField", fieldTypeEntityOrganisationFiled);

        // OrganisationPolicyField
        FieldTypeEntity fieldTypeEntityOrganisation = new FieldTypeEntity();
        fieldTypeEntityOrganisation.setReference("OrganisationPolicy");
        parseContext.addToAllTypes(fieldTypeEntityOrganisation);

        ComplexFieldEntity organisation = new ComplexFieldEntity();
        organisation.setReference("Organisation");
        organisation.setFieldType(fieldTypeEntityOrganisationFiled);

        ComplexFieldEntity orgPolicyCaseAssignedRole = new ComplexFieldEntity();
        orgPolicyCaseAssignedRole.setReference("OrgPolicyCaseAssignedRole");
        orgPolicyCaseAssignedRole.setFieldType(fieldTypeEntityText);

        ComplexFieldEntity orgPolicyReference = new ComplexFieldEntity();
        orgPolicyReference.setReference("OrgPolicyReference");
        orgPolicyReference.setFieldType(fieldTypeEntityText);

        fieldTypeEntityOrganisation.addComplexFields(Arrays.asList(
            organisation, orgPolicyCaseAssignedRole, orgPolicyReference));
        parseContext.registerCaseFieldType(CASE_TYPE, "OrganisationPolicyField", fieldTypeEntityOrganisation);

        return parseContext;
    }

    protected DefinitionDataItem buildDefinitionDataItem(String caseType, String filedType,
                                                       String displayOder, String questionText,
                                                       String displayContextParameter, String id,
                                                       String answer, String questionId) {
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.CHALLENGE_QUESTION_TAB.toString());
        definitionDataItem.addAttribute(ColumnName.CASE_TYPE_ID, caseType);
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD_TYPE, filedType);
        definitionDataItem.addAttribute(ColumnName.DISPLAY_ORDER, displayOder);
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_TEXT, questionText);
        definitionDataItem.addAttribute(ColumnName.DISPLAY_CONTEXT_PARAMETER, displayContextParameter);
        definitionDataItem.addAttribute(ColumnName.ID, id);
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD, answer);
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_QUESTION_ID, questionId);
        return definitionDataItem;
    }
}
