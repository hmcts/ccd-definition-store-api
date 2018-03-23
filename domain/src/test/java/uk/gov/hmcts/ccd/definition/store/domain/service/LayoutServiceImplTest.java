package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.PageEventMissingDisplayGroupValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutEntityValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.GenericLayoutRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

public class LayoutServiceImplTest {

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
            Arrays.asList(new GenericLayoutEntityValidatorImpl()),
            displayGroupRepository,
            Arrays.asList(new PageEventMissingDisplayGroupValidatorImpl())
        );
    }

    @Test(expected = ValidationException.class)
    public void shouldFailIfCaseFieldIsNotProvidedForWorkbasketInput() {
        GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
        CaseTypeEntity caseTypeEntity1 = new CaseTypeEntity();
        entity1.setCaseType(caseTypeEntity1);
        classUnderTest.createGenerics(Arrays.asList(entity1));
    }

    @Test(expected = ValidationException.class)
    public void shouldFailIfCaseTypeIsNotProvidedForWorkbasketInput() {
        GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
        CaseFieldEntity caseFieldEntity1 = new CaseFieldEntity();
        entity1.setCaseField(caseFieldEntity1);
        classUnderTest.createGenerics(Arrays.asList(entity1));
    }

    @Test
    public void shouldValidateAndSavePromptlyWhenEntityIsValid() {
        GenericLayoutEntity entity1 = new WorkBasketInputCaseFieldEntity();
        CaseFieldEntity caseFieldEntity1 = new CaseFieldEntity();
        entity1.setCaseField(caseFieldEntity1);
        CaseTypeEntity caseTypeEntity1 = new CaseTypeEntity();
        entity1.setCaseType(caseTypeEntity1);
        GenericLayoutEntity entity2 = new WorkBasketInputCaseFieldEntity();
        CaseFieldEntity caseFieldEntity2 = new CaseFieldEntity();
        CaseTypeEntity caseTypeEntity2 = new CaseTypeEntity();
        entity2.setCaseField(caseFieldEntity2);
        entity2.setCaseType(caseTypeEntity2);
        classUnderTest.createGenerics(Arrays.asList(entity1, entity2));

        verify(genericRepository).save(genericLayoutCaptor.capture());
        Collection<GenericLayoutEntity> savedDisplayGroupEntities = genericLayoutCaptor.getValue();
        assertEquals(2, savedDisplayGroupEntities.size());
        assertThat(savedDisplayGroupEntities, allOf( hasItem(entity1), hasItem(entity2)));
    }

    @Test(expected = ValidationException.class)
    public void pageTypeDisplayGroupMustFailIfEventIsNotProvided() {
        DisplayGroupEntity dg1 = new DisplayGroupEntity();
        dg1.setType(DisplayGroupType.PAGE);
        dg1.setEvent(new EventEntity());
        DisplayGroupEntity dg2 =  new DisplayGroupEntity();
        dg2.setType(DisplayGroupType.PAGE);
        final List<DisplayGroupEntity> displayGroupEntities = Arrays.asList(dg1, dg2);
        classUnderTest.createDisplayGroups(displayGroupEntities);
    }

    @Test
    public void tabTypeDisplayGroupMustNotExpectEventAndSavePromptly() {
        DisplayGroupEntity dg1 = new DisplayGroupEntity();
        dg1.setType(DisplayGroupType.TAB);
        dg1.setEvent(new EventEntity());
        DisplayGroupEntity dg2 =  new DisplayGroupEntity();
        dg2.setType(DisplayGroupType.TAB);
        final List<DisplayGroupEntity> displayGroupEntities = Arrays.asList(dg1, dg2);
        classUnderTest.createDisplayGroups(displayGroupEntities);

        verify(displayGroupRepository).save(displayGroupCaptor.capture());
        Collection<DisplayGroupEntity> savedDisplayGroupEntities = displayGroupCaptor.getValue();
        assertEquals(2, savedDisplayGroupEntities.size());
        assertThat(savedDisplayGroupEntities, allOf( hasItem(dg1), hasItem(dg2)));
    }
}
