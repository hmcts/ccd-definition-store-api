package uk.gov.hmcts.ccd.definition.store.domain.service;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.PageEventMissingDisplayGroupValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutEntityValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.GenericLayoutRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketInputCaseFieldEntity;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LayoutServiceImplTest {

    private static final String UNIQUE_CONSTRAINT_VALIDATION_ERROR = "Following have to be unique: [CaseTypeID '%s', CaseFieldID '%s', ListElementCode '%s'] with label '%s'";

    @Mock
    private DisplayGroupRepository displayGroupRepository;

    @Mock
    private GenericLayoutRepository genericRepository;

    @Captor
    private ArgumentCaptor<Collection<DisplayGroupEntity>> displayGroupCaptor;

    @Captor
    private ArgumentCaptor<Collection<GenericLayoutEntity>> genericLayoutCaptor;

    private LayoutServiceImpl classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new LayoutServiceImpl(
            genericRepository,
            asList(new GenericLayoutEntityValidatorImpl()),
            displayGroupRepository,
            asList(new PageEventMissingDisplayGroupValidatorImpl())
        );
    }

    @Test
    public void shouldValidateAndSavePromptlyWhenSingleEntityIsValid() {
        GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
        entity1.setCaseField(createCaseFieldEntity("ComplexCollectionComplex"));
        entity1.setCaseType(createCaseTypeEntity("FamilyDetails"));

        classUnderTest.createGenerics(asList(entity1));

        verify(genericRepository).saveAll(genericLayoutCaptor.capture());
        Collection<GenericLayoutEntity> savedDisplayGroupEntities = genericLayoutCaptor.getValue();
        assertEquals(1, savedDisplayGroupEntities.size());
        assertThat(savedDisplayGroupEntities, allOf(hasItem(entity1)));
    }

    @Test
    public void shouldValidateAndSavePromptlyWhenEntityIsValid() {
        GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
        entity1.setCaseField(createCaseFieldEntity("ComplexCollectionComplex"));
        entity1.setCaseType(createCaseTypeEntity("FamilyDetails"));

        GenericLayoutEntity entity2 = new WorkBasketInputCaseFieldEntity();
        entity2.setCaseField(createCaseFieldEntity("ComplexCollectionComplex"));
        entity2.setCaseType(createCaseTypeEntity("Homeless"));

        classUnderTest.createGenerics(asList(entity1, entity2));

        verify(genericRepository).saveAll(genericLayoutCaptor.capture());
        Collection<GenericLayoutEntity> savedDisplayGroupEntities = genericLayoutCaptor.getValue();
        assertEquals(2, savedDisplayGroupEntities.size());
        assertThat(savedDisplayGroupEntities, allOf(hasItem(entity1), hasItem(entity2)));
    }

    @Test
    public void shouldFailIfCaseTypeCaseFieldAndListElementCodeNotUniqueForWorkbasketInput() {
        String caseTypeRef = "ComplexCollectionComplex";
        String caseFieldRef = "FamilyDetails";
        String elementPath = "MotherName";

        GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
        entity1.setCaseField(createCaseFieldEntity(caseFieldRef));
        entity1.setCaseType(createCaseTypeEntity(caseTypeRef));
        entity1.setCaseFieldElementPath(elementPath);
        entity1.setLabel("label1");

        GenericLayoutEntity entity2 = new WorkBasketInputCaseFieldEntity();
        entity2.setCaseField(createCaseFieldEntity(caseFieldRef));
        entity2.setCaseType(createCaseTypeEntity(caseTypeRef));
        entity2.setCaseFieldElementPath(elementPath);
        entity2.setLabel("label2");

        GenericLayoutEntity otherEntity = new WorkBasketInputCaseFieldEntity();
        otherEntity.setCaseField(createCaseFieldEntity(caseFieldRef));
        otherEntity.setCaseType(createCaseTypeEntity("School"));
        otherEntity.setCaseFieldElementPath("SchoolName");

        classUnderTest = new LayoutServiceImpl(
            genericRepository,
            emptyList(),
            displayGroupRepository,
            emptyList()
        );

        try {
            classUnderTest.createGenerics(asList(entity1, entity2, otherEntity));
            fail();
        } catch (ValidationException ex) {
            assertThat(ex.getValidationResult().getValidationErrors().size(), is(2));
            assertEquals(1, ex.getValidationResult().getValidationErrors()
                .stream().filter(e -> e.getDefaultMessage().equals(format(UNIQUE_CONSTRAINT_VALIDATION_ERROR,
                caseTypeRef, caseFieldRef, elementPath, "label1")))
                .count());
            assertEquals(1, ex.getValidationResult().getValidationErrors()
                .stream().filter(e -> e.getDefaultMessage().equals(format(UNIQUE_CONSTRAINT_VALIDATION_ERROR,
                    caseTypeRef, caseFieldRef, elementPath, "label2")))
                .count());
        }
    }

    @Test
    public void shouldFailIfCaseTypeCaseFieldNotUniqueAndListElementCodeNullForWorkbasketInput1() {
        String caseTypeRef = "ComplexCollectionComplex";
        String caseFieldRef = "FamilyDetails";
        String elementPath = null;

        GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
        entity1.setCaseField(createCaseFieldEntity(caseFieldRef));
        entity1.setCaseType(createCaseTypeEntity(caseTypeRef));
        entity1.setCaseFieldElementPath(elementPath);
        entity1.setLabel("label1");

        GenericLayoutEntity entity2 = new WorkBasketInputCaseFieldEntity();
        entity2.setCaseField(createCaseFieldEntity(caseFieldRef));
        entity2.setCaseType(createCaseTypeEntity(caseTypeRef));
        entity2.setCaseFieldElementPath(elementPath);
        entity2.setLabel("label2");

        GenericLayoutEntity otherEntity = new WorkBasketInputCaseFieldEntity();
        otherEntity.setCaseField(createCaseFieldEntity(caseFieldRef));
        otherEntity.setCaseType(createCaseTypeEntity("School"));
        otherEntity.setCaseFieldElementPath("SchoolName");

        classUnderTest = new LayoutServiceImpl(
            genericRepository,
            emptyList(),
            displayGroupRepository,
            emptyList()
        );

        try {
            classUnderTest.createGenerics(asList(entity1, entity2, otherEntity));
            fail();
        } catch (ValidationException ex) {
            assertThat(ex.getValidationResult().getValidationErrors().size(), is(2));
            assertEquals(1, ex.getValidationResult().getValidationErrors()
                .stream().filter(e -> e.getDefaultMessage().equals(format(UNIQUE_CONSTRAINT_VALIDATION_ERROR,
                    caseTypeRef, caseFieldRef, elementPath, "label1")))
                .count());
            assertEquals(1, ex.getValidationResult().getValidationErrors()
                .stream().filter(e -> e.getDefaultMessage().equals(format(UNIQUE_CONSTRAINT_VALIDATION_ERROR,
                    caseTypeRef, caseFieldRef, elementPath, "label2")))
                .count());
        }
    }

    @Test(expected = ValidationException.class)
    public void shouldFailIfCaseFieldIsNotProvidedForWorkbasketInput() {
        GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
        entity1.setCaseType(createCaseTypeEntity("FamilyDetails"));

        classUnderTest.createGenerics(singletonList(entity1));
    }

    @Test(expected = ValidationException.class)
    public void shouldFailIfCaseTypeIsNotProvidedForWorkbasketInput() {
        GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
        entity1.setCaseField(createCaseFieldEntity("ComplexCollectionComplex"));

        classUnderTest.createGenerics(singletonList(entity1));
    }

    @Test(expected = ValidationException.class)
    public void pageTypeDisplayGroupMustFailIfEventIsNotProvided() {
        DisplayGroupEntity dg1 = new DisplayGroupEntity();
        dg1.setType(DisplayGroupType.PAGE);
        dg1.setEvent(new EventEntity());
        DisplayGroupEntity dg2 =  new DisplayGroupEntity();
        dg2.setType(DisplayGroupType.PAGE);
        final List<DisplayGroupEntity> displayGroupEntities = asList(dg1, dg2);

        classUnderTest.createDisplayGroups(displayGroupEntities);
    }

    @Test
    public void tabTypeDisplayGroupMustNotExpectEventAndSavePromptly() {
        DisplayGroupEntity dg1 = new DisplayGroupEntity();
        dg1.setType(DisplayGroupType.TAB);
        dg1.setEvent(new EventEntity());
        DisplayGroupEntity dg2 =  new DisplayGroupEntity();
        dg2.setType(DisplayGroupType.TAB);
        final List<DisplayGroupEntity> displayGroupEntities = asList(dg1, dg2);

        classUnderTest.createDisplayGroups(displayGroupEntities);

        verify(displayGroupRepository).saveAll(displayGroupCaptor.capture());
        Collection<DisplayGroupEntity> savedDisplayGroupEntities = displayGroupCaptor.getValue();
        assertEquals(2, savedDisplayGroupEntities.size());
        assertThat(savedDisplayGroupEntities, allOf(hasItem(dg1), hasItem(dg2)));
    }

    private CaseFieldEntity createCaseFieldEntity(String reference) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(createTextType());
        return caseFieldEntity;
    }

    private FieldTypeEntity createTextType() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setBaseFieldType(textFieldTypeEntity());
        return fieldTypeEntity;
    }

    private CaseTypeEntity createCaseTypeEntity(String reference) {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(reference);
        return caseTypeEntity;
    }

    private static FieldTypeEntity textFieldTypeEntity() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference("Text");
        fieldTypeEntity.addComplexFields(emptyList());
        return fieldTypeEntity;
    }
}
