package uk.gov.hmcts.ccd.definition.store.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.InvalidPropertyException;

import com.google.common.collect.Lists;

import java.util.List;

public class AmSwitchValidatorTest {

    @Mock
    private ApplicationParams goodApplicationParams;

    @Mock
    private ApplicationParams duplicateConfigForReadApplicationParams;

    @Mock
    private ApplicationParams duplicateConfigForWriteApplicationParams;

    @Mock
    private ApplicationParams duplicateConfigForReadAndWriteApplicationParams;

    private AmSwitchValidator validator = new AmSwitchValidator();

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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(ccdOnlyWriteCaseTypes).when(goodApplicationParams).getCaseTypesWithAmWrittenOnlyToCcd();
        doReturn(amOnlyWriteCaseTypes).when(goodApplicationParams).getCaseTypesWithAmWrittenOnlyToAm();
        doReturn(bothWriteCaseTypes).when(goodApplicationParams).getCaseTypesWithAmWrittenToBoth();
        doReturn(ccdOnlyReadCaseTypes).when(goodApplicationParams).getCaseTypesWithAmReadFromCcd();
        doReturn(amOnlyReadCaseTypes).when(goodApplicationParams).getCaseTypesWithAmReadFromAm();

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
    @DisplayName("Should successfully validate good configurations")
    public void shouldSuccessfullyValidateGoodConfigurations() {
        validator.validateAmPersistenceSwitchesIn(goodApplicationParams);
    }

    @Test
    @DisplayName("Should fail for duplicate read configurations")
    public void shouldFailForDuplicateReadConfigurations() {
        final InvalidPropertyException invalidPropertyException = assertThrows(InvalidPropertyException.class,
                () -> validator.validateAmPersistenceSwitchesIn(duplicateConfigForReadApplicationParams));
        assertTrue(invalidPropertyException.getMessage()
                .endsWith("Duplicate case type configurations detected for Access Management persistence switches."));
    }

    @Test
    @DisplayName("Should fail for duplicate write configurations")
    public void shouldFailForDuplicateWriteConfigurations() {
        final InvalidPropertyException invalidPropertyException = assertThrows(InvalidPropertyException.class,
                () -> validator.validateAmPersistenceSwitchesIn(duplicateConfigForWriteApplicationParams));
        assertTrue(invalidPropertyException.getMessage()
                .endsWith("Duplicate case type configurations detected for Access Management persistence switches."
                ));
    }

    @Test
    @DisplayName("Should fail for duplicate read and write configurations")
    public void shouldFailForDuplicateReadAndWriteConfigurations() {
        final InvalidPropertyException invalidPropertyException = assertThrows(InvalidPropertyException.class,
                () -> validator.validateAmPersistenceSwitchesIn(duplicateConfigForReadAndWriteApplicationParams));
        assertTrue(invalidPropertyException.getMessage()
                .endsWith("Duplicate case type configurations detected for Access Management persistence switches."));
    }

}
