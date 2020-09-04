package uk.gov.hmcts.ccd.definition.store.domain.service.nocconfig;

import java.util.List;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NoCConfigEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;

public interface NoCConfigService {

    void save(NoCConfigEntity noCConfigEntity);

    List<NoCConfigEntity> getAll(List<String> caseTypeReferences);

    void deleteCaseTypeNocConfig(String caseTypeReference);
}
