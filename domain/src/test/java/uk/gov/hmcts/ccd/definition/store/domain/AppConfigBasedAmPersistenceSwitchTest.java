package uk.gov.hmcts.ccd.definition.store.domain;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceReadSource.FROM_AM;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceReadSource.FROM_CCD;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_AM;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_BOTH;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_CCD;

public class AppConfigBasedAmPersistenceSwitchTest {

    @Mock
    private ApplicationParams goodApplicationParams;

    @Mock
    private ApplicationParams duplicateConfigForReadApplicationParams;

    @Mock
    private ApplicationParams duplicateConfigForWriteApplicationParams;

    @Mock
    private ApplicationParams duplicateConfigForReadAndWriteApplicationParams;

    @InjectMocks
    private AppConfigBasedAmPersistenceSwitch goodAmPersistenceSwitch;

    public static final String UNSPECIFIED = "Unspecified";
    private static final String DIVORCE_CT = "DIVORCE";
    private static final String PROBATE_CT = "PROBATE";
    private static final String CMC_CT = "CMC";
    private static final String FR_CT = "FR";
    private static final String TEST_CT = "TEST";

    private List<String> ccdOnlyWriteCaseTypes = Lists.newArrayList(DIVORCE_CT, CMC_CT, TEST_CT);
    private List<String> amOnlyWriteCaseTypes = Lists.newArrayList(PROBATE_CT);
    private List<String> bothWriteCaseTypes = Lists.newArrayList(FR_CT);
    private List<String> ccdOnlyReadCaseTypes = Lists.newArrayList(CMC_CT, PROBATE_CT);
    private List<String> amOnlyReadCaseTypes = Lists.newArrayList(DIVORCE_CT, FR_CT, TEST_CT);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(ccdOnlyWriteCaseTypes).when(goodApplicationParams).getCaseTypesWithAmWrittenOnlyToCcd();
        doReturn(amOnlyWriteCaseTypes).when(goodApplicationParams).getCaseTypesWithAmWrittenOnlyToAm();
        doReturn(bothWriteCaseTypes).when(goodApplicationParams).getCaseTypesWithAmWrittenToBoth();
        doReturn(ccdOnlyReadCaseTypes).when(goodApplicationParams).getCaseTypesWithAmReadFromCcd();
        doReturn(amOnlyReadCaseTypes).when(goodApplicationParams).getCaseTypesWithAmReadFromAm();
        goodAmPersistenceSwitch = new AppConfigBasedAmPersistenceSwitch(goodApplicationParams);
    }

    @Test
    @DisplayName("Should select CCD as read data source if case type is not specified")
    public void shouldSelectCcdAsReadDataSourceIfCaseTypeNotSpecified() {
        assertEquals(FROM_CCD, goodAmPersistenceSwitch.getReadDataSourceFor(UNSPECIFIED));
    }

    @Test
    @DisplayName("Should select CCD as write data source if case type not specified")
    public void shouldSelectCcdAsWriteDataSourceIfCaseTypeNotSpecified() {
        assertEquals(TO_CCD, goodAmPersistenceSwitch.getWriteDataSourceFor(UNSPECIFIED));
    }

    @Test
    @DisplayName("Should select CCD as read data source for appropriate case types")
    public void shouldSelectCcdAsReadDataSourceForAppropriateCaseTypes() {
        assertEquals(FROM_CCD, goodAmPersistenceSwitch.getReadDataSourceFor(CMC_CT));
        assertEquals(FROM_CCD, goodAmPersistenceSwitch.getReadDataSourceFor(PROBATE_CT));
    }

    @Test
    @DisplayName("Should selec AM as read data source for appropriate case types")
    public void shouldSelectAmAsReadDataSourceForAppropriateCaseTypes() {
        assertEquals(FROM_AM, goodAmPersistenceSwitch.getReadDataSourceFor(DIVORCE_CT));
        assertEquals(FROM_AM, goodAmPersistenceSwitch.getReadDataSourceFor(FR_CT));
        assertEquals(FROM_AM, goodAmPersistenceSwitch.getReadDataSourceFor(TEST_CT));
    }

    @Test
    @DisplayName("Should select CCD as write data source for appropriate case types")
    public void shouldSelectCcdAsWriteDataSourceForAppropriateCaseTypes() {
        assertEquals(TO_CCD, goodAmPersistenceSwitch.getWriteDataSourceFor(DIVORCE_CT));
        assertEquals(TO_CCD, goodAmPersistenceSwitch.getWriteDataSourceFor(CMC_CT));
        assertEquals(TO_CCD, goodAmPersistenceSwitch.getWriteDataSourceFor(TEST_CT));
    }

    @Test
    @DisplayName("Should select AM as write data source for appropriate case types")
    public void shouldSelectAmAsWriteDataSourceForAppropriateCaseTypes() {
        assertEquals(TO_AM, goodAmPersistenceSwitch.getWriteDataSourceFor(PROBATE_CT));
    }

    @Test
    @DisplayName("Should select both as write data source for appropriate case types")
    public void shouldSelectBothAsWriteDataSourceForAppropriateCaseTypes() {
        assertEquals(TO_BOTH, goodAmPersistenceSwitch.getWriteDataSourceFor(FR_CT));
    }

}
