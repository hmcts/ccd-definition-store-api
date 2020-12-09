package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class EventEntityEnablingConditionValidatorTest {

    private EventEntityEnablingConditionValidator classUnderTest;

    @Mock
    private ShowConditionParser showConditionExtractor;

    private CaseFieldEntityUtil caseTypeEntityUtil = new CaseFieldEntityUtil();

    private CaseTypeEntity caseTypeEntity;

    private EventEntityValidationContext eventEntityValidationContext;

    @Before
    public void setUp() throws InvalidShowConditionException {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new EventEntityEnablingConditionValidator(showConditionExtractor, caseTypeEntityUtil);
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("TestCaseType");
        eventEntityValidationContext = new EventEntityValidationContext(caseTypeEntity);
        mockShowCondition();
    }

    @Test
    public void shouldReturnEmptyErrorListWhenNoEventEnablingConditionPresent() {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(createCaseTypeEntity());
        eventEntity.setReference("createCase");
        ValidationResult validationResult = classUnderTest.validate(eventEntity, eventEntityValidationContext);

        assertNotNull(validationResult, "validation result should not be null");
        assertThat(validationResult.getValidationErrors().size(), is(0));
    }

    @Test
    public void shouldFailWhenCaseFieldIsNotDefined() throws InvalidShowConditionException {
        mockShowCondition(Sets.newHashSet("FieldA", "FieldC"), new ArrayList<>());
        String matchingConditionFieldNotDefined = "FieldA!=\"\" AND FieldC=\"I'm innocent\"";

        EventEntity eventEntity = createEvent(matchingConditionFieldNotDefined);
        ValidationResult validationResult = classUnderTest.validate(eventEntity, eventEntityValidationContext);

        assertNotNull(validationResult, "validation result should not be null");
        assertThat(validationResult.getValidationErrors().size(), is(1));
        assertThat(validationResult.getValidationErrors().get(0).toString(),
            is("validationError: Unknown field 'FieldC' for event "
                + "'createCase' in post state condition: "
                + "'FieldA!=\"\" AND FieldC=\"I'm innocent\"'"));
    }

    private EventEntity createEvent(String matchingCondition) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(createCaseTypeEntity());
        eventEntity.setReference("createCase");
        eventEntity.setEventEnablingCondition(matchingCondition);
        return eventEntity;
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

    private EventEntity createEventWithEnablingCondition(String enablingCondition) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(createCaseTypeEntity());
        eventEntity.setReference("createCase");

        eventEntity.setEventEnablingCondition(enablingCondition);
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
