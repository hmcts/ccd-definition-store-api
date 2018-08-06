package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeLite;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.repository.model.StateLite;

@Mapper(componentModel = "spring")
public interface CaseTypeEntityToCaseTypeLiteDTOMapper {

    @Mapping(source = "caseTypeEntity.reference", target = "id")
    CaseTypeLite map(CaseTypeEntity caseTypeEntity);

    @Mapping(source = "stateEntity.reference", target = "id")
    StateLite map(StateEntity stateEntity);

    @Mapping(source = "jurisdictionEntity.reference", target = "id")
    @Mapping(source = "jurisdictionEntity.liveTo", target = "liveUntil")
    Jurisdiction map(JurisdictionEntity jurisdictionEntity);
}
