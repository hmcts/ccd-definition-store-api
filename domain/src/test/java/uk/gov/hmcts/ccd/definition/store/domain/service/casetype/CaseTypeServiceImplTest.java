package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.LegacyCaseTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules.CaseTypeValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataFieldService;
import uk.gov.hmcts.ccd.definition.store.domain.validation.TestValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_FIXED_LIST;

class CaseTypeServiceImplTest {

    private static final String JURISDICTION_REFERENCE = "TEST";
    private static final String CASE_TYPE_REFERENCE_1 = "TestAddressBookCase1";
    private static final String CASE_TYPE_REFERENCE_2 = "TestAddressBookCase2";
    private static final String CASE_TYPE_REFERENCE_3 = "TestAddressBookCase3";
    private static final int DEFAULT_VERSION = 69;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @Mock
    private LegacyCaseTypeValidator legacyCaseTypeValidator;

    @Mock
    private CaseTypeEntityValidator caseTypeEntityValidator1;

    @Mock
    private CaseTypeEntityValidator caseTypeEntityValidator2;

    @Mock
    private MetadataFieldService metadataFieldService;

    @Captor
    private ArgumentCaptor<Collection<CaseTypeEntity>> captor;

    private final JurisdictionEntity jurisdiction = new JurisdictionEntity();

    private final CaseTypeEntity caseTypeEntity1 = new CaseTypeEntity();

    private final CaseTypeEntity caseTypeEntity2 = new CaseTypeEntity();

    private final CaseTypeEntity caseTypeEntity3 = new CaseTypeEntity();

    private final Collection<CaseTypeEntity> caseTypeEntities = Arrays.asList(caseTypeEntity1, caseTypeEntity2,
        caseTypeEntity3);

    private CaseTypeServiceImpl classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        caseTypeEntity1.setReference(CASE_TYPE_REFERENCE_1);
        caseTypeEntity2.setReference(CASE_TYPE_REFERENCE_2);
        caseTypeEntity3.setReference(CASE_TYPE_REFERENCE_3);
        jurisdiction.setReference(JURISDICTION_REFERENCE);

