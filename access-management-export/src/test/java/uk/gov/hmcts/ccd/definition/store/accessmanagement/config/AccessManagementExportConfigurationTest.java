package uk.gov.hmcts.ccd.definition.store.accessmanagement.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;

@DisplayName("amConfig")
public class AccessManagementExportConfigurationTest {

    private AccessManagementExportConfiguration amConfig;

    @BeforeEach
    void setUp() {
        amConfig = new AccessManagementExportConfiguration();
    }

    @Test
    @DisplayName("should provide a DefaultRoleSetupImportService")
    void shouldProvideDefaultRoleSetupImportService() {
        final DataSource dataSource = Mockito.mock(DataSource.class);
        DefaultRoleSetupImportService service = amConfig.defaultRoleSetupImportService(dataSource);
        assertNotNull(service);
    }
}
