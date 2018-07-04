package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_FIXED_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType.METADATA;

@Service
public class MetadataFieldServiceImpl implements MetadataFieldService {

    private final CaseFieldRepository caseFieldRepository;
    private final EntityToResponseDTOMapper dtoMapper;
    private final Map<MetadataField, MetadataFixedListItemFactory> metadataFixedListItemFactoryMap;

    @Autowired
    public MetadataFieldServiceImpl(CaseFieldRepository caseFieldRepository,
                                    EntityToResponseDTOMapper dtoMapper,
                                    Map<MetadataField, MetadataFixedListItemFactory> metadataFixedListItemFactoryMap) {
        this.caseFieldRepository = caseFieldRepository;
        this.dtoMapper = dtoMapper;
        this.metadataFixedListItemFactoryMap = metadataFixedListItemFactoryMap;
    }

    @Override
    public List<CaseField> getCaseMetadataFields(CaseType caseType) {
        // Get metadata fields and convert entity to dto
        List<CaseField> metadataFields = caseFieldRepository.findByDataFieldType(METADATA)
            .stream()
            .map(dtoMapper::map)
            .collect(toList());

        // enrich metadata fixed lists with values
        metadataFields.stream()
            .filter(field -> BASE_FIXED_LIST.equals(field.getFieldType().getType()))
            .forEach(field -> setFixedListItems(field, caseType));

        return metadataFields;
    }

    private void setFixedListItems(CaseField field, CaseType caseType) {
        ofNullable(metadataFixedListItemFactoryMap.get(MetadataField.fromString(field.getId())))
            .ifPresent(factory -> field.getFieldType().setFixedListItems(factory.createFixedListItems(caseType)));
    }
}
