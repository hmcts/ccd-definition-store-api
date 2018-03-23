package uk.gov.hmcts.ccd.definition.store.domain.service;

import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import java.util.List;
import java.util.Optional;

public interface JurisdictionService {

    Optional<JurisdictionEntity> get(String reference);

    List<Jurisdiction> getAll();

    List<Jurisdiction> getAll(List<String> references);

    void create(JurisdictionEntity jurisdiction);
}
