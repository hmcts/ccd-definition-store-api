package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.LegacyCaseTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules.CaseTypeValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.TestValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class CaseTypeServiceImplTest {

    private static final int DEFAULT_VERSION = 69;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Mock
    private EntityToResponseDTOMapper caseTypeMapper;

    @Mock
    private LegacyCaseTypeValidator legacyCaseTypeValidator;

    @Mock
    private CaseTypeEntityValidator caseTypeEntityValidator1;

    @Mock
    private CaseTypeEntityValidator caseTypeEntityValidator2;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor
    private ArgumentCaptor<Collection<CaseTypeEntity>> captor;

    private JurisdictionEntity jurisdiction = new JurisdictionEntity();

    private CaseTypeEntity caseTypeEntity1 = new CaseTypeEntity();

    private CaseTypeEntity caseTypeEntity2 = new CaseTypeEntity();

    private CaseTypeEntity caseTypeEntity3 = new CaseTypeEntity();

    private Collection<CaseTypeEntity> caseTypeEntities = Arrays.asList(caseTypeEntity1, caseTypeEntity2, caseTypeEntity3);

    private CaseTypeServiceImpl classUnderTest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        classUnderTest = new CaseTypeServiceImpl(
            caseTypeRepository,
            caseTypeMapper,
            legacyCaseTypeValidator,
            Arrays.asList(caseTypeEntityValidator1, caseTypeEntityValidator2),
            applicationEventPublisher
        );
    }

    @Nested
    class CreateAllTests {

        @BeforeEach
        public void setUp () {
            when(caseTypeRepository.findLastVersion(any())).thenReturn(Optional.of(DEFAULT_VERSION));
            when(caseTypeEntityValidator1.validate(any())).thenReturn(new ValidationResult());
            when(caseTypeEntityValidator2.validate(any())).thenReturn(new ValidationResult());
        }

        @Test
        @DisplayName("Should add the jurisdiction to all items is list, validate and same them all if they are all valid")
        public void shouldAddJurisdictionToAllCaseTypeEntitiesValidateThenSave_whenCaseTypeEntitesAreAllValid() {
            classUnderTest.createAll(jurisdiction, caseTypeEntities);
            assertComponentsCalled(true, null);
        }

        @Test
        @DisplayName("Should add the jurisdiction to all items is list, validate and throw a ValidationException with details or all invalid entities "
            + "if any are invalid")
        public void shouldAddJurisdictionToAllCaseTypeEntitiesValidateAndThrowValidationResultWithoutSaving_whenAnyCaseTypeEntitesAreInValid() {

            when(caseTypeEntityValidator1.validate(eq(caseTypeEntity1)))
                .thenReturn(validationResultWithError(validationErrorWithDefaultMessage("caseTypeEntityValidator1 failed for caseTypeEntity1")));
            when(caseTypeEntityValidator2.validate(eq(caseTypeEntity3)))
                .thenReturn(validationResultWithError(validationErrorWithDefaultMessage("caseTypeEntityValidator2 failed for caseTypeEntity3")));

            ValidationException validationException
                = assertThrows(ValidationException.class, () -> classUnderTest.createAll(jurisdiction, caseTypeEntities));

            ValidationResult validationResult = validationException.getValidationResult();
            assertFalse(validationResult.isValid());
            assertEquals(2, validationResult.getValidationErrors().size());

            assertThat(validationResult.getValidationErrors(), allOf(
                hasItem(matchesValidationErrorWithDefaultMessage("caseTypeEntityValidator1 failed for caseTypeEntity1")),
                hasItem(matchesValidationErrorWithDefaultMessage("caseTypeEntityValidator2 failed for caseTypeEntity3"))
                )
            );

            assertComponentsCalled(false, null);

        }

        @Test
        @DisplayName("Should throw propagate the CaseTypeValidationException thrown by the LegacyCaseTypeValidator")
        public void shouldThrowCaseTypeValidationExceptionWithoutSaving_whenLegacyCaseTypeValidatorThrowsCaseTypeValidationException() {

            CaseTypeValidationException caseTypeValidationException = new CaseTypeValidationException(new CaseTypeValidationResult());
            doThrow(caseTypeValidationException)
                .when(legacyCaseTypeValidator).validateCaseType(argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity2, jurisdiction)));

            CaseTypeValidationException actualCaseTypeValidationException = assertThrows(CaseTypeValidationException.class, () -> classUnderTest.createAll(jurisdiction, caseTypeEntities));

            assertTrue(actualCaseTypeValidationException == caseTypeValidationException);

            assertComponentsCalled(false, caseTypeEntity2);

        }

        private <T> Matcher<T> matchesValidationErrorWithDefaultMessage(String defaultMessage) {
            return new BaseMatcher<T>() {
                @Override
                public boolean matches(Object o) {
                    return o instanceof ValidationError
                        && ((ValidationError) o).getDefaultMessage().equals(defaultMessage);
                }

                @Override
                public void describeTo(Description description) {
                    description.appendText("a ValidationError with defaultMessage " + defaultMessage);
                }
            };
        }

        private ValidationResult validationResultWithError(ValidationError validationError) {
            ValidationResult validationResult = new ValidationResult();
            validationResult.addError(validationError);
            return validationResult;
        }

        private ValidationError validationErrorWithDefaultMessage(String defaultMessage) {
            return new TestValidationError(defaultMessage);
        }

        private <T> Matcher<T> matchesCaseTypeEntityWithJurisdictionAdded(CaseTypeEntity caseTypeEntity1, JurisdictionEntity jurisdiction) {

            return new BaseMatcher<T>() {
                @Override
                public boolean matches(Object o) {
                    return o == caseTypeEntity1
                        && ((CaseTypeEntity) o).getJurisdiction().equals(jurisdiction);
                }

                @Override
                public void describeTo(Description description) {
                    description.appendText("Expected to be the same CaseTypeEntity instance with the jurisdiction set");
                }
            };

        }

        private <T> Matcher<T> matchesCaseTypeEntityWithJurisdictionAndVersionAdded(CaseTypeEntity caseTypeEntity1,
                                                                                    JurisdictionEntity jurisdiction,
                                                                                    int version) {

            return new BaseMatcher<T>() {
                @Override
                public boolean matches(Object o) {
                    return o == caseTypeEntity1
                        && ((CaseTypeEntity) o).getJurisdiction().equals(jurisdiction)
                        && ((CaseTypeEntity) o).getVersion().equals(version);
                }

                @Override
                public void describeTo(Description description) {
                    description.appendText("Expected to be the same CaseTypeEntity instance with the jurisdiction set");
                }
            };

        }

        private void assertComponentsCalled(boolean shouldSave, CaseTypeEntity caseTypeWithLegacyValidationException) {

            InOrder inOrder = Mockito.inOrder(
                legacyCaseTypeValidator,
                caseTypeEntityValidator1, caseTypeEntityValidator2,
                caseTypeRepository
            );

            for (CaseTypeEntity caseTypeEntity : caseTypeEntities) {
                inOrder.verify(legacyCaseTypeValidator).validateCaseType(argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity, jurisdiction)));
                if (caseTypeWithLegacyValidationException != null && caseTypeWithLegacyValidationException == caseTypeEntity) {
                    return;
                }
                inOrder.verify(caseTypeEntityValidator1).validate(argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity, jurisdiction)));
                inOrder.verify(caseTypeEntityValidator2).validate(argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity, jurisdiction)));
            }

            if (shouldSave) {
                inOrder.verify(caseTypeRepository).save(captor.capture());
                Collection<CaseTypeEntity> savedCaseTypeEntities = captor.getValue();
                assertEquals(caseTypeEntities.size(), savedCaseTypeEntities.size());

                for (CaseTypeEntity caseTypeEntity : caseTypeEntities) {
                    assertThat(savedCaseTypeEntities, hasItem(matchesCaseTypeEntityWithJurisdictionAndVersionAdded(caseTypeEntity, jurisdiction, DEFAULT_VERSION + 1)));
                }

            }

            inOrder.verifyNoMoreInteractions();

        }

    }

    @Nested
    class FindByJurisdictionIdTests {

        @Test
        @DisplayName("Should map each CaseTypeEntity to a CaseType and return a list of mapped CaseTypes")
        public void shouldOrderEventsBeforeCallingMapperForEachCaseTypeAndReturnListOfMappedCaseTypes_whenRepositoryReturnsCaseTypeEntitiesWithDisorderedEvents() {

            String jurisdiction = "Jurisdiction";

            EventEntity eventEntity1 = eventEntity(1);
            EventEntity eventEntity2 = eventEntity(2);
            EventEntity eventEntity3 = eventEntity(3);
            EventEntity eventEntity4 = eventEntity(4);

            CaseTypeEntity caseTypeEntity1 = caseTypeEntity(eventEntity2, eventEntity1, eventEntity4, eventEntity3);
            CaseTypeEntity caseTypeEntity2 = caseTypeEntity(eventEntity3, eventEntity4, eventEntity2, eventEntity1);

            CaseType caseType1 = new CaseType();
            CaseType caseType2 = new CaseType();

            when(caseTypeRepository.findByJurisdictionId(any())).thenReturn(
                Arrays.asList(caseTypeEntity1, caseTypeEntity2)
            );
            when(caseTypeMapper.map(same(caseTypeEntity1))).thenReturn(caseType1);
            when(caseTypeMapper.map(same(caseTypeEntity2))).thenReturn(caseType2);

            ArgumentCaptor<CaseTypeEntity> caseTypeCaptor = ArgumentCaptor.forClass(CaseTypeEntity.class);

            List<CaseType> caseTypes = classUnderTest.findByJurisdictionId(jurisdiction);

            verify(caseTypeRepository).findByJurisdictionId(same(jurisdiction));
            verify(caseTypeMapper, times(2)).map(caseTypeCaptor.capture());

            CaseTypeEntity firstCallToMapper = caseTypeCaptor.getAllValues().get(0);
            assertTrue(firstCallToMapper == caseTypeEntity1);
            assertThat(firstCallToMapper.getEvents(), hasItem(eventEntity1));
            assertThat(firstCallToMapper.getEvents(), hasItem(eventEntity2));
            assertThat(firstCallToMapper.getEvents(), hasItem(eventEntity3));
            assertThat(firstCallToMapper.getEvents(), hasItem(eventEntity4));

            CaseTypeEntity secondCallToMapper = caseTypeCaptor.getAllValues().get(1);
            assertTrue(secondCallToMapper == caseTypeEntity2);
            assertThat(secondCallToMapper.getEvents(), hasItem(eventEntity1));
            assertThat(secondCallToMapper.getEvents(), hasItem(eventEntity2));
            assertThat(secondCallToMapper.getEvents(), hasItem(eventEntity3));
            assertThat(secondCallToMapper.getEvents(), hasItem(eventEntity4));

            assertEquals(2, caseTypes.size());
            assertThat(caseTypes, allOf(
                hasItem(caseType1),
                hasItem(caseType2)
                )
            );
        }

        @Test
        @DisplayName("Should return an empty list when the repository returns an empty list")
        public void shouldReturnEmptyList_whenRepositoryReturnsEmptyList() {
            String jurisdiction = "Jurisdiction";

            when(caseTypeRepository.findByJurisdictionId(any())).thenReturn(
                Collections.emptyList()
            );

            List<CaseType> caseTypes = classUnderTest.findByJurisdictionId(jurisdiction);

            verify(caseTypeRepository).findByJurisdictionId(same(jurisdiction));
            verifyZeroInteractions(caseTypeMapper);

            assertTrue(caseTypes.isEmpty());
        }

        @Test
        @DisplayName("Should return an empty list when the repository returns null")
        public void shouldReturnEmptyList_whenRepositoryReturnsNull() {
            String jurisdiction = "Jurisdiction";

            when(caseTypeRepository.findByJurisdictionId(any())).thenReturn(
                null
            );

            List<CaseType> caseTypes = classUnderTest.findByJurisdictionId(jurisdiction);

            verify(caseTypeRepository).findByJurisdictionId(same(jurisdiction));
            verifyZeroInteractions(caseTypeMapper);

            assertTrue(caseTypes.isEmpty());
        }

        private CaseTypeEntity caseTypeEntity(EventEntity... eventEntites) {
            CaseTypeEntity caseType = new CaseTypeEntity();
            caseType.addEvents(Arrays.asList(eventEntites));
            return caseType;
        }

        private EventEntity eventEntity(int order) {
            EventEntity eventEntity = new EventEntity();
            eventEntity.setOrder(order);
            return eventEntity;
        }

    }

    @Nested
    class FindByCaseTypeIdTests {

        @Test
        @DisplayName("Should call the mapper with the value returned from the repository and return the mapped value")
        public void shouldCallMapperAndReturnResult_whenRepositoryReturnsAnEntity() {
            String caseTypeId = "caseTypeID";
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            CaseType caseTypeFromMapper = new CaseType();
            when(caseTypeRepository.findCurrentVersionForReference(any())).thenReturn(
                Optional.of(caseTypeEntity)
            );
            when(caseTypeMapper.map(any(CaseTypeEntity.class))).thenReturn(
                caseTypeFromMapper
            );

            Optional<CaseType> caseType = classUnderTest.findByCaseTypeId(caseTypeId);

            verify(caseTypeRepository).findCurrentVersionForReference(same(caseTypeId));
            verify(caseTypeMapper).map(same(caseTypeEntity));
            assertTrue(caseType.isPresent());
            assertTrue(caseType.get() == caseTypeFromMapper);
        }

        @Test
        @DisplayName("Should return an empty Optional when the repository returns an empty Optional")
        public void shouldThrowNotFoundException_whenRepositoryReturnsEmptyOptional() {
            String caseTypeId = "caseTypeID";

            when(caseTypeRepository.findCurrentVersionForReference(any())).thenReturn(
                Optional.empty()
            );

            Optional<CaseType> caseType = classUnderTest.findByCaseTypeId(caseTypeId);

            assertFalse(caseType.isPresent());
        }
    }

    @Nested
    @DisplayName("Find Version Info")
    class FindVersionInfo {

        private static final String CASE_TYPE_REFERENCE = "xngi";

        @Test
        @DisplayName("Green Path - should return info object")
        void greenPath() {
            when(caseTypeRepository.findLastVersion(CASE_TYPE_REFERENCE)).thenReturn(Optional.of(789));
            Optional<CaseTypeVersionInformation> info = classUnderTest.findVersionInfoByCaseTypeId(CASE_TYPE_REFERENCE);
            assertThat(info.get().getVersion(), is(789));
        }

        @Test
        @DisplayName("Case type not found")
        void notFound() {
            when(caseTypeRepository.findLastVersion(CASE_TYPE_REFERENCE)).thenReturn(Optional.empty());
            Optional<CaseTypeVersionInformation> info = classUnderTest.findVersionInfoByCaseTypeId(CASE_TYPE_REFERENCE);
            assertFalse(info.isPresent());
        }
    }
}
