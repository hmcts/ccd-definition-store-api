package uk.gov.hmcts.ccd.definition.store.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceReadSource.FROM_AM;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceReadSource.FROM_CCD;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_AM;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_BOTH;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_CCD;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.google.common.collect.Lists;

import java.util.List;

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

    private List<String> repeatedCaseTypeList = Lists.newArrayList(DIVORCE_CT, FR_CT, TEST_CT);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(ccdOnlyWriteCaseTypes).when(goodApplicationParams).getCaseTypesWithAmWrittenOnlyToCcd();
        doReturn(amOnlyWriteCaseTypes).when(goodApplicationParams).getCaseTypesWithAmWrittenOnlyToAm();
        doReturn(bothWriteCaseTypes).when(goodApplicationParams).getCaseTypesWithAmWrittenToBoth();
        doReturn(ccdOnlyReadCaseTypes).when(goodApplicationParams).getCaseTypesWithAmReadFromCcd();
        doReturn(amOnlyReadCaseTypes).when(goodApplicationParams).getCaseTypesWithAmReadFromAm();
        goodAmPersistenceSwitch = new AppConfigBasedAmPersistenceSwitch(goodApplicationParams);

        doReturn(ccdOnlyWriteCaseTypes).when(duplicateConfigForReadApplicationParams)
                .getCaseTypesWithAmWrittenOnlyToCcd();
        doReturn(amOnlyWriteCaseTypes).when(duplicateConfigForReadApplicationParams)
                .getCaseTypesWithAmWrittenOnlyToAm();
        doReturn(bothWriteCaseTypes).when(duplicateConfigForReadApplicationParams).getCaseTypesWithAmWrittenToBoth();
        doReturn(repeatedCaseTypeList).when(duplicateConfigForReadApplicationParams).getCaseTypesWithAmReadFromCcd();
        doReturn(repeatedCaseTypeList).when(duplicateConfigForReadApplicationParams).getCaseTypesWithAmReadFromAm();

        doReturn(repeatedCaseTypeList).when(duplicateConfigForWriteApplicationParams)
                .getCaseTypesWithAmWrittenOnlyToCcd();
        doReturn(repeatedCaseTypeList).when(duplicateConfigForWriteApplicationParams)
                .getCaseTypesWithAmWrittenOnlyToAm();
        doReturn(repeatedCaseTypeList).when(duplicateConfigForWriteApplicationParams).getCaseTypesWithAmWrittenToBoth();
        doReturn(ccdOnlyReadCaseTypes).when(duplicateConfigForWriteApplicationParams).getCaseTypesWithAmReadFromCcd();
        doReturn(amOnlyReadCaseTypes).when(duplicateConfigForWriteApplicationParams).getCaseTypesWithAmReadFromAm();

        doReturn(repeatedCaseTypeList).when(duplicateConfigForReadAndWriteApplicationParams)
                .getCaseTypesWithAmWrittenOnlyToCcd();
        doReturn(repeatedCaseTypeList).when(duplicateConfigForReadAndWriteApplicationParams)
                .getCaseTypesWithAmWrittenOnlyToAm();
        doReturn(repeatedCaseTypeList).when(duplicateConfigForReadAndWriteApplicationParams)
                .getCaseTypesWithAmWrittenToBoth();
        doReturn(repeatedCaseTypeList).when(duplicateConfigForReadAndWriteApplicationParams)
                .getCaseTypesWithAmReadFromCcd();
        doReturn(repeatedCaseTypeList).when(duplicateConfigForReadAndWriteApplicationParams)
                .getCaseTypesWithAmReadFromAm();
    }

    @Test
    public void shouldSelectCcdAsReadDataSourceIfCaseTypeNotSpecified() {
        assertEquals(FROM_CCD, goodAmPersistenceSwitch.getReadDataSourceFor(UNSPECIFIED));
    }

    @Test
    public void shouldSelectCcdAsWriteDataSourceIfCaseTypeNotSpecified() {
        assertEquals(TO_CCD, goodAmPersistenceSwitch.getWriteDataSourceFor(UNSPECIFIED));
    }

    @Test
    public void shouldSelectCcdAsReadDataSourceForAppropriateCaseTypes() {
        assertEquals(FROM_CCD, goodAmPersistenceSwitch.getReadDataSourceFor(CMC_CT));
        assertEquals(FROM_CCD, goodAmPersistenceSwitch.getReadDataSourceFor(PROBATE_CT));
    }

    @Test
    public void shouldSelectAmAsReadDataSourceForAppropriateCaseTypes() {
        assertEquals(FROM_AM, goodAmPersistenceSwitch.getReadDataSourceFor(DIVORCE_CT));
        assertEquals(FROM_AM, goodAmPersistenceSwitch.getReadDataSourceFor(FR_CT));
        assertEquals(FROM_AM, goodAmPersistenceSwitch.getReadDataSourceFor(TEST_CT));
    }

    @Test
    public void shouldSelectCcdAsWriteDataSourceForAppropriateCaseTypes() {
        assertEquals(TO_CCD, goodAmPersistenceSwitch.getWriteDataSourceFor(DIVORCE_CT));
        assertEquals(TO_CCD, goodAmPersistenceSwitch.getWriteDataSourceFor(CMC_CT));
        assertEquals(TO_CCD, goodAmPersistenceSwitch.getWriteDataSourceFor(TEST_CT));
    }

    @Test
    public void shouldSelectAmAsWriteDataSourceForAppropriateCaseTypes() {
        assertEquals(TO_AM, goodAmPersistenceSwitch.getWriteDataSourceFor(PROBATE_CT));
    }

    @Test
    public void shouldSelectBothAsWriteDataSourceForAppropriateCaseTypes() {
        assertEquals(TO_BOTH, goodAmPersistenceSwitch.getWriteDataSourceFor(FR_CT));
    }

    @Test(expected = IgnoredPropertyException.class)
    public void shouldFailForDuplicateReadConfigurations() {
        new AppConfigBasedAmPersistenceSwitch(
                duplicateConfigForReadApplicationParams);
    }

    @Test(expected = IgnoredPropertyException.class)
    public void shouldFailForDuplicateWriteConfigurations() {
        new AppConfigBasedAmPersistenceSwitch(
                duplicateConfigForWriteApplicationParams);
    }

    @Test(expected = IgnoredPropertyException.class)
    public void shouldFailForDuplicateReadAndWriteConfigurations() {
        new AppConfigBasedAmPersistenceSwitch(
                duplicateConfigForReadAndWriteApplicationParams);
    }

}
