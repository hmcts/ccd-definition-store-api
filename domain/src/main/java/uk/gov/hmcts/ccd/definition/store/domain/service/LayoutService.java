package uk.gov.hmcts.ccd.definition.store.domain.service;

import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;

public interface LayoutService {
    void createGenerics(List<GenericLayoutEntity> genericLayouts);

    void createDisplayGroups(List<DisplayGroupEntity> displayGroups);
}
