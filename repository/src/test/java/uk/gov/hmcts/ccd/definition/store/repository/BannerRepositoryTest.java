package uk.gov.hmcts.ccd.definition.store.repository;


import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.List;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class BannerRepositoryTest {

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    private static final String JURISDICTION_REFERENCE_1 = "PROBATE";
    private static final String JURISDICTION_REFERENCE_2 = "DIVORCE";

    @BeforeEach
    void setUp() {
        JurisdictionEntity testJurisdiction1 = testHelper.createJurisdiction(JURISDICTION_REFERENCE_1, "", "");
        JurisdictionEntity testJurisdiction2 = testHelper.createJurisdiction(JURISDICTION_REFERENCE_2, "", "");
        BannerEntity bannerEntity1 = createBanner(testJurisdiction1);
        BannerEntity bannerEntity2 = createBanner(testJurisdiction1);
        BannerEntity bannerEntity3 = createBanner(testJurisdiction2);
        saveBannersAndFlushSession(bannerEntity1, bannerEntity2, bannerEntity3);
    }

    @Test
    void shouldDeleteBannersForProvidedReference() {
        int deletedCount = bannerRepository.deleteByJurisdictionReference(JURISDICTION_REFERENCE_1);

        List<BannerEntity> banners = bannerRepository.findAll();
        assertAll(
            () -> assertThat(deletedCount, is(2)),
            () -> assertThat(banners.size(), is(1)),
            () -> assertThat(banners.get(0).getJurisdiction().getReference(), is(not(JURISDICTION_REFERENCE_1)))
        );
    }

    @Test
    void shouldNotDeleteAnyBannersWhenReferenceDoesNotExist() {
        int deletedCount = bannerRepository.deleteByJurisdictionReference("UNKNOWN_REFERENCE");

        List<BannerEntity> banners = bannerRepository.findAll();
        assertAll(
            () -> assertThat(deletedCount, is(0)),
            () -> assertThat(banners.size(), is(3))
        );
    }

    private BannerEntity createBanner(JurisdictionEntity jurisdiction) {
        BannerEntity bannerEntity = new BannerEntity();
        bannerEntity.setJurisdiction(jurisdiction);
        bannerEntity.setBannerEnabled(true);
        bannerEntity.setBannerUrl("URL");
        bannerEntity.setBannerUrlText("URL Text");
        bannerEntity.setBannerDescription("Banner Description");
        return bannerEntity;
    }

    private void saveBannersAndFlushSession(BannerEntity... bannerEntities) {
        for (BannerEntity bannerEntity : bannerEntities) {
            bannerRepository.save(bannerEntity);
        }
        entityManager.flush();
        entityManager.clear();
    }
}
