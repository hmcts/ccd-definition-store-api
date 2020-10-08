package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventPostStateEntity;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.showcondition.BaseShowConditionTest.caseFieldEntity;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.showcondition.BaseShowConditionTest.exampleFieldTypeEntityWithComplexFields;

public class EventEntityPostStateValidatorTest {

    private static final String STATE_APPROVAL_REQUIRED = "ApprovalRequired";

    private static final String STATE_READY_FOR_DIRECTIONS = "ReadyForDirections";

    private static final String STATE_DEFAULT = "DefaultPostState";

    private static final String ENABLING_CONDITION = "FieldA!=\"\" AND FieldB=\"I'm innocent\"";

    private EventEntityPostStateValidator classUnderTest;

    @Mock
    private ShowConditionParser showConditionExtractor;

    private CaseFieldEntityUtil caseTypeEntityUtil = new CaseFieldEntityUtil();

    private CaseTypeEntity caseTypeEntity;

    private EventEntityValidationContext eventEntityValidationContext;

    @Before
    public void setUp() throws InvalidShowConditionException {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new EventEntityPostStateValidator(showConditionExtractor, caseTypeEntityUtil);
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("TestCaseType");
        eventEntityValidationContext = new EventEntityValidationContext(caseTypeEntity);
        mockShowCondition();
    }

    @Test
    public void shouldReturnEmptyErrorListWhenNoPostStatesPresent() {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(createCaseTypeEntity());
        eventEntity.setReference("createCase");
        ValidationResult validationResult = classUnderTest.validate(eventEntity, eventEntityValidationContext);

        assertNotNull(validationResult, "validation result should not be null");
        assertThat(validationResult.getValidationErrors().size(), is(0));
    }

    @Test
    public void shouldFailWhenNoDefaultPostStatePresent() {
        EventEntity eventEntity = createEventWithPostStates(STATE_APPROVAL_REQUIRED, ENABLING_CONDITION, 1);
        ValidationResult validationResult = classUnderTest.validate(eventEntity, eventEntityValidationContext);

        assertNotNull(validationResult, "validation result should not be null");
        assertThat(validationResult.getValidationErrors().size(), is(1));
        assertThat(validationResult.getValidationErrors().get(0).getDefaultMessage(),
            is("Non-conditional post state is required for case type 'TestCaseType', event 'createCase'"));
    }

    @Test
    public void shouldNotFailWhenDefaultPostStatePresent() {
        EventEntity eventEntity = createEventWithPostStates(STATE_APPROVAL_REQUIRED, ENABLING_CONDITION, 1);
        EventPostStateEntity defaultPostStateEntity = createEventPostStateEntity(STATE_READY_FOR_DIRECTIONS,
            null, eventEntity, 99);
        eventEntity.getPostStates().add(defaultPostStateEntity);
        ValidationResult validationResult = classUnderTest.validate(eventEntity, eventEntityValidationContext);

        assertNotNull(validationResult, "validation result should not be null");
        assertThat(validationResult.getValidationErrors().size(), is(0));
    }

    @Test
    public void shouldFailWhenMultiplePostStatesHasSamePriority() {
        EventEntity eventEntity = createEventWithPostStates(STATE_APPROVAL_REQUIRED, ENABLING_CONDITION, 1);
        EventPostStateEntity postStateEntity = createEventPostStateEntity(STATE_READY_FOR_DIRECTIONS,
            ENABLING_CONDITION, eventEntity, 1);
        eventEntity.getPostStates().add(postStateEntity);

        EventPostStateEntity defaultPostStateEntity = createEventPostStateEntity(STATE_DEFAULT,
            null, eventEntity, 99);
        eventEntity.getPostStates().add(defaultPostStateEntity);

        ValidationResult validationResult = classUnderTest.validate(eventEntity, eventEntityValidationContext);

        assertNotNull(validationResult, "validation result should not be null");
        assertThat(validationResult.getValidationErrors().size(), is(1));
        assertThat(validationResult.getValidationErrors().get(0).getDefaultMessage(),
            is("Duplicate post state priorities for case type 'TestCaseType', event 'createCase'"));
    }

    @Test
    public void shouldNotFailWhenMultiplePostStatesHasDifferentPriority() {
        EventEntity eventEntity = createEventWithPostStates(STATE_APPROVAL_REQUIRED, ENABLING_CONDITION, 2);
        EventPostStateEntity defaultPostStateEntity = createEventPostStateEntity(STATE_READY_FOR_DIRECTIONS,
            null, eventEntity, 1);
        eventEntity.getPostStates().add(defaultPostStateEntity);
        ValidationResult validationResult = classUnderTest.validate(eventEntity, eventEntityValidationContext);

        assertNotNull(validationResult, "validation result should not be null");
        assertThat(validationResult.getValidationErrors().size(), is(0));
    }

