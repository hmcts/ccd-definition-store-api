package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType.METADATA;

@Service
public class MetadataFieldServiceImpl implements MetadataFieldService {

    private final CaseFieldRepository caseFieldRepository;
    private final EntityToResponseDTOMapper dtoMapper;

    @Autowired
    public MetadataFieldServiceImpl(CaseFieldRepository caseFieldRepository,
                                    EntityToResponseDTOMapper dtoMapper) {
        this.caseFieldRepository = caseFieldRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<CaseField> getCaseMetadataFields() {
        // Get metadata fields and convert entity to dto
        return caseFieldRepository.findByDataFieldTypeAndCaseTypeNull(METADATA)
            .stream()
            .map(dtoMapper::map)
            .collect(toList());
    }

}
