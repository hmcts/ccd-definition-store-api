package uk.gov.hmcts.ccd.definition.store.repository.am.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource({"classpath:application.properties"})
public class AmDataSourceConfiguration {

    @Primary
    @Bean(name = "definitionDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("amDataSource")
    @ConfigurationProperties(prefix = "am.datasource")
    public DataSource getAmDataSource() {
        return DataSourceBuilder.create().build();
    }
}
