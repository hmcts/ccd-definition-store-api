package uk.gov.hmcts.ccd.definition.store.repository;

import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

import static net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel.DEBUG;

@Configuration
@ComponentScan("uk.gov.hmcts.ccd.definition.store")
public class TestConfiguration {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        ChainListener listener = new ChainListener();
        SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
        loggingListener.setQueryLogEntryCreator(new DefaultQueryLogEntryCreator());
        listener.addListener(loggingListener);
        listener.addListener(new DataSourceQueryCountListener());
        return ProxyDataSourceBuilder
            .create(properties.initializeDataSourceBuilder().type(HikariDataSource.class).build())
            .name("DS-Proxy")
            .logQueryBySlf4j(DEBUG)
            .listener(listener)
            .build();
    }
}
