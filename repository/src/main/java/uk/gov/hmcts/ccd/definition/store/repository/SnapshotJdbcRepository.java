package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.core.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Optional;

@Slf4j
@Repository
public class SnapshotJdbcRepository {

    private static final String PRECOMPUTED_RESPONSE = "precomputed_response";
    private final JdbcTemplate jdbcTemplate;

    public SnapshotJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<CaseType> loadCaseTypeSnapshot(String caseTypeReference, Integer version) {
        if (caseTypeReference == null || version == null) {
            log.warn("Attempted to load snapshot with null parameters: ref={}, ver={}", caseTypeReference, version);
            return Optional.empty();
        }

        try {
            return jdbcTemplate.query(
                "SELECT precomputed_response FROM case_type_snapshot "
                    + "WHERE case_type_reference = ? AND version_id = ?",
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    try (InputStream inputStream = rs.getBinaryStream(PRECOMPUTED_RESPONSE)) {
                        if (inputStream == null) {
                            return Optional.empty();
                        }

                        try (JsonParser parser = JsonUtils.OBJECT_MAPPER.getFactory().createParser(inputStream)) {
                            parser.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
                            return Optional.ofNullable(JsonUtils.OBJECT_MAPPER.readValue(parser, CaseType.class));
                        }
                    } catch (IOException | SQLException e) {
                        log.error("Deserialization failed for snapshot [{}:{}]", caseTypeReference, version, e);
                        return Optional.empty();
                    }
                },
                caseTypeReference, version);
        } catch (Exception e) {
            log.error("Unexpected error retrieving snapshot [{}:{}]", caseTypeReference, version, e);
            return Optional.empty();
        }
    }
}
