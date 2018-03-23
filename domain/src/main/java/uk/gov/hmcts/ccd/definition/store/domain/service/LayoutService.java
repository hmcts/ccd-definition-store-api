package uk.gov.hmcts.ccd.definition.store.domain.service;

import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.Collection;

public interface LayoutService {
    void createGenerics(Collection<GenericLayoutEntity> genericLayouts);

    void createDisplayGroups(Collection<DisplayGroupEntity> displayGroups);
}
