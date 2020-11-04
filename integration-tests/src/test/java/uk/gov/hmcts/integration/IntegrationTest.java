package uk.gov.hmcts.integration;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.ContextCleanupListener;
import uk.gov.hmcts.ccd.definition.store.CaseDataAPIApplication;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CaseDataAPIApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(locations = "classpath:integration_tests.properties")
abstract class IntegrationTest {

    protected static final String VALID_IDAM_TOKEN = "Bearer UserAuthToken";
    protected static final String INVALID_IDAM_TOKEN = "Bearer irJ7eg6TsoOPW2uoWCXmUwHeMy5Nz9si2fRNiZbL";
    protected static final String BEARER = "Bearer ";
    protected static final String HEADER_SERVICE_AUTHORIZATION = "ServiceAuthorization";
    protected static final String APPLICATION_JSON = "application/json";

    @Autowired
    protected TestRestTemplate restTemplate;

    @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
    @TestConfiguration
    static class Configuration extends ContextCleanupListener {

        private EmbeddedPostgres pg;
        private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

        @Bean
        public DataSource dataSource() throws IOException, SQLException {

            pg = EmbeddedPostgres
                .builder()
                .setPort(0)
                .start();
            return dataSource(pg);
        }

        @PreDestroy
        public void contextDestroyed() throws IOException {
            if (null != pg) {
                LOG.info("Closing down Postgres, port number = {}", pg.getPort());
                pg.close();
            }
        }

        private DataSource dataSource(final EmbeddedPostgres pg) throws SQLException {
            final Properties props = new Properties();
            // Instruct JDBC to accept JSON string for JSONB
            props.setProperty("stringtype", "unspecified");
            final Connection connection = DriverManager.getConnection(pg.getJdbcUrl("postgres", "postgres"), props);
            LOG.info("Started Postgres, port number = {}", pg.getPort());
            return new SingleConnectionDataSource(connection, true);
        }
    }


    protected HttpEntity<Object> invalidRequestEntity() {
        return new HttpEntity<>(buildHeaders(INVALID_IDAM_TOKEN));
    }

    protected HttpEntity<?> validRequestEntity() {
        return new HttpEntity<>(validHeaders());
    }

    protected void assertHappyPath(final ResponseEntity<String> response) {
        assertThat(response.getStatusCodeValue(), not(401));
        assertThat(response.getStatusCodeValue(), not(403));
    }

    private HttpHeaders validHeaders() {
        return buildHeaders(VALID_IDAM_TOKEN);
    }

    private HttpHeaders buildHeaders(final String idamToken) {
        HttpHeaders headers = buildHeaders();
        headers.add(AUTHORIZATION, idamToken);
        return headers;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_SERVICE_AUTHORIZATION, "ServiceToken");
        headers.add(CONTENT_TYPE, APPLICATION_JSON);
        return headers;
    }
}
