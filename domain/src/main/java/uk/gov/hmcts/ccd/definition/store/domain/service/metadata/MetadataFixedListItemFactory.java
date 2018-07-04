package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.util.List;

public interface MetadataFixedListItemFactory {

    List<FixedListItem> createFixedListItems(CaseType caseType);

}
