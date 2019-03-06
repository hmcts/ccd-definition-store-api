package uk.gov.hmcts.ccd.definition.store.accessmanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "uk.gov.hmcts.ccd.definition.store.accessmanagement",
    "uk.gov.hmcts.reform.amlib"})
@EnableConfigurationProperties(value = AccessManagementExportProperties.class)
@Slf4j
public class AccessManagementExportConfiguration {

    @Autowired
    private AccessManagementExportProperties config;
}
