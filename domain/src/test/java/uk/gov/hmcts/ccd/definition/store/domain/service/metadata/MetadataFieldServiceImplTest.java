package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType.METADATA;

class MetadataFieldServiceImplTest {

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @Mock
    private CaseFieldRepository caseFieldRepository;

    private MetadataFieldServiceImpl metadataFieldService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        metadataFieldService = new MetadataFieldServiceImpl(caseFieldRepository, dtoMapper);
    }

    @Test
    @DisplayName("Should return list of metadata case fields")
    void shouldReturnListOfMetadataFields() {
        CaseType caseType = new CaseType();
        CaseField metadataField = new CaseField();
        metadataField.setId(MetadataField.CASE_REFERENCE.name());
        FieldType fieldType = new FieldType();
        fieldType.setType(BASE_TEXT);
        metadataField.setFieldType(fieldType);

        CaseFieldEntity metadataFieldEntity = new CaseFieldEntity();
        when(caseFieldRepository.findByDataFieldTypeAndCaseTypeNull(METADATA))
            .thenReturn(singletonList(metadataFieldEntity));
        when(dtoMapper.map(metadataFieldEntity)).thenReturn(metadataField);

        List<CaseField> metadataFields = metadataFieldService.getCaseMetadataFields();

        assertThat(metadataFields.get(0), is(metadataField));
        verify(caseFieldRepository).findByDataFieldTypeAndCaseTypeNull(METADATA);
        verify(dtoMapper).map(metadataFieldEntity);
    }

    @Test
    @DisplayName("Should return empty list when no metadata fields are defined")
    void shouldReturnEmptyList() {
        CaseType caseType = new CaseType();

        when(caseFieldRepository.findByDataFieldTypeAndCaseTypeNull(METADATA)).thenReturn(emptyList());

        List<CaseField> metadataFields = metadataFieldService.getCaseMetadataFields();

        assertThat(metadataFields.isEmpty(), is(true));
        verifyZeroInteractions(dtoMapper);
    }

}
