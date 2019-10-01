package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.repository.am.AMCaseTypeACLRepository;
import uk.gov.hmcts.ccd.definition.store.repository.am.SwitchableCaseTypeRepository;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class SwitchableCaseTypeRepositoryTest {

    private static final String CASE_TYPE_REFERENCE = "Test reference";
    private static final String EXCLUDED_JURISDICTION_REFERENCE = "EXCLUDED JURISDICTION";
    private static final Integer COUNT = 5;

    @Mock
    private CCDCaseTypeRepository ccdCaseTypeRepository;
    @Mock
    private AMCaseTypeACLRepository amCaseTypeACLRepository;
    @Mock
    private AppConfigBasedAmPersistenceSwitch amPersistenceSwitch;

    private SwitchableCaseTypeRepository switchableCaseTypeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        switchableCaseTypeRepository = new SwitchableCaseTypeRepository(ccdCaseTypeRepository, amCaseTypeACLRepository, amPersistenceSwitch);
    }

    @Test
    void caseTypeExistsInAnyJurisdiction() {
        when(ccdCaseTypeRepository.caseTypeExistsInAnyJurisdiction(CASE_TYPE_REFERENCE, EXCLUDED_JURISDICTION_REFERENCE)).thenReturn(COUNT);

        Integer result = switchableCaseTypeRepository.caseTypeExistsInAnyJurisdiction(CASE_TYPE_REFERENCE, EXCLUDED_JURISDICTION_REFERENCE);

        Assertions.assertAll(
            () -> assertThat(result, is(COUNT)),
            () -> verify(ccdCaseTypeRepository).caseTypeExistsInAnyJurisdiction(CASE_TYPE_REFERENCE, EXCLUDED_JURISDICTION_REFERENCE),
            () -> verifyZeroInteractions(amCaseTypeACLRepository)
        );
    }

    @Test
    void findByJurisdictionId() {

    }

    @Test
    void findCurrentVersionForReference() {
    }

    @Test
    void findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc() {
    }

    @Test
    void save() {
    }

    @Test
    void saveAll() {
    }
}
