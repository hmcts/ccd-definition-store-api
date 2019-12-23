package uk.gov.hmcts.ccd.definition.store.domain.service.banner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.BannerRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;

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
    public void create(BannerEntity bannerEntity) {
        LOG.debug("Create Banner Entity {}", bannerEntity);
        this.bannerRepository.save(bannerEntity);
    }

    @Override
    public List<Banner> findByJurisdictionId(String jurisdictionReference) {
        LOG.debug("Find Banner information for the jurisdiction {}", jurisdictionReference);
        Optional<List<BannerEntity>> bannerEntities
            = Optional.ofNullable(bannerRepository.findByJurisdictionId(jurisdictionReference));

        return bannerEntities.orElse(Collections.emptyList())
            .stream()
            .map(dtoMapper::map)
            .collect(toList());
    }

    @Override
    public List<Banner> getAll() {
        List<BannerEntity> bannerEntities = bannerRepository.findAll();
        return bannerEntities.stream()
            .map(dtoMapper::map)
            .collect(toList());
    }

    @Override
    public List<Banner> getAll(List<String> references) {
        List<BannerEntity> bannerEntities = bannerRepository.findAllByReference(references);
        return bannerEntities.stream()
            .map(dtoMapper::map)
            .collect(toList());
    }
}
