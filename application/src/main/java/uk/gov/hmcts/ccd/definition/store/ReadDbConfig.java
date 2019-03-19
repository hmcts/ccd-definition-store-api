package uk.gov.hmcts.ccd.definition.store;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "readEntityManagerFactory",
    transactionManagerRef = "readTransactionManager", basePackages = {"uk.gov.hmcts.ccd.definition.store.repository"})
public class ReadDbConfig {

    @Bean
    @Primary
    @ConfigurationProperties("read.datasource")
    public DataSourceProperties readDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "readDataSource")
    @Profile("!test")
    @Primary
    @ConfigurationProperties("read.datasource.hikari")
    public DataSource readDataSource() {
        return readDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "readEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(
        EntityManagerFactoryBuilder builder, @Qualifier("readDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("uk.gov.hmcts.ccd.definition.store.repository.entity").persistenceUnit("read").build();
    }

    @Bean(name = "readTransactionManager")
    @Primary
    public PlatformTransactionManager readTransactionManager(
        @Qualifier("readEntityManagerFactory") EntityManagerFactory readEntityManagerFactory) {
        return new JpaTransactionManager(readEntityManagerFactory);
    }

}
