package uk.gov.hmcts.ccd.definition.store.domain.service.banner;

import java.util.List;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;

public interface BannerService {

    void create(BannerEntity bannerEntity);

    List<Banner> findByJurisdictionId(String jurisdictionReference);

    List<Banner> getAll();

    List<Banner> getAll(List<String> references);
}
