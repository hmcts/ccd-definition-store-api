package uk.gov.hmcts.ccd.definition.store.domain.service.banner;

import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;

import java.util.List;

public interface BannerService {

    void save(BannerEntity bannerEntity);

    List<Banner> getAll(List<String> references);

    void deleteJurisdictionBanners(String jurisdictionReference);
}
