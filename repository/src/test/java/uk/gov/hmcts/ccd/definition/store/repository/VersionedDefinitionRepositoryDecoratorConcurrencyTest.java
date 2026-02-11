package uk.gov.hmcts.ccd.definition.store.repository;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VersionedDefinitionRepositoryDecoratorConcurrencyTest {

    @Test
    void saveAllConcurrentSameReferenceRetriesOnDuplicateKey() throws Exception {
        VersionedDefinitionRepository<CaseTypeEntity, Integer> repository = mock(VersionedDefinitionRepository.class);

        AtomicInteger currentVersion = new AtomicInteger(0);
        ConcurrentHashMap<String, Set<Integer>> usedVersions = new ConcurrentHashMap<>();
        CyclicBarrier versionReadBarrier = new CyclicBarrier(2);
        AtomicInteger versionReadCount = new AtomicInteger(0);
        AtomicBoolean versionOneUsed = new AtomicBoolean(false);
        AtomicBoolean duplicateThrown = new AtomicBoolean(false);
        AtomicBoolean firstBatchAttempt = new AtomicBoolean(true);

        when(repository.findLastVersion(anyString())).thenAnswer(invocation -> {
            int call = versionReadCount.incrementAndGet();
            if (call <= 2) {
                try {
                    versionReadBarrier.await(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
            return Optional.of(currentVersion.get());
        });

        when(repository.saveAll(any())).thenAnswer(invocation -> {
            if (firstBatchAttempt.getAndSet(false)) {
                duplicateThrown.set(true);
                throw new DataIntegrityViolationException(
                    "duplicate key value violates unique constraint "
                        + "\"unique_field_type_reference_version_jurisdiction\"");
            }

            Iterable<CaseTypeEntity> iterable = invocation.getArgument(0);
            List<CaseTypeEntity> entities = new ArrayList<>();
            for (CaseTypeEntity entity : iterable) {
                entities.add(entity);
            }

            String reference = entities.get(0).getReference();
            usedVersions.putIfAbsent(reference, ConcurrentHashMap.newKeySet());
            Set<Integer> versionsForRef = usedVersions.get(reference);

            for (CaseTypeEntity entity : entities) {
                if (entity.getVersion() == 1 && !versionOneUsed.compareAndSet(false, true)) {
                    duplicateThrown.set(true);
                    throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
                }
                if (versionsForRef.contains(entity.getVersion())) {
                    duplicateThrown.set(true);
                    throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
                }
            }

            for (CaseTypeEntity entity : entities) {
                versionsForRef.add(entity.getVersion());
                currentVersion.updateAndGet(v -> Math.max(v, entity.getVersion()));
            }

            return entities;
        });

        CaseTypeEntity firstEntity = new CaseTypeEntity();
        firstEntity.setReference("dup");
        CaseTypeEntity secondEntity = new CaseTypeEntity();
        secondEntity.setReference("dup");

        VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> decorator =
            new VersionedDefinitionRepositoryDecorator<>(repository);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);

        Callable<CaseTypeEntity> task1 = () -> {
            startGate.await(5, TimeUnit.SECONDS);
            return decorator.saveAll(List.of(firstEntity)).get(0);
        };

        Callable<CaseTypeEntity> task2 = () -> {
            startGate.await(5, TimeUnit.SECONDS);
            return decorator.saveAll(List.of(secondEntity)).get(0);
        };

        Future<CaseTypeEntity> first = executor.submit(task1);
        Future<CaseTypeEntity> second = executor.submit(task2);
        startGate.countDown();

        try {
            int v1 = first.get(10, TimeUnit.SECONDS).getVersion();
            int v2 = second.get(10, TimeUnit.SECONDS).getVersion();
            assertThat(List.of(v1, v2), containsInAnyOrder(1, 2));
            assertThat("Expected duplicate key error to occur at least once", duplicateThrown.get());
        } finally {
            executor.shutdownNow();
        }
    }
}
