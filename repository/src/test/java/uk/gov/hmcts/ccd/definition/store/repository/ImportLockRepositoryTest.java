package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {SanityCheckApplication.class, TestConfiguration.class})
@TestPropertySource(locations = "classpath:test.properties")
class ImportLockRepositoryTest {

    @Autowired
    private ImportLockRepository importLockRepository;

    @Autowired
    private FieldTypeRepository fieldTypeRepository;

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void sameJurisdictionWaitsUntilCommitOrRollbackBeforeAllocatingVersions(boolean existingJurisdiction,
                                                                           boolean rollback) throws Exception {
        String reference = "ImportLock-" + UUID.randomUUID();
        CountDownLatch firstSaved = new CountDownLatch(1);
        CountDownLatch finishFirst = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            if (existingJurisdiction) {
                transaction().executeWithoutResult(status -> findOrCreateJurisdiction(reference));
            }
            final Future<Integer> first = executor.submit(() -> transaction().execute(status -> {
                importLockRepository.lockJurisdiction(reference);
                final int version = saveFieldType(reference);
                firstSaved.countDown();
                await(finishFirst);
                if (rollback) {
                    status.setRollbackOnly();
                }
                return version;
            }));
            assertTrue(firstSaved.await(10, TimeUnit.SECONDS), "First transaction must flush its insert");

            Future<Integer> second = executor.submit(() -> transaction().execute(status -> {
                secondStarted.countDown();
                importLockRepository.lockJurisdiction(reference);
                return saveFieldType(reference);
            }));
            assertTrue(secondStarted.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> second.get(250, TimeUnit.MILLISECONDS),
                "The second transaction must wait even after the first insert has been flushed");

            finishFirst.countDown();
            assertEquals(1, first.get(10, TimeUnit.SECONDS));
            assertEquals(rollback ? 1 : 2, second.get(10, TimeUnit.SECONDS));
            assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM jurisdiction WHERE reference = ?", Integer.class, reference));
            assertEquals(rollback ? 1 : 2, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM field_type WHERE reference = ?", Integer.class, reference));
        } finally {
            finishFirst.countDown();
            executor.shutdown();
            assertTrue(executor.awaitTermination(20, TimeUnit.SECONDS));
            transaction().executeWithoutResult(status -> {
                jdbcTemplate.update("DELETE FROM field_type WHERE reference = ?", reference);
                jdbcTemplate.update("DELETE FROM jurisdiction WHERE reference = ?", reference);
            });
        }
    }

    @Test
    void differentJurisdictionsCanImportConcurrently() throws Exception {
        String reference = "ImportLock-" + UUID.randomUUID();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            transaction().executeWithoutResult(status -> {
                importLockRepository.lockJurisdiction(reference);
                Future<?> other = executor.submit(() -> transaction().executeWithoutResult(otherStatus ->
                    importLockRepository.lockJurisdiction(reference + "-other")));
                try {
                    other.get(5, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    throw new AssertionError("Another jurisdiction must not wait for this transaction", ex);
                }
            });
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(20, TimeUnit.SECONDS));
        }
    }

    @Test
    void requiresAnImportTransaction() {
        assertThrows(IllegalTransactionStateException.class,
            () -> importLockRepository.lockJurisdiction("without-transaction"));
    }

    private TransactionTemplate transaction() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setTimeout(15);
        return transaction;
    }

    private int saveFieldType(String reference) {
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(reference);
        fieldType.setJurisdiction(findOrCreateJurisdiction(reference));
        fieldType.setBaseFieldType(fieldTypeRepository.findBaseType("FixedList").orElseThrow());
        new VersionedDefinitionRepositoryDecorator<>(fieldTypeRepository).saveAll(List.of(fieldType));
        fieldTypeRepository.flush();
        return fieldType.getVersion();
    }

    private JurisdictionEntity findOrCreateJurisdiction(String reference) {
        return jurisdictionRepository.findFirstByReferenceOrderByVersionDesc(reference).orElseGet(() -> {
            JurisdictionEntity created = new JurisdictionEntity();
            created.setReference(reference);
            created.setName(reference);
            return new VersionedDefinitionRepositoryDecorator<>(jurisdictionRepository).save(created);
        });
    }

    private void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting to complete the first transaction");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
    }
}
