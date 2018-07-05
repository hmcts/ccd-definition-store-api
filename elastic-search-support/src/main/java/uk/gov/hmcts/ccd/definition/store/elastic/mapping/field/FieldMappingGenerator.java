package uk.gov.hmcts.ccd.definition.store.elastic.mapping.field;

import java.io.IOException;
import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

public interface FieldMappingGenerator {

    String dataMapping(FieldEntity fieldEntity) throws IOException;

    String dataClassificationMapping(FieldEntity fieldEntity) throws IOException;

    List<String> getCcdTypes();
}
