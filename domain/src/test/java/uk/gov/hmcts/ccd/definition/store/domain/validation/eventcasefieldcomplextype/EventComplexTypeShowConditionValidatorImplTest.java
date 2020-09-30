package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

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
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORDER_SUMMARY;

public class EventComplexTypeShowConditionValidatorImplTest {

    @Mock
    private ShowConditionParser showConditionExtractor;

    private EventComplexTypeShowConditionValidatorImpl classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new EventComplexTypeShowConditionValidatorImpl(
            showConditionExtractor,
            new CaseFieldEntityUtil());
    }

    @Test
    public void shouldValidateShowConditionForCustomComplexFieldWhereShowConditionReferencesEventComplexTypeItself()
        throws InvalidShowConditionException {

        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode"
            + ".AddressUKCode.Country";
        String showCondition = matchingCaseFieldKey + "=\"UK\"";

        EventComplexTypeEntity eventComplexTypeEntityWithShowCondition = eventComplexTypeEntity("reference1",
            showCondition);

        EventCaseFieldEntity eventCaseFieldWithEventComplexTypeEntity = eventCaseFieldEntity(
            null,
            asList(eventComplexTypeEntityWithShowCondition));

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(showCondition).field(matchingCaseFieldKey).build());

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext(
                "EventId",
                asList(
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId1"),
                        null),
                    eventCaseFieldWithEventComplexTypeEntity,
                    eventCaseFieldEntity(
                        caseFieldEntity(matchingCaseFieldId,
                            exampleFieldTypeEntityWithComplexFields()),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null)
                ));

        assertTrue(classUnderTest.validate(eventComplexTypeEntityWithShowCondition,
            eventCaseFieldEntityValidationContext)
            .isValid());
    }

    @Test
    public void shouldValidateShowConditionForCustomComplexField() throws InvalidShowConditionException {

        String matchingCaseFieldKey = "MatchingCaseFieldId1";
        String showCondition = matchingCaseFieldKey + "=\"UK\"";

        EventComplexTypeEntity eventComplexTypeEntityWithShowCondition = eventComplexTypeEntity("reference1",
            showCondition);

        EventCaseFieldEntity eventCaseFieldWithEventComplexTypeEntity = eventCaseFieldEntity(
            null,
            asList(eventComplexTypeEntityWithShowCondition));

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(showCondition).field(matchingCaseFieldKey).build());

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext(
                "EventId",
                asList(
                    eventCaseFieldEntity(
                        caseFieldEntity(matchingCaseFieldKey),
                        null),
                    eventCaseFieldWithEventComplexTypeEntity,
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null)
                ));

        assertTrue(classUnderTest.validate(eventComplexTypeEntityWithShowCondition,
            eventCaseFieldEntityValidationContext)
            .isValid());
    }

    @Test
    public void shouldValidateShowConditionForMetadataField() throws InvalidShowConditionException {
        String field = MetadataField.STATE.getReference();
        String showCondition = field + "=\"TODO\"";

        EventComplexTypeEntity eventComplexTypeEntityWithShowCondition = eventComplexTypeEntity("reference1",
            showCondition);

        EventCaseFieldEntity eventCaseFieldWithEventComplexTypeEntity = eventCaseFieldEntity(
            null,
            asList(eventComplexTypeEntityWithShowCondition));

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(showCondition).field(field).build());

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext(
                "EventId",
                asList(
                    eventCaseFieldWithEventComplexTypeEntity,
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId"),
                        null)
                ));

        assertTrue(classUnderTest.validate(eventComplexTypeEntityWithShowCondition,
            eventCaseFieldEntityValidationContext)
            .isValid());
    }

    @Test
    public void shouldFailForBlankShowCondition() throws InvalidShowConditionException {
        EventComplexTypeEntity eventComplexTypeEntity = eventComplexTypeEntity("reference1",
            "");

        ValidationResult validationResult = classUnderTest.validate(eventComplexTypeEntity, null);

        assertTrue(validationResult.isValid());
    }

    @Test
    public void failsWithEventComplexTypeEntityInvalidShowConditionErrorForInvalidShowCondition()
        throws InvalidShowConditionException {

        String showCondition = "InvalidShowCondition";
        EventComplexTypeEntity eventComplexTypeEntityWithInvalidShowCondition = eventComplexTypeEntity("reference1",
            showCondition);

        EventCaseFieldEntityValidationContext eventCaseFieldEntityWithInvalidShowCondition =
            new EventCaseFieldEntityValidationContext(null, null);

        when(showConditionExtractor.parseShowCondition(any())).thenThrow(new InvalidShowConditionException(null));

        ValidationResult validationResult = classUnderTest.validate(eventComplexTypeEntityWithInvalidShowCondition,
            eventCaseFieldEntityWithInvalidShowCondition);

        verify(showConditionExtractor).parseShowCondition(eq(showCondition));

        assertFalse(validationResult.isValid());

        assertEquals(1, validationResult.getValidationErrors().size());
        Assert.assertTrue(validationResult.getValidationErrors()
            .get(0) instanceof EventComplexTypeEntityInvalidShowConditionError);
    }

    @Test
    public void failsWithEventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldErrorForInvalidShowCondition()
        throws InvalidShowConditionException {

        String matchingCaseFieldId = "complexName";
        String matchingCaseFieldKey = matchingCaseFieldId + ".LastNameWithSomeCplxFields.SomeComplexFieldsCode";
        String showCondition = matchingCaseFieldKey + "=\"Moreno\"";

        EventComplexTypeEntity eventComplexTypeEntityWithShowCondition = eventComplexTypeEntity("reference1",
            showCondition);

        EventCaseFieldEntity eventCaseFieldWithEventComplexTypeEntity = eventCaseFieldEntity(
            null,
            asList(eventComplexTypeEntityWithShowCondition));


        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(showCondition).field(matchingCaseFieldKey).build());

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext(
                "EventId",
                asList(
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId1"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity(matchingCaseFieldId,
                            exampleFieldTypeEntityWithComplexFields()),
                        null),
                    eventCaseFieldWithEventComplexTypeEntity,
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null),
                    eventCaseFieldEntity(
                        caseFieldEntity("NonMatchingCaseFieldId2"),
                        null)
                ));

        ValidationResult validationResult = classUnderTest.validate(eventComplexTypeEntityWithShowCondition,
            eventCaseFieldEntityValidationContext);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        Assert.assertTrue(validationResult.getValidationErrors()
            .get(0) instanceof EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError);

    }

    private EventComplexTypeEntity eventComplexTypeEntity(String reference,
                                                          String showCondition) {
        EventComplexTypeEntity eventComplexTypeEntity = new EventComplexTypeEntity();
        eventComplexTypeEntity.setReference(reference);
        eventComplexTypeEntity.setShowCondition(showCondition);
        return eventComplexTypeEntity;
    }

    private static EventCaseFieldEntity eventCaseFieldEntity(CaseFieldEntity caseFieldEntity,
                                                             List<EventComplexTypeEntity> eventComplexTypeEntities) {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setEvent(new EventEntity());
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        eventCaseFieldEntity.addComplexFields(eventComplexTypeEntities);
        return eventCaseFieldEntity;
    }

    private static CaseFieldEntity caseFieldEntity(String reference,
                                                   FieldTypeEntity fieldTypeEntity) {
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
                                    complexFieldEntity("SecondSurname", fieldTypeEntity("Text", emptyList()))
                                )))
                    ))))
        );
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

    private static FieldTypeEntity orderSummaryFieldTypeEntity() {
        return fieldTypeEntity(PREDEFINED_COMPLEX_ORDER_SUMMARY,
            asList(
                complexFieldEntity("PaymentReference", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("PaymentTotal", fieldTypeEntity("MoneyGBP", emptyList())),
                complexFieldEntity("Fees", fieldTypeEntity("FeesList", emptyList()))
            ));
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

    private static ComplexFieldEntity complexFieldEntity(String reerence,
                                                         FieldTypeEntity fieldTypeEntity) {
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

    private static FieldTypeListItemEntity fieldTypeListItemEntity(String label,
                                                                   String value) {
        FieldTypeListItemEntity fieldTypeListItemEntity = new FieldTypeListItemEntity();
        fieldTypeListItemEntity.setLabel(label);
        fieldTypeListItemEntity.setValue(value);
        return fieldTypeListItemEntity;
    }
}
