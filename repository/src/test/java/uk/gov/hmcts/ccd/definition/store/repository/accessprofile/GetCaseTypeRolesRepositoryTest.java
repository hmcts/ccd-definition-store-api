package uk.gov.hmcts.ccd.definition.store.repository.accessprofile;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SanityCheckApplication;
import uk.gov.hmcts.ccd.definition.store.repository.TestConfiguration;
import uk.gov.hmcts.ccd.definition.store.repository.TestHelper;
import uk.gov.hmcts.ccd.definition.store.repository.UserRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class GetCaseTypeRolesRepositoryTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GetCaseTypeRolesRepository getCaseTypeRolesRepository;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private TestHelper testHelper;

    private JurisdictionEntity testJurisdictionWithCaseTypeACL;


    @BeforeEach
    void setUp() {
        this.testJurisdictionWithCaseTypeACL = testHelper.createJurisdiction(
            "jurisdictionWithCaseTypeACL", "nameWithCaseTypeACL", "descWithCaseTypeACL");
        createCaseTypeEntityWithCaseTypeACL(
            "CaseTypeWithACL", "CaseTypeWithACL", 1,
            testJurisdictionWithCaseTypeACL, testHelper.createCaseTypeACL());
    }

    @Test
    void shouldFindCaseTypeRoles() {
        Optional<CaseTypeEntity> caseTypeEntity = caseTypeRepository
            .findCurrentVersionForReference("CaseTypeWithACL");

        Set<String> caseTypeRoles = getCaseTypeRolesRepository.findCaseTypeRoles(caseTypeEntity.get().getId());
        assertEquals(4, caseTypeRoles.size());
    }

    private CaseTypeEntity createCaseTypeEntityWithCaseTypeACL(String reference,
                                                               String name,
                                                               Integer version,
                                                               JurisdictionEntity jurisdiction,
                                                               Collection<CaseTypeACLEntity> caseTypeACLList) {
        CaseTypeEntity caseTypeEntity = testHelper
            .createCaseTypeEntityWithCaseTypeACL(reference, name, version, jurisdiction, caseTypeACLList);
        saveCaseTypeClearAndFlushSession(caseTypeEntity);
        return caseTypeEntity;
    }

    private void saveCaseTypeClearAndFlushSession(CaseTypeEntity caseType) {
        caseTypeRepository.save(caseType);
        entityManager.flush();
        entityManager.clear();
    }
}
