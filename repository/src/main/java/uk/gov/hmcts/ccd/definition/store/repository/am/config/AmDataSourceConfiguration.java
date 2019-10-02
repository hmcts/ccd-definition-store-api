package uk.gov.hmcts.ccd.definition.store.repository.am.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import javax.sql.DataSource;

@Configuration
@PropertySource({"classpath:application.properties"})
public class AmDataSourceConfiguration {

    /*@Bean
    public AccessManagementService getAmAccessManagementService(@Qualifier("amDataSource") DataSource dataSource) {
        return new AccessManagementService(dataSource);
    }

    @Bean
    public DefaultRoleSetupImportService getAmDefaultRoleSetupImportService(@Qualifier("amDataSource") DataSource dataSource) {
        return new DefaultRoleSetupImportService(dataSource);
    }*/

    @Bean("amDataSource")
    @ConfigurationProperties(prefix = "am.datasource")
    public DataSource getAmDataSource() {
        return DataSourceBuilder.create().build();
    }
}
