package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CaseTypeService {

    void createAll(JurisdictionEntity jurisdiction,
                   Collection<CaseTypeEntity> caseTypes,
                   Set<String> missingAccessprofiles);

    boolean caseTypeExistsInAnyJurisdiction(String reference, String jurisdictionId);

    List<CaseType> findByJurisdictionId(String jurisdictionId);

    Optional<CaseType> findByCaseTypeId(String id);

    List<CaseType> findByCaseTypeIds(List<String> ids);

    Optional<CaseTypeVersionInformation> findVersionInfoByCaseTypeId(String id);

    String findDefinitiveCaseTypeId(String id);

    List<String> findAllCaseTypeIdsByJurisdictionIds(List<String> jurisdictionReferences);

    List<String> findAllCaseTypeIds();
}
