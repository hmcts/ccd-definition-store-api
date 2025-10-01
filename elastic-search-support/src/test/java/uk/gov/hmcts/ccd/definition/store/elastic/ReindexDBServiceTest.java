package uk.gov.hmcts.ccd.definition.store.elastic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexDBService;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexDBServiceImpl;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReindexDBServiceTest {
    private ReindexRepository reindexRepository;
    private ReindexDBService reindexDBService;

    private final String oldIndexName = "casetypea_cases-000001";
    private final String newIndexName = "casetypea_cases-000002";

    @BeforeEach
    void setUp() {
        reindexRepository = mock(ReindexRepository.class);
        EntityToResponseDTOMapper mapper = mock(EntityToResponseDTOMapper.class);
        reindexDBService = new ReindexDBServiceImpl(reindexRepository, mapper);
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

        ReindexEntity result = reindexDBService.saveEntity(
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
    void shouldUpdateEntitySuccess() {
        ReindexEntity existing = new ReindexEntity();
        existing.setIndexName(newIndexName);

        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(existing));

        reindexDBService.updateEntity(newIndexName, anyString());

        verify(reindexRepository).save(existing);
        assertEquals("SUCCESS", existing.getStatus());
        assertNotNull(existing.getEndTime());
        assertNotNull(existing.getReindexResponse());
    }

    @Test
    void shouldUpdateEntityFailure() {
        ReindexEntity existing = new ReindexEntity();
        existing.setIndexName(newIndexName);

        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(existing));

        RuntimeException ex = new RuntimeException("Simulated failure");
        reindexDBService.updateEntity(newIndexName, ex);

        verify(reindexRepository).save(existing);
        assertEquals("FAILED", existing.getStatus());
        assertNotNull(existing.getEndTime());
        assertTrue(existing.getExceptionMessage().contains("Simulated failure"));
        assertNull(existing.getReindexResponse());
    }

    @Test
    void shouldUnwrapCompletionException() {
        Throwable root = new IllegalArgumentException("Root cause");
        CompletionException completion = new CompletionException(root);

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        reindexDBService.updateEntity(newIndexName, completion);

        verify(reindexRepository).save(entity);
        assertTrue(entity.getExceptionMessage().contains("IllegalArgumentException"));
    }

    @Test
    void shouldSkipMarkSuccessIfEntityNotFound() {
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reindexDBService.updateEntity(newIndexName, anyString());
        });

        assertTrue(exception.getMessage().contains("No reindex entity metadata found for index name"));
        verify(reindexRepository, never()).save(any());
    }

    @Test
    void shouldSkipMarkFailureIfEntityNotFound() {
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.empty());

        reindexDBService.updateEntity(newIndexName, new RuntimeException("Fail"));

        verify(reindexRepository, never()).save(any());
    }

    @Test
    void shouldNotUnwrapRegularException() {
        RuntimeException regularException = new RuntimeException("Regular exception");

        ReindexEntity entity = new ReindexEntity();
        when(reindexRepository.findByIndexName(newIndexName)).thenReturn(Optional.of(entity));

        reindexDBService.updateEntity(newIndexName, regularException);

        verify(reindexRepository).save(entity);
        assertTrue(entity.getExceptionMessage().contains("RuntimeException"));
        assertTrue(entity.getExceptionMessage().contains("Regular exception"));
    }
}
