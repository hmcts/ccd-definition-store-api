package uk.gov.hmcts.ccd.definition.store.domain.service.banner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.BannerRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;

@Component
public class BannerServiceImpl implements BannerService {

    private static final Logger LOG = LoggerFactory.getLogger(BannerServiceImpl.class);

    private final BannerRepository bannerRepository;

    private final EntityToResponseDTOMapper dtoMapper;

    @Autowired
    public BannerServiceImpl(BannerRepository bannerRepository,
                             EntityToResponseDTOMapper dtoMapper) {
        this.bannerRepository = bannerRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public void save(BannerEntity bannerEntity) {
        LOG.debug("Create Banner Entity {}", bannerEntity);
        String reference = bannerEntity.getJurisdiction().getReference();
        Optional<BannerEntity> bannerEntityObj = Optional.ofNullable(bannerRepository.findByJurisdictionId(reference));
        BannerEntity bannerEntityDB = bannerEntity;
        if (bannerEntityObj.isPresent()) {
            bannerEntityDB = bannerEntityObj.get();
            bannerEntityDB.copy(bannerEntity);
        }
        this.bannerRepository.save(bannerEntityDB);
    }

    @Override
    public List<Banner> getAll(List<String> references) {
        List<BannerEntity> bannerEntities = bannerRepository.findAllByReference(references);
        return bannerEntities.stream()
            .map(dtoMapper::map)
            .collect(toList());
    }

    @Override
    public void deleteJurisdictionBanners(String jurisdictionReference) {
        if (isNullOrEmpty(jurisdictionReference)) {
            return;
        }
        int deletedBannersCount = bannerRepository.deleteByJurisdictionReference(jurisdictionReference);
        LOG.debug(
            "Deleted {} existing banner entities for jurisdiction {}.", deletedBannersCount, jurisdictionReference);
    }
}
