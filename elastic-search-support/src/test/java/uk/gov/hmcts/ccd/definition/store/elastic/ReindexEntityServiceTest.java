package uk.gov.hmcts.ccd.definition.store.elastic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReindexEntityServiceTest {
    private ReindexRepository reindexRepository;
    private ReindexEntityService reindexEntityService;

    private final String oldIndexName = "casetypea_cases-000001";
    private final String newIndexName = "casetypea_cases-000002";

    @BeforeEach
    void setUp() {
        reindexRepository = mock(ReindexRepository.class);
        reindexEntityService = new ReindexEntityService(reindexRepository);
    }

    @Test
    void shouldInitiateReindexEntity() {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("caseTypeA");
        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference("jurA");
        caseType.setJurisdiction(jurisdiction);

        when(reindexRepository.findByIndexName(oldIndexName)).thenReturn(Optional.empty());

        ArgumentCaptor<ReindexEntity> captor = ArgumentCaptor.forClass(ReindexEntity.class);
        when(reindexRepository.saveAndFlush(any(ReindexEntity.class))).thenAnswer(i -> i.getArgument(0));

        ReindexEntity result = reindexEntityService.persistInitialReindexMetadata(
            true, true, caseType, newIndexName
        );

        verify(reindexRepository).saveAndFlush(captor.capture());

        ReindexEntity saved = captor.getValue();
        assertSame(saved, result);
        assertTrue(saved.getReindex());
        assertTrue(saved.getDeleteOldIndex());
        assertEquals("caseTypeA", saved.getCaseType());
        assertEquals("jurA", saved.getJurisdiction());
        assertEquals(newIndexName, saved.getIndexName());
        assertNotNull(saved.getStartTime());
        assertEquals("STARTED", saved.getStatus());
    }

    @Test
    void shouldPersistSuccess() {
        ReindexEntity existing = new ReindexEntity();
        existing.setIndexName(newIndexName);

        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(existing));

        reindexEntityService.persistSuccess(newIndexName);

        verify(reindexRepository).save(existing);
        assertEquals("SUCCESS", existing.getStatus());
        assertNotNull(existing.getEndTime());
    }

    @Test
    void shouldPersistFailure() {
        ReindexEntity existing = new ReindexEntity();
        existing.setIndexName(newIndexName);

        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(existing));

        RuntimeException ex = new RuntimeException("Simulated failure");
        reindexEntityService.persistFailure(newIndexName, ex);

        verify(reindexRepository).save(existing);
        assertEquals("FAILED", existing.getStatus());
        assertNotNull(existing.getEndTime());
        assertTrue(existing.getMessage().contains("Simulated failure"));
    }

    @Test
    void shouldUnwrapCompletionException() {
        Throwable root = new IllegalArgumentException("Root cause");
        CompletionException completion = new CompletionException(root);

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        reindexEntityService.persistFailure(newIndexName, completion);

        verify(reindexRepository).save(entity);
        assertTrue(entity.getMessage().contains("IllegalArgumentException"));
    }

    @Test
    void shouldSkipMarkSuccessIfEntityNotFound() {
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.empty());

        reindexEntityService.persistSuccess(newIndexName);

        verify(reindexRepository, never()).save(any());
    }

    @Test
    void shouldSkipMarkFailureIfEntityNotFound() {
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.empty());

        reindexEntityService.persistFailure(newIndexName, new RuntimeException("Fail"));

        verify(reindexRepository, never()).save(any());
    }
}
