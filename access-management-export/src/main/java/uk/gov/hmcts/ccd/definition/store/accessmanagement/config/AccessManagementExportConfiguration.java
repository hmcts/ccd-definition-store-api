package uk.gov.hmcts.ccd.definition.store.accessmanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = {
    "uk.gov.hmcts.ccd.definition.store.accessmanagement",
    "uk.gov.hmcts.reform.amlib"})
@EnableConfigurationProperties(value = AccessManagementExportProperties.class)
@Slf4j
public class AccessManagementExportConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @Autowired
    public DefaultRoleSetupImportService defaultRoleSetupImportService(DataSource datasource) {
        return new DefaultRoleSetupImportService(datasource) {
        };
    }
}
