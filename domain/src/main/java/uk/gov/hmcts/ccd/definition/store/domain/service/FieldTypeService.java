package uk.gov.hmcts.ccd.definition.store.domain.service;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FieldTypeService {

    List<FieldTypeEntity> getBaseTypes();

    List<FieldTypeEntity> getTypesByJurisdiction(String jurisdictionReference);

    List<FieldTypeEntity> getPredefinedComplexTypes();

    void saveTypes(JurisdictionEntity jurisdiction, Collection<FieldTypeEntity> fieldTypes);

    void save(FieldTypeEntity fieldTypeEntity);

    Optional<FieldTypeEntity> findBaseType(String text);

}
