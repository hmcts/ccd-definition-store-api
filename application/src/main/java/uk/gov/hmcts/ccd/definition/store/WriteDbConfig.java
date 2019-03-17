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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "writeEntityManagerFactory",
    transactionManagerRef = "writeTransactionManager", basePackages = {"uk.gov.hmcts.ccd.definition.store.write.repository"})
public class WriteDbConfig {

    @Bean
    @ConfigurationProperties("write.datasource")
    public DataSourceProperties writeDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "writeDataSource")
    @ConfigurationProperties("write.datasource.hikari")
    public DataSource writeDataSource() {
        return writeDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "writeEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory(
        EntityManagerFactoryBuilder builder, @Qualifier("writeDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("uk.gov.hmcts.ccd.definition.store.repository.entity").persistenceUnit("write").build();
    }

    @Bean(name = "writeTransactionManager")
    public PlatformTransactionManager writeTransactionManager(
        @Qualifier("writeEntityManagerFactory") EntityManagerFactory writeEntityManagerFactory) {
        return new JpaTransactionManager(writeEntityManagerFactory);
    }

}
