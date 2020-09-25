package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORDER_SUMMARY;

public class EventCaseFieldShowConditionValidatorImplTest {

    @Mock
    private ShowConditionParser showConditionExtractor;

    private EventCaseFieldShowConditionValidatorImpl classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new EventCaseFieldShowConditionValidatorImpl(
            showConditionExtractor,
            new CaseFieldEntityUtil());
    }

    @Test
    public void shouldValidateShowConditionForCustomComplexField() throws InvalidShowConditionException {

        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId
            + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode.AddressUKCode.Country";
        String showCondition = matchingCaseFieldKey + "=\"UK\"";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition);

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(showCondition).field(matchingCaseFieldKey).build());

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext("EventId",
                asList(
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId1"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity(matchingCaseFieldId,
                            exampleFieldTypeEntityWithComplexFields()),
                        null),
                    eventCaseFieldEntityWithShowCondition,
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null)
                )
            );

        assertTrue(classUnderTest.validate(eventCaseFieldEntityWithShowCondition, eventCaseFieldEntityValidationContext)
            .isValid());
    }

    @Test
    public void shouldValidateShowConditionForCustomComplexFieldFixedList() throws InvalidShowConditionException {

        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".NamePrefix";
        String showCondition = matchingCaseFieldKey + "=\"Mr.\"";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition);

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(showCondition).field(matchingCaseFieldKey).build());

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext("EventId",
                asList(
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId1"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity(matchingCaseFieldId,
                            exampleFieldTypeEntityWithComplexFields()),
                        null)
                )
            );

        assertTrue(classUnderTest.validate(eventCaseFieldEntityWithShowCondition, eventCaseFieldEntityValidationContext)
            .isValid());
    }

    @Test
    public void shouldFailForInvalidShowCondition() throws InvalidShowConditionException {

        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode";
        String showCondition = matchingCaseFieldKey + "=\"Moreno\"";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition);

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(showCondition).field(matchingCaseFieldKey).build());

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext("EventId",
                asList(
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId1"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity(matchingCaseFieldId,
                            exampleFieldTypeEntityWithComplexFields()),
                        null),
                    eventCaseFieldEntityWithShowCondition,
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null)
                )
            );

        ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntityWithShowCondition,
            eventCaseFieldEntityValidationContext);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        Assert.assertTrue(validationResult.getValidationErrors().get(0)
            instanceof EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError);

    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void anotherEventCaseFieldExistsWithSameCaseFieldIdAsEventFieldEntityWithShowConditionSet_validValidationResultReturned()
        throws InvalidShowConditionException {

        String matchingCaseFieldId = "MatchingCaseFieldId";
        String showCondition = matchingCaseFieldId + "=true";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition
        );

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().field(matchingCaseFieldId).build()
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext("EventId",
                asList(
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId1"),
                        null
                    ),
                    eventCaseFieldEntityWithShowCondition,
                    eventCaseFieldEntity(
                        caseFieldEntity(matchingCaseFieldId),
                        null
                    ),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null
                    ),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId3"),
                        null
                    )
                )
            );

        assertTrue(classUnderTest.validate(eventCaseFieldEntityWithShowCondition, eventCaseFieldEntityValidationContext)
            .isValid());

        verify(showConditionExtractor).parseShowCondition(eq(showCondition));
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void noOtherEventCaseFieldExistsWithSameCaseFieldIdAsEventFieldEntityWithShowConditionSet_invalidValidationResultReturned()
        throws InvalidShowConditionException {

        String matchingCaseFieldId = "MatchingCaseFieldId";
        String showCondition = matchingCaseFieldId + "=true";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition
        );

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().field(matchingCaseFieldId).build()
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext("EventId",
                asList(
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId1"),
                        null
                    ),
                    eventCaseFieldEntityWithShowCondition,
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null
                    ),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId3"),
                        null
                    )
                )
            );

        ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntityWithShowCondition, eventCaseFieldEntityValidationContext);

        verify(showConditionExtractor).parseShowCondition(eq(showCondition));

        assertFalse(validationResult.isValid());

        assertEquals(1, validationResult.getValidationErrors().size());
        Assert.assertTrue(validationResult.getValidationErrors().get(0)
            instanceof EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError);

    }

    @Test
    public void eventCaseFieldEntityWithNullShowCondition_validValidationResultReturned() {

        EventCaseFieldEntity eventCaseFieldEntityWithNullShowCondition = eventCaseFieldEntity(
            null,
            null
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext("EventId", emptyList());

        assertTrue(classUnderTest.validate(
            eventCaseFieldEntityWithNullShowCondition, eventCaseFieldEntityValidationContext).isValid());

        verifyZeroInteractions(showConditionExtractor);
    }

    @Test
    public void eventCaseFieldEntityWithBlankShowCondition_validValidationResultReturned() {

        EventCaseFieldEntity eventCaseFieldEntityWithBlankShowCondition = eventCaseFieldEntity(
            null,
            "     "
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext("EventId", emptyList());

        assertTrue(classUnderTest.validate(
            eventCaseFieldEntityWithBlankShowCondition, eventCaseFieldEntityValidationContext).isValid());

        verifyZeroInteractions(showConditionExtractor);

    }

    @Test
    public void invalidShowConditionExceptionThrown_validValidationResultReturned()
        throws InvalidShowConditionException {

        String showCondition = "InvalidShowCondition";
        EventCaseFieldEntity eventCaseFieldEntityWithInvalidShowCondition = eventCaseFieldEntity(
            null,
            showCondition
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext(null, null);

        when(showConditionExtractor.parseShowCondition(any())).thenThrow(new InvalidShowConditionException(null));

        ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntityWithInvalidShowCondition, eventCaseFieldEntityValidationContext);

        verify(showConditionExtractor).parseShowCondition(eq(showCondition));

        assertFalse(validationResult.isValid());

        assertEquals(1, validationResult.getValidationErrors().size());
        Assert.assertTrue(
            validationResult.getValidationErrors().get(0) instanceof EventCaseFieldEntityInvalidShowConditionError);

    }

    @Test
    public void shouldReturnInvalidResultWhenAnyCaseFieldUsedInAndConditionsDoNotMatchEventCaseFields()
        throws InvalidShowConditionException {

        String matchingCaseFieldId1 = "MatchingCaseFieldId1";
        String matchingCaseFieldId2 = "MatchingCaseFieldId2";
        String showCondition = matchingCaseFieldId1 + "=true AND " + matchingCaseFieldId2 + "=true";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition
        );

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().field(matchingCaseFieldId1).field(matchingCaseFieldId2).build()
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext("EventId",
                asList(
                    eventCaseFieldEntity(
                        caseFieldEntity("MatchingCaseFieldId1"),
                        null
                    ),
                    eventCaseFieldEntityWithShowCondition,
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null
                    )
                )
            );

        ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntityWithShowCondition, eventCaseFieldEntityValidationContext);

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getValidationErrors(), hasSize(1));
        assertThat(validationResult.getValidationErrors().get(0),
            instanceOf(EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError.class));

        verify(showConditionExtractor).parseShowCondition(eq(showCondition));
    }

    @Test
    public void shouldValidateShowConditionWithMetadataField() throws InvalidShowConditionException {
        String field = MetadataField.STATE.getReference();
        String showCondition = field + "=\"TODO\"";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition
        );

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().field(field).build()
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext("EventId",
                asList(
                    eventCaseFieldEntityWithShowCondition,
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId"),
                        null
                    )
                )
            );

        ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntityWithShowCondition, eventCaseFieldEntityValidationContext);

        assertThat(validationResult.isValid(), is(true));
    }

    private static FieldTypeEntity exampleFieldTypeEntityWithComplexFields() {
        return fieldTypeEntity("FullName",
            asList(
                complexFieldEntity(
                    "NamePrefix",
                    fixedListFieldTypeEntity(
                        "FixedList-PreFix",
                        asList(
                            fieldTypeListItemEntity("Mr.", "Mr."),
                            fieldTypeListItemEntity("Mrs.", "Mrs.")))),
                complexFieldEntity("FirstName", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("MiddleName", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("LastNameWithSomeCplxFields", fieldTypeEntity("FullName1",
                    asList(
                        complexFieldEntity("LastName", fieldTypeEntity("Text", emptyList())),
                        complexFieldEntity("SomeComplexFieldsCode",
                            fieldTypeEntity("SomeComplexFields",
                                asList(
                                    complexFieldEntity("AddressUKCode", addressUKFieldTypeEntity()),
                                    complexFieldEntity("AddressGlobalCode", addressGlobalFieldTypeEntity()),
                                    complexFieldEntity("AddressGlobalUKCode", addressGlobalUKFieldTypeEntity()),
                                    complexFieldEntity("OrderSummaryCode", orderSummaryFieldTypeEntity()),
                                    complexFieldEntity("SecondSurname", fieldTypeEntity(
                                        "Text", emptyList()))
                                )))
                    ))))
        );
    }

    private static FieldTypeEntity orderSummaryFieldTypeEntity() {
        return fieldTypeEntity(PREDEFINED_COMPLEX_ORDER_SUMMARY,
            asList(
                complexFieldEntity("PaymentReference", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("PaymentTotal", fieldTypeEntity("MoneyGBP", emptyList())),
                complexFieldEntity("Fees", fieldTypeEntity("FeesList", emptyList()))
            ));
    }

    private static FieldTypeEntity addressUKFieldTypeEntity() {
        return address(PREDEFINED_COMPLEX_ADDRESS_UK);
    }

    private static FieldTypeEntity addressGlobalFieldTypeEntity() {
        return address(PREDEFINED_COMPLEX_ADDRESS_GLOBAL);
    }

    private static FieldTypeEntity addressGlobalUKFieldTypeEntity() {
        return address(PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK);
    }

    private static FieldTypeEntity address(String reference) {
        return fieldTypeEntity(reference,
            asList(
                complexFieldEntity("AddressLine1", fieldTypeEntity("TextMax150", emptyList())),
                complexFieldEntity("AddressLine2", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("AddressLine3", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("PostTown", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("County", fieldTypeEntity("TextMax50", emptyList())),
                complexFieldEntity("PostCode", fieldTypeEntity("TextMax14", emptyList())),
                complexFieldEntity("Country", fieldTypeEntity("TextMax50", emptyList()))
            ));
    }

    private static EventCaseFieldEntity eventCaseFieldEntity(CaseFieldEntity caseFieldEntity, String showCondition) {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setEvent(new EventEntity());
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        eventCaseFieldEntity.setShowCondition(showCondition);
        return eventCaseFieldEntity;
    }

    private static CaseFieldEntity caseFieldEntity(String reference, FieldTypeEntity fieldTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(fieldTypeEntity);
        return caseFieldEntity;
    }

    private static CaseFieldEntity caseFieldEntity(String caseFieldReference) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(caseFieldReference);
        caseFieldEntity.setFieldType(fieldTypeEntity("TEXT", emptyList()));
        return caseFieldEntity;
    }

    private static FieldTypeEntity fieldTypeEntity(String reference,
                                                   List<ComplexFieldEntity> complexFieldEntityList) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addComplexFields(complexFieldEntityList);
        return fieldTypeEntity;
    }

    private static ComplexFieldEntity complexFieldEntity(String reerence, FieldTypeEntity fieldTypeEntity) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reerence);
        complexFieldEntity.setFieldType(fieldTypeEntity);
        return complexFieldEntity;
    }

    private static FieldTypeEntity fixedListFieldTypeEntity(String reference,
                                                            List<FieldTypeListItemEntity> listItemEntities) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addListItems(listItemEntities);
        return fieldTypeEntity;
    }

    private static FieldTypeListItemEntity fieldTypeListItemEntity(String label, String value) {
        FieldTypeListItemEntity fieldTypeListItemEntity = new FieldTypeListItemEntity();
        fieldTypeListItemEntity.setLabel(label);
        fieldTypeListItemEntity.setValue(value);
        return fieldTypeListItemEntity;
    }
}
