package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import java.io.IOException;
import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

public interface TypeMappingGenerator {

    String generateMapping(FieldEntity fieldEntity) throws IOException;

    List<String> getTypes();
}
