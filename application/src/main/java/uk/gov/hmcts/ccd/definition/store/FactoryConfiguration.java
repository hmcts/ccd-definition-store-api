package uk.gov.hmcts.ccd.definition.store;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataFixedListItemFactory;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.domain.service.metadata.StateMetadataFixedListItemFactory.QUALIFIER;

@Configuration
public class FactoryConfiguration {

    @Bean
    public Map<MetadataField, MetadataFixedListItemFactory> metadataFixedListItemFactoryMap(
        @Qualifier(QUALIFIER) MetadataFixedListItemFactory stateMetadataFixedListItemFactory) {
        Map<MetadataField, MetadataFixedListItemFactory> map = new HashMap<>();
        map.put(MetadataField.STATE, stateMetadataFixedListItemFactory);

        return map;
    }
}
