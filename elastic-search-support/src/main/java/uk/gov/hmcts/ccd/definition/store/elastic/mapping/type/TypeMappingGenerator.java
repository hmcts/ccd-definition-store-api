package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

public interface TypeMappingGenerator {

    String dataMapping(FieldEntity field);

    String dataClassificationMapping(FieldEntity field);

    List<String> getCcdTypes();
}
