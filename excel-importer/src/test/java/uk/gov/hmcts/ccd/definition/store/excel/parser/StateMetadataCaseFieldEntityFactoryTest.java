package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;
import static uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField.STATE;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_FIXED_LIST;

class StateMetadataCaseFieldEntityFactoryTest {

    private StateMetadataCaseFieldEntityFactory factory;

    @Mock
    private VersionedDefinitionRepositoryDecorator<FieldTypeEntity, Integer> repository;

    @Mock
    private ParseContext parseContext;

    @BeforeEach
    void setUp() {
        initMocks(this);
        factory = new StateMetadataCaseFieldEntityFactory(mock(FieldTypeRepository.class));
        setInternalState(factory, "versionedRepository", repository);
    }

    @Test
    @DisplayName("should create and return case field entity")
    void shouldCreateAndReturnCaseFieldEntity() {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);
        FieldTypeEntity fixedListBaseType = new FieldTypeEntity();
        FieldTypeEntity stateFixedListType = new FieldTypeEntity();

        when(parseContext.getBaseType(BASE_FIXED_LIST)).thenReturn(Optional.of(fixedListBaseType));
        when(repository.save(any(FieldTypeEntity.class))).thenReturn(stateFixedListType);

        CaseFieldEntity caseField = factory.createCaseFieldEntity(parseContext, caseType);

        assertAll(() -> assertThat(caseField, notNullValue()),
                  () -> assertThat(caseField.getReference(), is(STATE.getReference())),
                  () -> assertThat(caseField.getFieldType(), is(stateFixedListType)),
                  () -> assertThat(caseField.getSecurityClassification(), is(SecurityClassification.PUBLIC)),
                  () -> assertThat(caseField.getLabel(), is(STATE.getLabel())),
                  () -> assertThat(caseField.getHidden(), is(false)),
                  () -> assertThat(caseField.getLiveFrom(), is(LocalDate.now())),
                  () -> assertThat(caseField.getDataFieldType(), is(DataFieldType.METADATA)));
    }

    @Test
    @DisplayName("should create and save fixed list for states")
    void shouldCreateAndSaveFixedListForStates() {
        CaseTypeEntity caseType = new CaseTypeEntity();
        FieldTypeEntity fixedListBaseType = new FieldTypeEntity();
        FieldTypeEntity stateFixedListType = new FieldTypeEntity();
        stateFixedListType.setReference("stateFixedList");

        when(parseContext.getBaseType(BASE_FIXED_LIST)).thenReturn(Optional.of(fixedListBaseType));
        when(repository.save(any(FieldTypeEntity.class))).thenReturn(stateFixedListType);

        CaseFieldEntity caseField = factory.createCaseFieldEntity(parseContext, caseType);

        assertAll(() -> assertThat(caseField, notNullValue()),
                  () -> assertThat(caseField.getFieldType(), equalTo(stateFixedListType)),
                  () -> verify(parseContext).getBaseType(BASE_FIXED_LIST),
                  () -> verify(repository).save(any(FieldTypeEntity.class)));
    }

    @Test
    @DisplayName("should throw exception when base fixed list not found in parser context")
    void shouldThrowException() {
        CaseTypeEntity caseType = new CaseTypeEntity();

        when(parseContext.getBaseType(BASE_FIXED_LIST)).thenReturn(Optional.empty());

        assertThrows(SpreadsheetParsingException.class, () -> factory.createCaseFieldEntity(parseContext, caseType));
    }
}