    @Test
    public void shouldFailWhenCaseFieldIsNotDefinedDefaultPostStatePresent() throws InvalidShowConditionException {
        mockShowCondition(Sets.newHashSet("FieldA", "FieldC"), new ArrayList<>());
        String matchingConditionFieldNotDefined = "FieldA!=\"\" AND FieldC=\"I'm innocent\"";

        EventEntity eventEntity = createEventWithPostStates(STATE_APPROVAL_REQUIRED,
            matchingConditionFieldNotDefined, 1);
        EventPostStateEntity defaultPostStateEntity = createEventPostStateEntity(STATE_READY_FOR_DIRECTIONS,
            null, eventEntity, 99);
        eventEntity.getPostStates().add(defaultPostStateEntity);
        ValidationResult validationResult = classUnderTest.validate(eventEntity, eventEntityValidationContext);

        assertNotNull(validationResult, "validation result should not be null");
        assertThat(validationResult.getValidationErrors().size(), is(1));
    }

    @Test
    public void successfullyValidatesPostStateConditionForCustomComplexField() throws InvalidShowConditionException {
        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId
            + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode.AddressUKCode.Country";
        String showCondition = matchingCaseFieldKey + "=\"UK\"";

        ShowCondition validParsedShowCondition = new ShowCondition
            .Builder()
            .showConditionExpression(showCondition)
            .field(matchingCaseFieldKey)
            .build();
        Mockito.when(showConditionExtractor
            .parseShowCondition(ArgumentMatchers.any()))
            .thenReturn(validParsedShowCondition);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("SimpleType");
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId1"));
        caseTypeEntity.addCaseField(caseFieldEntity(matchingCaseFieldId, exampleFieldTypeEntityWithComplexFields()));
        caseTypeEntity.addCaseField(caseFieldEntity("NonMatchingCaseFieldId2"));
        EventEntity event = new EventEntity();
        event.setCaseType(caseTypeEntity);

        EventPostStateEntity eventPostStateEntity = createEventPostStateEntity(STATE_APPROVAL_REQUIRED,
            showCondition, event, 1);
        EventPostStateEntity defaultPostStateEntity = createEventPostStateEntity(STATE_READY_FOR_DIRECTIONS,
            null, event, 99);
        event.getPostStates().add(defaultPostStateEntity);
        event.getPostStates().add(eventPostStateEntity);
        ValidationResult validationResult = classUnderTest.validate(event, eventEntityValidationContext);

        assertNotNull(validationResult, "validation result should not be null");
        assertThat(validationResult.getValidationErrors().size(), is(0));
        Assert.assertThat(validationResult.isValid(), CoreMatchers.is(true));
    }

    private ShowCondition mockShowCondition() throws InvalidShowConditionException {
        return mockShowCondition(new HashSet<>(), new ArrayList<>());
    }

    private ShowCondition mockShowCondition(Set<String> fields,
                                            List<String> fieldsWithSubTypes) throws InvalidShowConditionException {
        ShowCondition showCondition = mock(ShowCondition.class);
        Mockito.when(showCondition.getFields()).thenReturn(fields);
        Mockito.when(showCondition.getFieldsWithSubtypes()).thenReturn(fieldsWithSubTypes);
        Mockito.when(showCondition.getShowConditionExpression()).thenReturn("");
        Mockito.when(showConditionExtractor.parseShowCondition(ArgumentMatchers.any())).thenReturn(showCondition);
        return showCondition;
    }

    private EventEntity createEventWithPostStates(String postStateReference,
                                                  String matchingCondition,
                                                  int priority) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(createCaseTypeEntity());
        eventEntity.setReference("createCase");
        EventPostStateEntity postStateEntity = createEventPostStateEntity(postStateReference,
            matchingCondition,
            eventEntity,
            priority);
        eventEntity.getPostStates().add(postStateEntity);
        return eventEntity;
    }

    private EventPostStateEntity createEventPostStateEntity(String postStateReference,
                                                            String matchingCondition,
                                                            EventEntity eventEntity,
                                                            int priority) {
        EventPostStateEntity postStateEntity = new EventPostStateEntity();
        postStateEntity.setPostStateReference(postStateReference);
        postStateEntity.setEnablingCondition(matchingCondition);
        postStateEntity.setPriority(priority);
        postStateEntity.setEventEntity(eventEntity);
        return postStateEntity;
    }

    private CaseTypeEntity createCaseTypeEntity() {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("TestCaseRef");
        caseTypeEntity.addCaseField(createCaseFieldEntity("FieldA"));
        caseTypeEntity.addCaseField(createCaseFieldEntity("FieldB"));
        return caseTypeEntity;
    }

    private CaseFieldEntity createCaseFieldEntity(final String reference) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        return caseFieldEntity;
    }
}
