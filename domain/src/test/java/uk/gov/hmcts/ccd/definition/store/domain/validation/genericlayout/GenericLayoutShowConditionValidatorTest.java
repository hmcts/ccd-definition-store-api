package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.InputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GenericLayoutShowConditionValidatorTest {

    private static final String SOME_SHOW_CONDITION = "someShowCondition";
    private static final String CASE_FIELD = "Case Field I";
    private static final String CASE_FIELD2 = "Case Field II";

    @Mock
    private ShowConditionParser showConditionExtractor;

    private GenericLayoutShowConditionValidatorImpl underTest;

    private InputCaseFieldEntity entity;
    private List<GenericLayoutEntity> allGenericLayouts;
    private CaseFieldEntity caseField;
    private CaseTypeEntity caseType;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new GenericLayoutShowConditionValidatorImpl(
            showConditionExtractor,
            new CaseFieldEntityUtil());

        caseType = new CaseTypeEntity();
        caseType.setReference("Case Type I");

        entity = new SearchInputCaseFieldEntity();
        entity.setCaseType(caseType);

        caseField = new CaseFieldEntity();
        caseField.setReference(CASE_FIELD);
        entity.setCaseField(caseField);

        GenericLayoutEntity entity2 = new SearchInputCaseFieldEntity();
        CaseFieldEntity caseField2 = new CaseFieldEntity();
        caseField2.setReference(CASE_FIELD2);
        entity2.setCaseField(caseField2);
        entity2.setCaseType(caseType);

        allGenericLayouts = Lists.newArrayList(entity, entity2);
    }

    @Test
    public void shouldNotExecuteWhenShowConditionIsEmpty() throws Exception {

        entity.setShowCondition(null);

        underTest.validate(entity, allGenericLayouts);

        verify(showConditionExtractor, never()).parseShowCondition(anyString());
    }

    @Test
    public void invalidShowConditionExceptionThrown_validValidationResultReturned() throws Exception {

        entity.setShowCondition(SOME_SHOW_CONDITION);

        when(showConditionExtractor.parseShowCondition(any())).thenThrow(new InvalidShowConditionException(null));

        ValidationResult result = underTest.validate(entity, allGenericLayouts);

        assertThat(result.isValid(), is(false));

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("Invalid show condition 'someShowCondition' for case type 'Case Type I' "
                + "and case field 'Case Field I'"));

    }

    @Test
    public void shouldReturnNoValidationErrorsOnSuccess() throws Exception {

        entity.setShowCondition(SOME_SHOW_CONDITION);

        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression("parsedSC").field(CASE_FIELD2).build();
        when(showConditionExtractor.parseShowCondition(SOME_SHOW_CONDITION))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = underTest.validate(entity, allGenericLayouts);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void shouldThrowErrorWhenFieldIdDoesNotExistsInTheCaseType() throws Exception {

        entity.setShowCondition(SOME_SHOW_CONDITION);

        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression("parsedSC").field("UnKnownCaseField").build();
        when(showConditionExtractor.parseShowCondition(SOME_SHOW_CONDITION))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = underTest.validate(entity, allGenericLayouts);

        assertThat(result.isValid(), is(false));

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("Unknown field 'UnKnownCaseField' for case type 'Case Type I' "
                + "in show condition: 'someShowCondition'"));
    }

    @Test
    public void shouldNotThrowErrorForMetaDataField() throws Exception {

        entity.setShowCondition(SOME_SHOW_CONDITION);

        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression("parsedSC").field("[CREATED_DATE]").build();
        when(showConditionExtractor.parseShowCondition(SOME_SHOW_CONDITION))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = underTest.validate(entity, allGenericLayouts);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void shouldValidateShowConditionForCustomComplexField() throws Exception {

        String matchingCaseFieldId = "complexName";

        GenericLayoutEntity entity2 = new SearchInputCaseFieldEntity();
        CaseFieldEntity caseField2 = new CaseFieldEntity();
        caseField2.setReference(matchingCaseFieldId);
        caseField2.setFieldType(exampleFieldTypeEntityWithComplexFields());
        entity2.setCaseField(caseField2);
        entity2.setCaseType(caseType);

        allGenericLayouts = Lists.newArrayList(entity, entity2);

        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.LastName";
        String showCondition = matchingCaseFieldKey + "=\"Mathangi\"";

        entity.setShowCondition(showCondition);

        ShowCondition validParsedShowCondition = new ShowCondition.Builder()
            .showConditionExpression("parsedSC").field(matchingCaseFieldKey).build();
        when(showConditionExtractor.parseShowCondition(any()))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = underTest.validate(entity, allGenericLayouts);

        assertThat(result.isValid(), is(true));

    }

    @Test
    public void shouldThrowErrorWhenShowConditionNotMatchingForCustomComplexField() throws Exception {

        String matchingCaseFieldId = "complexName";

        GenericLayoutEntity entity2 = new SearchInputCaseFieldEntity();
        CaseFieldEntity caseField2 = new CaseFieldEntity();
        caseField2.setReference(matchingCaseFieldId);
        caseField2.setFieldType(exampleFieldTypeEntityWithComplexFields());
        entity2.setCaseField(caseField2);
        entity2.setCaseType(caseType);

        allGenericLayouts = Lists.newArrayList(entity, entity2);

        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.ABC";
        String showCondition = matchingCaseFieldKey + "=\"Mathangi\"";

        entity.setShowCondition(showCondition);

        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression("parsedSC")
            .field(matchingCaseFieldKey).build();
        when(showConditionExtractor.parseShowCondition(any()))
            .thenReturn(validParsedShowCondition);

        ValidationResult result = underTest.validate(entity, allGenericLayouts);

        assertThat(result.isValid(), is(false));

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("Unknown field 'complexName.LastNameWithSomeCplxFields.ABC' for case type 'Case Type I' "
                + "in show condition: 'complexName.LastNameWithSomeCplxFields.ABC=\"Mathangi\"'"));

    }

    private static FieldTypeEntity exampleFieldTypeEntityWithComplexFields() {
        return fieldTypeEntity("complexName",
            asList(
                complexFieldEntity("FirstName", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("MiddleName", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("LastNameWithSomeCplxFields", fieldTypeEntity("FullName1",
                    asList(
                        complexFieldEntity("LastName", fieldTypeEntity("Text", emptyList()))
                    )))
            ));
    }

    private static ComplexFieldEntity complexFieldEntity(String reerence, FieldTypeEntity fieldTypeEntity) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reerence);
        complexFieldEntity.setFieldType(fieldTypeEntity);
        return complexFieldEntity;
    }

    private static FieldTypeEntity fieldTypeEntity(String reference,
                                                   List<ComplexFieldEntity> complexFieldEntityList) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addComplexFields(complexFieldEntityList);
        return fieldTypeEntity;
    }

}
