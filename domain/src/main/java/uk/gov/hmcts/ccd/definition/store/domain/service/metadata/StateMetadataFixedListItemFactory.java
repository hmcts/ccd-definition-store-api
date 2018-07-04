package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ccd.definition.store.domain.service.metadata.StateMetadataFixedListItemFactory.QUALIFIER;

@Component
@Qualifier(QUALIFIER)
public class StateMetadataFixedListItemFactory implements MetadataFixedListItemFactory {

    public static final String QUALIFIER = "StateMetadataFixedListItemFactory";

    @Override
    public List<FixedListItem> createFixedListItems(CaseType caseType) {
        return caseType.getStates()
            .stream()
            .map(state -> new FixedListItem(state.getId(), state.getName()))
            .collect(Collectors.toList());
    }
}
