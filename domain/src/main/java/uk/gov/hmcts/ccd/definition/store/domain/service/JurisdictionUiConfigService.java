package uk.gov.hmcts.ccd.definition.store.domain.service;

import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfig;

import java.util.List;

public interface JurisdictionUiConfigService {

    void save(JurisdictionUiConfigEntity jurisdictionUiConfig);

    List<JurisdictionUiConfig> getAll(List<String> references);
}
