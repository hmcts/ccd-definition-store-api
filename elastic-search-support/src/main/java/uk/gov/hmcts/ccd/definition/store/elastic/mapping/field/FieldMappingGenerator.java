package uk.gov.hmcts.ccd.definition.store.elastic.mapping.field;

import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

public interface FieldMappingGenerator {

    String dataMapping(FieldEntity fieldEntity);

    String dataClassificationMapping(FieldEntity fieldEntity);

    List<String> getCcdTypes();
}
