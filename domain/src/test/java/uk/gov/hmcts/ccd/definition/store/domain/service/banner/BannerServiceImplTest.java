package uk.gov.hmcts.ccd.definition.store.domain.service.banner;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.BannerRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BannerServiceImplTest {

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    private BannerServiceImpl classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new BannerServiceImpl(bannerRepository, dtoMapper);
    }

    @Test
    @DisplayName(
        "Should get Banners for the passed jurisdictions")
    void shouldGetBannersForValidJurisdictions() {
        List<BannerEntity> banners = Lists.newArrayList();
        banners.add(createBanner("Test Desc1", true));
        banners.add(createBanner("Test Desc2", false));
        doReturn(banners).when(bannerRepository).findAllByReference(anyList());
        List<String> references = Lists.newArrayList("Test", "Divorce");
        List<Banner> bannersReturned = classUnderTest.getAll(references);
        Assert.assertEquals(2, bannersReturned.size());
    }

    @Test
    @DisplayName(
        "Should get empty Banners list for the passed empty jurisdictions")
    void shouldGetEmptyBannersListForEmptyJurisdictions() {
        List<Banner> banners = Lists.newArrayList();
        doReturn(banners).when(bannerRepository).findAllByReference(anyList());
        List<Banner> bannersReturned = classUnderTest.getAll(Lists.newArrayList());
        Assert.assertEquals(0, bannersReturned.size());
    }

    @Test
    @DisplayName(
        "Should get empty Banners list for the passed empty jurisdictions")
    void shouldSaveBannerEntity() {
        BannerEntity bannerEntity = mock(BannerEntity.class);
        JurisdictionEntity jurisdiction = mock(JurisdictionEntity.class);
        doReturn(jurisdiction).when(bannerEntity).getJurisdiction();
        doReturn("PROBATE").when(jurisdiction).getReference();
        doReturn(null).when(bannerRepository).findByJurisdictionId(anyString());
        classUnderTest.save(bannerEntity);
        verify(bannerRepository, times(1)).save(eq(bannerEntity));
    }

    @Test
    @DisplayName(
        "Should get empty Banners list for the passed empty jurisdictions")
    void shouldSaveCopyBannerEntity() {
        BannerEntity bannerEntity = mock(BannerEntity.class);
        JurisdictionEntity jurisdiction = mock(JurisdictionEntity.class);
        doReturn(jurisdiction).when(bannerEntity).getJurisdiction();
        doReturn("PROBATE").when(jurisdiction).getReference();
        BannerEntity bannerEntityDB = mock(BannerEntity.class);
        doReturn(bannerEntityDB).when(bannerRepository).findByJurisdictionId(anyString());
        classUnderTest.save(bannerEntity);
        verify(bannerEntityDB, times(1)).copy(eq(bannerEntity));
        verify(bannerRepository, times(1)).save(eq(bannerEntityDB));
    }

    @Test
    void shouldAttemptToDeleteBannersWhenJurisdictionReferenceIsProvided() {
        String reference = "PROBATE";

        classUnderTest.deleteJurisdictionBanners(reference);

        verify(bannerRepository, times(1)).deleteByJurisdictionReference(eq(reference));
    }

    @Test
    void shouldNotAttemptToDeleteBannersWhenNoJurisdictionReferenceIsProvided() {
        classUnderTest.deleteJurisdictionBanners(null);
        classUnderTest.deleteJurisdictionBanners("");

        verifyNoMoreInteractions(bannerRepository);
    }

    private BannerEntity createBanner(String description, boolean enabled) {
        BannerEntity banner = new BannerEntity();
        banner.setBannerDescription(description);
        banner.setBannerEnabled(enabled);
        banner.setBannerUrl("http://localhost:3451/");
        banner.setBannerUrlText("Click here to see it.>>>");
        return banner;
    }


}
