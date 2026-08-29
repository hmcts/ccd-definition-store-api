package uk.gov.hmcts.ccd.definition.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
public class TransactionConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionConfiguration.class);

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory emf,
                                                         @Value("${ccd.tx-timeout.default}") String defaultTimeout,
                                                         @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
                                                         String hibernateJdbcBatchSize,
                                                         @Value("${spring.datasource.hikari.maximum-pool-size}")
                                                         String hikariMaximumPoolSize,
                                                         @Value("${spring.datasource.hikari.connection-timeout}")
                                                         String hikariConnectionTimeout,
                                                         @Value("${spring.datasource.hikari.keepalive-time:}")
                                                         String hikariKeepaliveTime,
                                                         @Value("${spring.datasource.hikari.max-lifetime:}")
                                                         String hikariMaxLifetime,
                                                         @Value("${spring.datasource.hikari.idle-timeout:}")
                                                         String hikariIdleTimeout,
                                                         @Value("${spring.datasource.hikari.validation-timeout:}")
                                                         String hikariValidationTimeout) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        int resolvedDefaultTimeout = resolveDefaultTimeout(defaultTimeout);
        transactionManager.setEntityManagerFactory(emf);
        transactionManager.setDefaultTimeout(resolvedDefaultTimeout);
        LOG.info("Default transaction timeout set to {} seconds", resolvedDefaultTimeout);
        LOG.info("Hibernate JDBC batch size set to {}", hibernateJdbcBatchSize);
        LOG.info("Hikari settings: maximumPoolSize={}, connectionTimeout={}, keepaliveTime={}, maxLifetime={}, "
                + "idleTimeout={}, validationTimeout={}",
            hikariMaximumPoolSize, hikariConnectionTimeout, hikariKeepaliveTime, hikariMaxLifetime, hikariIdleTimeout,
            hikariValidationTimeout);
        return transactionManager;
    }

    private int resolveDefaultTimeout(String defaultTimeout) {
        if (!StringUtils.hasText(defaultTimeout)) {
            return 30;
        }
        return Integer.parseInt(defaultTimeout);
    }

}
