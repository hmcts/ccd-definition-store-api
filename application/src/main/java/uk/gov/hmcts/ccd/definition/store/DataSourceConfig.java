package uk.gov.hmcts.ccd.definition.store;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel.INFO;

import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("master.datasource")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("master.datasource.hikari")
    public DataSource masterDataSource() {
        HikariDataSource dataSource = masterDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();

        return ProxyDataSourceBuilder
            .create(dataSource)
            .name("master-db-data-source")
            .countQuery()
            .logQueryBySlf4j(INFO)
            .build();
    }

    @Bean
    @Primary
    @ConfigurationProperties("replicas.datasource")
    public DataSourceProperties replicasDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("replicas.datasource.hikari")
    public DataSource replicasDataSource() {
        HikariDataSource dataSource = replicasDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();

        return ProxyDataSourceBuilder
            .create(dataSource)
            .name("replica-db-data-source")
            .countQuery()
            .logQueryBySlf4j(INFO)
            .build();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        final RoutingDataSource routingDataSource = new RoutingDataSource();

        final DataSource masterDataSource = masterDataSource();
        final DataSource replicaDataSource = replicasDataSource();

        final Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(RoutingDataSource.Route.MASTER, masterDataSource);
        targetDataSources.put(RoutingDataSource.Route.REPLICA, replicaDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(replicaDataSource);

        return routingDataSource;
    }

}
