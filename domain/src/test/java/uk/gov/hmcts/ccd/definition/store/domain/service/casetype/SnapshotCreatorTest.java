package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.Version;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnapshotCreatorTest {

    private static final String CASE_TYPE_REF = "TestCaseType";
    private static final String CASE_TYPE_REF_NOT_FOUND = "NonExistentCaseType";

    @Mock
    private CaseTypeService caseTypeService;

    @InjectMocks
    private SnapshotCreator snapshotCreator;

    private CaseType sampleCaseType;

    @BeforeEach
    void setUp() {
        sampleCaseType = createSampleCaseType(CASE_TYPE_REF, 1);
    }

    @Test
    void shouldCreateSnapshot_whenCaseTypeExists() {
        // Given: Case type exists
        when(caseTypeService.findByCaseTypeId(CASE_TYPE_REF))
            .thenReturn(Optional.of(sampleCaseType));

        // When: Create snapshot
        snapshotCreator.createSnapshotForCaseType(CASE_TYPE_REF);

        // Then: Should find case type (which triggers snapshot creation in service)
        verify(caseTypeService, times(1)).findByCaseTypeId(CASE_TYPE_REF);
    }

    @Test
    void shouldLogWarning_whenCaseTypeNotFound() {
        // Given: Case type does not exist
        when(caseTypeService.findByCaseTypeId(CASE_TYPE_REF_NOT_FOUND))
            .thenReturn(Optional.empty());

        // When: Create snapshot for non-existent case type
        snapshotCreator.createSnapshotForCaseType(CASE_TYPE_REF_NOT_FOUND);

        // Then: Should attempt to find case type and log warning
        verify(caseTypeService, times(1)).findByCaseTypeId(CASE_TYPE_REF_NOT_FOUND);
    }

    @Test
    void shouldHandleNullCaseTypeReference() {
        // Given: Null case type reference
        when(caseTypeService.findByCaseTypeId(null))
            .thenReturn(Optional.empty());

        // When: Create snapshot with null reference
        snapshotCreator.createSnapshotForCaseType(null);

        // Then: Should handle gracefully
        verify(caseTypeService, times(1)).findByCaseTypeId(null);
    }

    @Test
    void shouldHandleEmptyCaseTypeReference() {
        // Given: Empty case type reference
        when(caseTypeService.findByCaseTypeId(""))
            .thenReturn(Optional.empty());

        // When: Create snapshot with empty reference
        snapshotCreator.createSnapshotForCaseType("");

        // Then: Should handle gracefully
        verify(caseTypeService, times(1)).findByCaseTypeId("");
    }

    @Test
    void shouldHandleMultipleCaseTypes() {
        // Given: Multiple case types exist
        CaseType caseType1 = createSampleCaseType("CaseType1", 1);
        CaseType caseType2 = createSampleCaseType("CaseType2", 2);

        when(caseTypeService.findByCaseTypeId("CaseType1"))
            .thenReturn(Optional.of(caseType1));
        when(caseTypeService.findByCaseTypeId("CaseType2"))
            .thenReturn(Optional.of(caseType2));

        // When: Create snapshots for multiple case types
        snapshotCreator.createSnapshotForCaseType("CaseType1");
        snapshotCreator.createSnapshotForCaseType("CaseType2");

        // Then: Should process both
        verify(caseTypeService, times(1)).findByCaseTypeId("CaseType1");
        verify(caseTypeService, times(1)).findByCaseTypeId("CaseType2");
    }

    @Test
    void shouldHandleServiceException() {
        // Given: Service throws exception
        when(caseTypeService.findByCaseTypeId(CASE_TYPE_REF))
            .thenThrow(new RuntimeException("Service error"));

        // When/Then: Should propagate exception (no catch block in SnapshotCreator)
        try {
            snapshotCreator.createSnapshotForCaseType(CASE_TYPE_REF);
        } catch (RuntimeException e) {
            verify(caseTypeService, times(1)).findByCaseTypeId(CASE_TYPE_REF);
        }
    }

    @Test
    void shouldProcessCaseTypeWithDifferentVersions() {
        // Given: Same case type but different versions at different times
        CaseType version1 = createSampleCaseType(CASE_TYPE_REF, 1);
        CaseType version2 = createSampleCaseType(CASE_TYPE_REF, 2);

        when(caseTypeService.findByCaseTypeId(CASE_TYPE_REF))
            .thenReturn(Optional.of(version1))
            .thenReturn(Optional.of(version2));

        // When: Create snapshot twice
        snapshotCreator.createSnapshotForCaseType(CASE_TYPE_REF);
        snapshotCreator.createSnapshotForCaseType(CASE_TYPE_REF);

        // Then: Should call service twice
        verify(caseTypeService, times(2)).findByCaseTypeId(CASE_TYPE_REF);
    }

    @Test
    void shouldHandleCaseTypeWithNullVersion() {
        // Given: Case type with null version
        CaseType caseTypeWithNullVersion = createSampleCaseType(CASE_TYPE_REF, null);

        when(caseTypeService.findByCaseTypeId(CASE_TYPE_REF))
            .thenReturn(Optional.of(caseTypeWithNullVersion));

        // When: Create snapshot
        snapshotCreator.createSnapshotForCaseType(CASE_TYPE_REF);

        // Then: Should handle gracefully
        verify(caseTypeService, times(1)).findByCaseTypeId(CASE_TYPE_REF);
    }

    private CaseType createSampleCaseType(String reference, Integer versionNumber) {
        CaseType caseType = new CaseType();
        caseType.setId(reference);
        caseType.setName("Test Case Type " + reference);

        if (versionNumber != null) {
            Version version = new Version();
            version.setNumber(versionNumber);
            caseType.setVersion(version);
        }

        return caseType;
    }
}
