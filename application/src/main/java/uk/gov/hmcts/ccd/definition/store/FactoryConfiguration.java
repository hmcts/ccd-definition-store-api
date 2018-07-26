package uk.gov.hmcts.ccd.definition.store;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.excel.parser.MetadataCaseFieldEntityFactory;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.excel.parser.StateMetadataCaseFieldEntityFactory.QUALIFIER;

@Configuration
public class FactoryConfiguration {

    @Bean
    public Map<MetadataField, MetadataCaseFieldEntityFactory> metadataCaseFieldEntityFactoryRegistry(
        @Qualifier(QUALIFIER) MetadataCaseFieldEntityFactory stateMetadataCaseFieldEntityFactory) {

        Map<MetadataField, MetadataCaseFieldEntityFactory> map = new HashMap<>();
        map.put(MetadataField.STATE, stateMetadataCaseFieldEntityFactory);

        return map;
    }
}
