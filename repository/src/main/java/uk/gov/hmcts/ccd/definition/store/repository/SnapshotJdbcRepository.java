package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.core.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Repository
public class SnapshotJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public SnapshotJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<CaseType> loadCaseTypeSnapshot(String caseTypeReference, Integer version) {
        try {
            return jdbcTemplate.query(
                "SELECT precomputed_response FROM case_type_snapshot " +
                    "WHERE case_type_reference = ? AND version_id = ?",
                (rs) -> {
                    if (rs.next()) {
                        // Stream directly from ResultSet without creating a large String
                        try (InputStream inputStream = rs.getBinaryStream("precomputed_response");
                             JsonParser parser = JsonUtils.OBJECT_MAPPER.getFactory().createParser(inputStream)) {
                                parser.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
                                CaseType result = JsonUtils.OBJECT_MAPPER.readValue(parser, CaseType.class);

                                return Optional.of(result);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return Optional.empty();
                },
                caseTypeReference, version);
        } catch (DataAccessException dae) {
            log.warn("database access failed for snapshot [{}:{}]: {}", caseTypeReference, version, dae.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error while deserializing snapshot [{}:{}]", caseTypeReference, version, e);
            return Optional.empty();
        }
    }
}
