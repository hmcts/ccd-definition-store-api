package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.event.SnapshotCreationEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsynchronousSnapshotCreationListenerTest {

    private static final String CASE_TYPE_REF_1 = "CaseType1";
    private static final String CASE_TYPE_REF_2 = "CaseType2";
    private static final String CASE_TYPE_REF_3 = "CaseType3";
    private static final String JURISDICTION = "TestJurisdiction";

    @Mock
    private SnapshotCreator snapshotCreator;

    @InjectMocks
    private AsynchronousSnapshotCreationListener listener;

    @Test
    void shouldCreateSnapshots_whenEventReceived() {
        // Given: Event with multiple case types
        doNothing().when(snapshotCreator).createSnapshotForCaseType(anyString());

        List<String> caseTypeReferences = Arrays.asList(CASE_TYPE_REF_1, CASE_TYPE_REF_2, CASE_TYPE_REF_3);
        SnapshotCreationEvent event = new SnapshotCreationEvent(JURISDICTION, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should create snapshot for each case type
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_1);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_2);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_3);
    }

    @Test
    void shouldHandleSingleCaseType() {
        // Given: Event with single case type
        doNothing().when(snapshotCreator).createSnapshotForCaseType(anyString());

        List<String> caseTypeReferences = Collections.singletonList(CASE_TYPE_REF_1);
        SnapshotCreationEvent event = new SnapshotCreationEvent(JURISDICTION, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should create snapshot for the case type
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_1);
    }

    @Test
    void shouldHandleEmptyList() {
        // Given: Event with empty list
        List<String> caseTypeReferences = Collections.emptyList();
        SnapshotCreationEvent event = new SnapshotCreationEvent(JURISDICTION, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should not call snapshot creator
        verify(snapshotCreator, never()).createSnapshotForCaseType(anyString());
    }

    @Test
    void shouldHandleNullCaseTypeReferences() {
        // Given: Event with null case type references
        SnapshotCreationEvent mockEvent = mock(SnapshotCreationEvent.class);
        when(mockEvent.jurisdiction()).thenReturn(JURISDICTION);
        when(mockEvent.caseTypeReferences()).thenReturn(null);

        // When: Event is processed
        listener.onSnapshotCreationRequested(mockEvent);

        // Then: Should log warning and return early
        verify(snapshotCreator, never()).createSnapshotForCaseType(anyString());
    }

    @Test
    void shouldHandleNullJurisdiction() {
        // Given: Event with null jurisdiction
        doNothing().when(snapshotCreator).createSnapshotForCaseType(anyString());

        List<String> caseTypeReferences = Arrays.asList(CASE_TYPE_REF_1, CASE_TYPE_REF_2);
        SnapshotCreationEvent event = new SnapshotCreationEvent(null, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should still create snapshots (logs "all" as jurisdiction)
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_1);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_2);
    }

    @Test
    void shouldContinueProcessing_whenOneSnapshotFails() {
        // Given: Second snapshot creation fails
        doNothing().when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_1);
        doThrow(new RuntimeException("Snapshot creation failed"))
            .when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_2);
        doNothing().when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_3);

        List<String> caseTypeReferences = Arrays.asList(CASE_TYPE_REF_1, CASE_TYPE_REF_2, CASE_TYPE_REF_3);
        SnapshotCreationEvent event = new SnapshotCreationEvent(JURISDICTION, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should continue processing remaining case types
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_1);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_2);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_3);
    }

    @Test
    void shouldHandleAllSnapshotsFailing() {
        // Given: All snapshot creations fail
        doThrow(new RuntimeException("Snapshot 1 failed"))
            .when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_1);
        doThrow(new RuntimeException("Snapshot 2 failed"))
            .when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_2);
        doThrow(new RuntimeException("Snapshot 3 failed"))
            .when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_3);

        List<String> caseTypeReferences = Arrays.asList(CASE_TYPE_REF_1, CASE_TYPE_REF_2, CASE_TYPE_REF_3);
        SnapshotCreationEvent event = new SnapshotCreationEvent(JURISDICTION, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should attempt all and log failures
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_1);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_2);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_3);
    }

    @Test
    void shouldHandleMixOfSuccessAndFailure() {
        // Given: Mix of successful and failed snapshot creations
        doNothing().when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_1);
        doThrow(new RuntimeException("Failed"))
            .when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_2);
        doNothing().when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_3);

        List<String> caseTypeReferences = Arrays.asList(CASE_TYPE_REF_1, CASE_TYPE_REF_2, CASE_TYPE_REF_3);
        SnapshotCreationEvent event = new SnapshotCreationEvent(JURISDICTION, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should process all and log success/failure counts
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_1);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_2);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_3);
    }

    @Test
    void shouldHandleLargeNumberOfCaseTypes() {
        // Given: Large number of case types
        doNothing().when(snapshotCreator).createSnapshotForCaseType(anyString());

        List<String> caseTypeReferences = Arrays.asList(
            "CT1", "CT2", "CT3", "CT4", "CT5",
            "CT6", "CT7", "CT8", "CT9", "CT10"
        );
        SnapshotCreationEvent event = new SnapshotCreationEvent(JURISDICTION, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should process all case types
        for (String ref : caseTypeReferences) {
            verify(snapshotCreator, times(1)).createSnapshotForCaseType(ref);
        }
    }

    @Test
    void shouldHandleDifferentExceptionTypes() {
        // Given: Different types of exceptions
        doThrow(new RuntimeException("Runtime exception"))
            .when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_1);
        doThrow(new IllegalStateException("Illegal state"))
            .when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_2);
        doThrow(new NullPointerException("Null pointer"))
            .when(snapshotCreator).createSnapshotForCaseType(CASE_TYPE_REF_3);

        List<String> caseTypeReferences = Arrays.asList(CASE_TYPE_REF_1, CASE_TYPE_REF_2, CASE_TYPE_REF_3);
        SnapshotCreationEvent event = new SnapshotCreationEvent(JURISDICTION, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should handle all exception types gracefully
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_1);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_2);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_3);
    }

    @Test
    void shouldHandleEmptyJurisdictionString() {
        // Given: Event with empty jurisdiction string
        doNothing().when(snapshotCreator).createSnapshotForCaseType(anyString());

        List<String> caseTypeReferences = Collections.singletonList(CASE_TYPE_REF_1);
        SnapshotCreationEvent event = new SnapshotCreationEvent("", caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should still process
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_1);
    }

    @Test
    void shouldHandleDuplicateCaseTypeReferences() {
        // Given: Event with duplicate case type references
        doNothing().when(snapshotCreator).createSnapshotForCaseType(anyString());

        List<String> caseTypeReferences = Arrays.asList(CASE_TYPE_REF_1, CASE_TYPE_REF_1, CASE_TYPE_REF_2);
        SnapshotCreationEvent event = new SnapshotCreationEvent(JURISDICTION, caseTypeReferences);

        // When: Event is processed
        listener.onSnapshotCreationRequested(event);

        // Then: Should process each occurrence (even duplicates)
        verify(snapshotCreator, times(2)).createSnapshotForCaseType(CASE_TYPE_REF_1);
        verify(snapshotCreator, times(1)).createSnapshotForCaseType(CASE_TYPE_REF_2);
    }
}
