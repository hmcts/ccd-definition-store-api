package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ImportLockRepository {

    // Keep import locks separate from other users of PostgreSQL's two-integer advisory lock keys ("CCDI").
    private static final int IMPORT_LOCK_NAMESPACE = 0x43434449;

    private final JdbcTemplate jdbcTemplate;

    public ImportLockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Serializes imports for a jurisdiction across application instances, including its first import.
     * The caller must acquire the lock before reading versions and keep the transaction open until
     * all import data has been committed or rolled back. Hash collisions only serialize unrelated imports.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockJurisdiction(String reference) {
        jdbcTemplate.queryForObject("SELECT pg_advisory_xact_lock(?, hashtext(?))", Object.class,
            IMPORT_LOCK_NAMESPACE, reference);
    }
}
