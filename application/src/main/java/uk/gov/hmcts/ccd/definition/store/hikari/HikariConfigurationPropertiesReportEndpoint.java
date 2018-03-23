package uk.gov.hmcts.ccd.definition.store.hikari;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.actuate.endpoint.ConfigurationPropertiesReportEndpoint;
import org.springframework.stereotype.Component;

// needed to fix  "error" : "Cannot serialize 'spring.datasource.hikari'" in actuator /configprops endpoint
// and be able to see configuration properties set for Hikari connection pool
@Component
public class HikariConfigurationPropertiesReportEndpoint extends ConfigurationPropertiesReportEndpoint {

    @Override
    protected void configureObjectMapper(ObjectMapper mapper) {
        super.configureObjectMapper(mapper);
        mapper.addMixIn(HikariDataSource.class, HikariDataSourceMixIn.class);
    }
}