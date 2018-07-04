package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_FIXED_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;

class MetadataFieldServiceImplTest {

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @Mock
    private CaseFieldRepository caseFieldRepository;

    @Mock
    private Map<MetadataField, MetadataFixedListItemFactory> metadataFixedListItemFactoryMap;

    private MetadataFieldServiceImpl metadataFieldService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        metadataFieldService = new MetadataFieldServiceImpl(caseFieldRepository, dtoMapper,
                                                            metadataFixedListItemFactoryMap);
    }

    @Test
    @DisplayName("Should return list of metadata fields")
    void shouldReturnListOfMetadataFields() {
        CaseType caseType = new CaseType();
        CaseFieldEntity metadataFieldEntity = new CaseFieldEntity();
        CaseField metadataField = new CaseField();
        metadataField.setId(MetadataField.CASE_REFERENCE.name());
        FieldType fieldType = new FieldType();
        fieldType.setType(BASE_TEXT);
        metadataField.setFieldType(fieldType);

        when(caseFieldRepository.findByDataFieldType(DataFieldType.METADATA)).thenReturn(singletonList
                                                                                             (metadataFieldEntity));
        when(dtoMapper.map(metadataFieldEntity)).thenReturn(metadataField);

        List<CaseField> metadataFields = metadataFieldService.getCaseMetadataFields(caseType);

        assertThat(metadataFields.get(0), is(metadataField));
        verifyZeroInteractions(metadataFixedListItemFactoryMap);
    }

    @Test
    @DisplayName("Should return fixed list metadata fields with fixed list values")
    void shouldReturnFixedListMetadataFieldsWithFixedListValues() {
        CaseType caseType = new CaseType();
        CaseFieldEntity metadataFieldEntity = new CaseFieldEntity();
        CaseField metadataField = new CaseField();
        metadataField.setId(MetadataField.STATE.name());
        FieldType fieldType = new FieldType();
        fieldType.setType(BASE_FIXED_LIST);
        metadataField.setFieldType(fieldType);

        when(caseFieldRepository.findByDataFieldType(DataFieldType.METADATA)).thenReturn(singletonList
                                                                                             (metadataFieldEntity));
        when(dtoMapper.map(metadataFieldEntity)).thenReturn(metadataField);
        MetadataFixedListItemFactory factory = mock(MetadataFixedListItemFactory.class);
        when(metadataFixedListItemFactoryMap.get(MetadataField.STATE)).thenReturn(factory);
        when(factory.createFixedListItems(caseType)).thenReturn(singletonList(new FixedListItem("Code", "Label")));

        List<CaseField> metadataFields = metadataFieldService.getCaseMetadataFields(caseType);

        assertThat(metadataFields.get(0), is(metadataField));
        assertThat(metadataFields.get(0).getFieldType().getFixedListItems(), hasSize(1));
        assertThat(metadataFields.get(0).getFieldType().getFixedListItems().get(0).getCode(), Is.is("Code"));
        assertThat(metadataFields.get(0).getFieldType().getFixedListItems().get(0).getLabel(), Is.is("Label"));
    }

    @Test
    @DisplayName("Should return empty list when no metadata fields are defined")
    void shouldReturnEmptyList() {
        CaseType caseType = new CaseType();

        when(caseFieldRepository.findByDataFieldType(DataFieldType.METADATA)).thenReturn(emptyList());

        List<CaseField> metadataFields = metadataFieldService.getCaseMetadataFields(caseType);

        assertThat(metadataFields.isEmpty(), is(true));
        verifyZeroInteractions(dtoMapper);
        verifyZeroInteractions(metadataFixedListItemFactoryMap);
    }

}