        classUnderTest = new CaseTypeServiceImpl(
            caseTypeRepository,
            dtoMapper,
            legacyCaseTypeValidator,
            Arrays.asList(caseTypeEntityValidator1, caseTypeEntityValidator2),
            metadataFieldService);
    }

    @Nested
    class CreateAllTests {

        @BeforeEach
        void setUp() {
            when(caseTypeRepository.findLastVersion(any())).thenReturn(Optional.of(DEFAULT_VERSION));
            when(caseTypeEntityValidator1.validate(any())).thenReturn(new ValidationResult());
            when(caseTypeEntityValidator2.validate(any())).thenReturn(new ValidationResult());
            when(caseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE_1))
                .thenReturn(Optional.of(caseTypeEntity1));
            final CaseType caseType = new CaseType();
            caseType.setId(CASE_TYPE_REFERENCE_1);
            when(dtoMapper.map(same(caseTypeEntity1))).thenReturn(caseType);
            when(caseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE_2))
                .thenReturn(Optional.empty());
            when(caseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE_3))
                .thenReturn(Optional.empty());
        }

        @Test
        @DisplayName(
            "Should add the jurisdiction to all items in list, validate and save them all if they are all valid")
        void shouldAddJurisdictionToAllCaseTypeEntitiesValidateThenSave_whenCaseTypeEntitiesAreAllValid() {
            classUnderTest.createAll(jurisdiction, caseTypeEntities, new HashSet<>());
            assertComponentsCalled(true, null);
        }

        @SuppressWarnings("checkstyle:LineLength")
        @Test
        @DisplayName(
            "Should add the jurisdiction to all items in list, validate and throw a ValidationException with details "
                + "of all invalid entities if any are invalid")
        void shouldAddJurisdictionToAllCaseTypeEntitiesValidateAndThrowValidationResultWithoutSaving_whenAnyCaseTypeEntitiesAreInValid() {

            when(caseTypeEntityValidator1.validate(eq(caseTypeEntity1)))
                .thenReturn(validationResultWithError(
                    validationErrorWithDefaultMessage("caseTypeEntityValidator1 failed for caseTypeEntity1")));
            when(caseTypeEntityValidator2.validate(eq(caseTypeEntity3)))
                .thenReturn(validationResultWithError(
                    validationErrorWithDefaultMessage("caseTypeEntityValidator2 failed for caseTypeEntity3")));

            ValidationException validationException
                = assertThrows(ValidationException.class, () -> classUnderTest.createAll(
                    jurisdiction, caseTypeEntities, new HashSet<>()));

            ValidationResult validationResult = validationException.getValidationResult();
            assertFalse(validationResult.isValid());
            assertEquals(2, validationResult.getValidationErrors().size());

            assertThat(validationResult.getValidationErrors(), allOf(
                hasItem(
                    matchesValidationErrorWithDefaultMessage("caseTypeEntityValidator1 failed for caseTypeEntity1")),
                hasItem(matchesValidationErrorWithDefaultMessage("caseTypeEntityValidator2 failed for caseTypeEntity3"))
                )
            );

            assertComponentsCalled(false, null);

        }

        @SuppressWarnings("checkstyle:LineLength")
        @Test
        @DisplayName("Should throw propagate the CaseTypeValidationException thrown by the LegacyCaseTypeValidator")
        void shouldThrowCaseTypeValidationExceptionWithoutSaving_whenLegacyCaseTypeValidatorThrowsCaseTypeValidationException() {

            CaseTypeValidationException caseTypeValidationException = new CaseTypeValidationException(
                new CaseTypeValidationResult());
            doThrow(caseTypeValidationException)
                .when(legacyCaseTypeValidator).validateCaseType(
                argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity2, jurisdiction)));

            CaseTypeValidationException actualCaseTypeValidationException = assertThrows(
                CaseTypeValidationException.class, () -> classUnderTest.createAll(
                    jurisdiction, caseTypeEntities, new HashSet<>()));

            assertTrue(actualCaseTypeValidationException == caseTypeValidationException);

            assertComponentsCalled(false, caseTypeEntity2);

        }

        @Test
        @DisplayName("Should throw a ValidationException if any of the Case Type IDs do not match their definitive "
            + "spellings")
        void shouldThrowValidationExceptionIfCaseTypeIdDoesNotMatchDefinitiveSpelling() {
            final String caseTypeReference = "TESTAddressBookCase2";
            final CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setReference(caseTypeReference);
            when(caseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE_2))
                .thenReturn(Optional.of(caseTypeEntity));
            final CaseType caseType = new CaseType();
            caseType.setId(caseTypeReference);
            when(dtoMapper.map(same(caseTypeEntity))).thenReturn(caseType);

            ValidationException validationException
                = assertThrows(ValidationException.class, () -> classUnderTest.createAll(
                    jurisdiction, caseTypeEntities, new HashSet<>()));
            verify(caseTypeRepository).findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE_2);

            ValidationResult validationResult = validationException.getValidationResult();
            assertFalse(validationResult.isValid());
            assertEquals(1, validationResult.getValidationErrors().size());

            assertThat(validationResult.getValidationErrors(), hasItem(
                matchesValidationErrorWithDefaultMessage("Current spelling of this Case Type ID is "
                    + "'TESTAddressBookCase2' but the imported Case Type ID was 'TestAddressBookCase2'.")));

            assertComponentsCalled(false, null);
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

        private <T> Matcher<T> matchesCaseTypeEntityWithJurisdictionAdded(CaseTypeEntity caseTypeEntity1,
                                                                          JurisdictionEntity jurisdiction) {

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
                inOrder.verify(legacyCaseTypeValidator).validateCaseType(
                    argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity, jurisdiction)));
                if (caseTypeWithLegacyValidationException != null
                    && caseTypeWithLegacyValidationException == caseTypeEntity) {
                    return;
                }
                inOrder.verify(caseTypeEntityValidator1).validate(
                    argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity, jurisdiction)));
                inOrder.verify(caseTypeEntityValidator2).validate(
                    argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity, jurisdiction)));
                inOrder.verify(caseTypeRepository).findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(
                    caseTypeEntity.getReference());
                inOrder.verify(caseTypeRepository).caseTypeExistsInAnyJurisdiction(
                    caseTypeEntity.getReference(), jurisdiction.getReference());
            }

            if (shouldSave) {
                inOrder.verify(caseTypeRepository).saveAll(captor.capture());
                Collection<CaseTypeEntity> savedCaseTypeEntities = captor.getValue();
                assertEquals(caseTypeEntities.size(), savedCaseTypeEntities.size());

                for (CaseTypeEntity caseTypeEntity : caseTypeEntities) {
                    assertThat(savedCaseTypeEntities, hasItem(
                        matchesCaseTypeEntityWithJurisdictionAndVersionAdded(caseTypeEntity, jurisdiction,
                            DEFAULT_VERSION + 1)));
                }

            }

            inOrder.verifyNoMoreInteractions();

        }

    }

    @Nested
    class FindByJurisdictionIdTests {

        @SuppressWarnings("checkstyle:LineLength")
        @Test
        @DisplayName("Should map each CaseTypeEntity to a CaseType and return a list of mapped CaseTypes")
        void shouldOrderEventsBeforeCallingMapperForEachCaseTypeAndReturnListOfMappedCaseTypes_whenRepositoryReturnsCaseTypeEntitiesWithDisorderedEvents() {

            EventEntity eventEntity1 = eventEntity(1);
            EventEntity eventEntity2 = eventEntity(2);
            EventEntity eventEntity3 = eventEntity(3);
            EventEntity eventEntity4 = eventEntity(4);

            CaseTypeEntity caseTypeEntity1 = caseTypeEntity(eventEntity2, eventEntity1, eventEntity4, eventEntity3);
            CaseTypeEntity caseTypeEntity2 = caseTypeEntity(eventEntity3, eventEntity4, eventEntity2, eventEntity1);

            CaseField metadataField = new CaseField();
            metadataField.setFieldType(new FieldType());

            CaseType caseType1 = new CaseType();
            CaseType caseType2 = new CaseType();
            when(caseTypeRepository.findByJurisdictionId(any())).thenReturn(
                Arrays.asList(caseTypeEntity1, caseTypeEntity2)
            );
            when(dtoMapper.map(same(caseTypeEntity1))).thenReturn(caseType1);
            when(dtoMapper.map(same(caseTypeEntity2))).thenReturn(caseType2);
            when(metadataFieldService.getCaseMetadataFields()).thenReturn(singletonList(metadataField));

            ArgumentCaptor<CaseTypeEntity> caseTypeCaptor = ArgumentCaptor.forClass(CaseTypeEntity.class);

            String jurisdiction = "Jurisdiction";
            List<CaseType> caseTypes = classUnderTest.findByJurisdictionId(jurisdiction);

            verify(caseTypeRepository).findByJurisdictionId(same(jurisdiction));
            verify(dtoMapper, times(2)).map(caseTypeCaptor.capture());

            assertEquals(2, caseTypes.size());
            assertThat(caseTypes, allOf(
                hasItem(caseType1),
                hasItem(caseType2)
                )
            );
            assertThat(caseTypes.get(0).getCaseFields(), hasItem(metadataField));
            assertThat(caseTypes.get(1).getCaseFields(), hasItem(metadataField));

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

        }

        @Test
        @DisplayName("Should return an empty list when the repository returns an empty list")
        void shouldReturnEmptyList_whenRepositoryReturnsEmptyList() {
            String jurisdiction = "Jurisdiction";

            when(caseTypeRepository.findByJurisdictionId(any())).thenReturn(
                Collections.emptyList()
            );

            List<CaseType> caseTypes = classUnderTest.findByJurisdictionId(jurisdiction);

            verify(caseTypeRepository).findByJurisdictionId(same(jurisdiction));
            verifyZeroInteractions(dtoMapper);

            assertTrue(caseTypes.isEmpty());
        }

        @Test
        @DisplayName("Should return an empty list when the repository returns null")
        void shouldReturnEmptyList_whenRepositoryReturnsNull() {
            String jurisdiction = "Jurisdiction";

            when(caseTypeRepository.findByJurisdictionId(any())).thenReturn(
                null
            );

            List<CaseType> caseTypes = classUnderTest.findByJurisdictionId(jurisdiction);

            verify(caseTypeRepository).findByJurisdictionId(same(jurisdiction));
            verifyZeroInteractions(dtoMapper);

            assertTrue(caseTypes.isEmpty());
        }

        private CaseTypeEntity caseTypeEntity(EventEntity... eventEntities) {
            CaseTypeEntity caseType = new CaseTypeEntity();
            caseType.addEvents(Arrays.asList(eventEntities));
            return caseType;
        }

        private EventEntity eventEntity(int order) {
            EventEntity eventEntity = new EventEntity();
            eventEntity.setOrder(order);
            return eventEntity;
        }

    }

    @Nested
    class CaseTypeExists {

        private static final String CASE_TYPE_REFERENCE = "TestAddressBookCase";
        private static final String JURISDICTION_REFERENCE = "TEST";

        @Test
        @DisplayName("Should return true if case type for jurisdiction other than given exists")
        void shouldReturnTrue_whenCaseTypeForJurisdictionOtherThanGivenExist() {
            when(caseTypeRepository.caseTypeExistsInAnyJurisdiction(CASE_TYPE_REFERENCE, JURISDICTION_REFERENCE))
                .thenReturn(2);

            boolean caseTypeExists = classUnderTest.caseTypeExistsInAnyJurisdiction(
                CASE_TYPE_REFERENCE, JURISDICTION_REFERENCE);

            assertTrue(caseTypeExists);
            verify(caseTypeRepository).caseTypeExistsInAnyJurisdiction(CASE_TYPE_REFERENCE, JURISDICTION_REFERENCE);
        }

        @Test
        @DisplayName("Should return false if case type for jurisdiction other than given does not exist")
        void shouldReturnFalse_whenCaseTypeForJurisdictionOtherThanGivenDoesNotExist() {
            when(caseTypeRepository.caseTypeExistsInAnyJurisdiction(CASE_TYPE_REFERENCE, JURISDICTION_REFERENCE))
                .thenReturn(0);
            Boolean caseTypeExists = classUnderTest.caseTypeExistsInAnyJurisdiction(
                CASE_TYPE_REFERENCE, JURISDICTION_REFERENCE);

            assertFalse(caseTypeExists);
            verify(caseTypeRepository).caseTypeExistsInAnyJurisdiction(CASE_TYPE_REFERENCE, JURISDICTION_REFERENCE);
        }
    }

    @Nested
    class FindByCaseTypeIdTests {

        private static final String caseTypeId = "caseTypeID";
        private final CaseType caseType = new CaseType();
        private final CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        private final CaseField metadataField = new CaseField();

        @BeforeEach
        void setup() {
            metadataField.setId(MetadataField.STATE.getReference());
            FieldType fieldType = new FieldType();
            fieldType.setType(BASE_FIXED_LIST);
            metadataField.setFieldType(fieldType);

            when(caseTypeRepository.findCurrentVersionForReference(caseTypeId)).thenReturn(Optional.of(caseTypeEntity));
            when(dtoMapper.map(caseTypeEntity)).thenReturn(caseType);
            when(metadataFieldService.getCaseMetadataFields()).thenReturn(singletonList(metadataField));
        }

        @Test
        @DisplayName("Should call the mapper with the value returned from the repository and return the mapped value")
        void shouldCallMapperAndReturnResult_whenRepositoryReturnsAnEntity() {
            Optional<CaseType> caseType = classUnderTest.findByCaseTypeId(caseTypeId);

            verify(caseTypeRepository).findCurrentVersionForReference(same(caseTypeId));
            verify(dtoMapper).map(same(caseTypeEntity));
            assertTrue(caseType.isPresent());
            assertThat(caseType.get(), is(this.caseType));
        }

        @Test
        @DisplayName("Should return an empty Optional when the repository returns an empty Optional")
        void shouldThrowNotFoundException_whenRepositoryReturnsEmptyOptional() {
            String caseTypeId = "caseTypeID";

            when(caseTypeRepository.findCurrentVersionForReference(any())).thenReturn(
                Optional.empty()
            );

            Optional<CaseType> caseType = classUnderTest.findByCaseTypeId(caseTypeId);

            assertFalse(caseType.isPresent());
        }

        @Test
        @DisplayName("Should return case type with metadata fields")
        void shouldReturnCaseTypeWithMetadataFieldsAndFixedListItems_whenMetadataFieldIsOfTypeFixedList() {
            Optional<CaseType> response = classUnderTest.findByCaseTypeId(caseTypeId);

            assertTrue(response.isPresent());
            CaseType result = response.get();
            verifyResult(result);
            assertThat(result.getCaseFields().get(0), is(metadataField));
        }

        private void verifyResult(CaseType result) {
            assertThat(result.getCaseFields(), hasSize(1));
            assertThat(result.getCaseFields().get(0).getId(), is(MetadataField.STATE.getReference()));
            verify(caseTypeRepository).findCurrentVersionForReference(same(caseTypeId));
            verify(dtoMapper).map(same(caseTypeEntity));
            verify(metadataFieldService).getCaseMetadataFields();
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
            assertTrue(info.isPresent());
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

    @Nested
    @DisplayName("Find definitive Case Type ID (reference)")
    class FindDefinitiveCaseTypeId {

        private static final String CASE_TYPE_REFERENCE = "testaddressbookcase1";
        private final CaseType caseType = new CaseType();

        @Test
        @DisplayName("Definitive Case Type ID found")
        void definitiveCaseTypeIdExists() {
            when(caseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE))
                .thenReturn(Optional.of(caseTypeEntity1));
            caseType.setId(CASE_TYPE_REFERENCE_1);
            when(dtoMapper.map(same(caseTypeEntity1))).thenReturn(caseType);
            String caseTypeId = classUnderTest.findDefinitiveCaseTypeId(CASE_TYPE_REFERENCE);
            verify(caseTypeRepository).findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(
                same(CASE_TYPE_REFERENCE));
            verify(dtoMapper).map(same(caseTypeEntity1));
            assertThat(caseTypeId, is(CASE_TYPE_REFERENCE_1));
        }

        @Test
        @DisplayName("No Definitive Case Type ID found")
        void definitiveCaseTypeIdDoesNotExist() {
            when(caseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE))
                .thenReturn(Optional.empty());
            String caseTypeId = classUnderTest.findDefinitiveCaseTypeId(CASE_TYPE_REFERENCE);
            assertNull(caseTypeId);
        }
    }
}
