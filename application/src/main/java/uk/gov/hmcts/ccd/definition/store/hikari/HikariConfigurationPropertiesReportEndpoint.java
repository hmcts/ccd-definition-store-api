package uk.gov.hmcts.ccd.definition.store.hikari;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zaxxer.hikari.HikariDataSource;

import java.util.ArrayList;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.boot.actuate.endpoint.Show;
import org.springframework.stereotype.Component;

// needed to fix  "error" : "Cannot serialize 'spring.datasource.hikari'" in actuator /configprops endpoint
// and be able to see configuration properties set for Hikari connection pool
@Component
public class HikariConfigurationPropertiesReportEndpoint extends ConfigurationPropertiesReportEndpoint {

    public HikariConfigurationPropertiesReportEndpoint() {
        super(new ArrayList<SanitizingFunction>(), Show.ALWAYS);
    }

    @Override
    protected void configureJsonMapper(JsonMapper.Builder builder) {
        super.configureJsonMapper(builder);
        builder.addMixIn(HikariDataSource.class, HikariDataSourceMixIn.class);
    }
}
