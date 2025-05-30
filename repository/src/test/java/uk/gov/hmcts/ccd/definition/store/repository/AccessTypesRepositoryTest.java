package uk.gov.hmcts.ccd.definition.store.repository;

import org.hamcrest.core.Is;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class AccessTypesRepositoryTest {

    @Autowired
    private AccessTypesRepository accessTypesRepository;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestHelper testHelper;

    @Test
    void saveAndRetrieveAccessTypeRoles() {

        final CaseTypeEntity caseType = createCaseTypeEntity();
        saveCaseType(caseType);

        final AccessTypeEntity accessTypes = createAccessTypeEntity(caseType);
        saveAccessTypeRoles(accessTypes);

        List<AccessTypeEntity> accessTypesList = accessTypesRepository.findAllWithCaseTypeIds();
        assertThat(accessTypesList.size(), is(1));

        AccessTypeEntity result = accessTypesList.get(0);
        assertThat(result.getId(), Is.is(1));
        assertThat(result.getLiveFrom(), Is.is(LocalDate.of(2023, Month.FEBRUARY, 12)));
        assertThat(result.getLiveTo(), Is.is(LocalDate.of(2027, Month.OCTOBER, 17)));
        assertThat(result.getCaseType().getId(), Is.is(caseType.getId()));
        assertThat(result.getAccessTypeId(), Is.is("some access type id"));
        assertThat(result.getOrganisationProfileId(), Is.is("some org profile id"));
        assertThat(result.getAccessMandatory(), Is.is(true));

    }

    @NotNull
    private CaseTypeEntity createCaseTypeEntity() {
        final JurisdictionEntity testJurisdiction = testHelper.createJurisdiction();

        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("id");
        caseType.setName("Test case");
        caseType.setVersion(1);
        caseType.setDescription("Some case type");
        caseType.setJurisdiction(testJurisdiction);
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);
        return caseType;
    }

    @NotNull
    private AccessTypeEntity createAccessTypeEntity(CaseTypeEntity caseType) {
        final AccessTypeEntity accessTypes = new AccessTypeEntity();
        accessTypes.setLiveFrom(LocalDate.of(2023, Month.FEBRUARY, 12));
        accessTypes.setLiveTo(LocalDate.of(2027, Month.OCTOBER, 17));
        accessTypes.setCaseType(toCaseTypeLiteEntity(caseType));
        accessTypes.setAccessTypeId("some access type id");
        accessTypes.setOrganisationProfileId("some org profile id");
        accessTypes.setAccessMandatory(true);
        accessTypes.setAccessDefault(true);
        accessTypes.setDisplay(true);
        accessTypes.setDescription("some description");
        accessTypes.setHint("some hint");
        accessTypes.setDisplayOrder(1);
        return accessTypes;
    }

    private void saveCaseType(CaseTypeEntity caseType) {
        caseTypeRepository.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }

    private void saveAccessTypeRoles(AccessTypeEntity accessTypes) {
        accessTypesRepository.save(accessTypes);
        entityManager.flush();
        entityManager.clear();
    }
}
