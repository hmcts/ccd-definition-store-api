package uk.gov.hmcts.ccd.definition.store.domain.service.banner;

import java.util.List;

import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;

public interface BannerService {

    void save(BannerEntity bannerEntity);

    List<Banner> getAll(List<String> references);

    void deleteJurisdictionBanners(String jurisdictionReference);
}
