package uk.gov.hmcts.ccd.definition.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController;
import uk.gov.hmcts.ccd.definition.store.hikari.CustomTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableCaching
class ApplicationConfiguration {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Executor asyncExecutor(@Value("${async.executor.core.pool.size}") Integer corePoolSize,
                                  @Value("${async.executor.max.pool.size}") Integer maxPoolSize,
                                  @Value("${async.executor.queue.capacity}") Integer queueCapacity,
                                  @Value("${async.executor.prefix}") String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(prefix);
        executor.initialize();
        return executor;
    }

    @Bean
    public CacheManagerCustomizer<CaffeineCacheManager> cacheManagerCustomizer() {
        return cacheManager -> cacheManager.setAllowNullValues(false);
    }

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory emf,
                                                         @Value("${ccd.tx-timeouts.default}") String timeout) {
        final CustomTransactionManager transactionManager = new CustomTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        transactionManager.configureTxTimeout(
            ImportController.class, "processUpload", Integer.parseInt(timeout));
        return transactionManager;
    }
}
