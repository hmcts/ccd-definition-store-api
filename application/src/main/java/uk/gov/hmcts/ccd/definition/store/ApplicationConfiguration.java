package uk.gov.hmcts.ccd.definition.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

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
    @Primary
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

    @Bean("validateExecutor")
    public Executor validateExecutor(@Value("${validate.executor.core.pool.size}") Integer corePoolSize,
                                     @Value("${validate.executor.max.pool.size}") Integer maxPoolSize,
                                     @Value("${validate.executor.prefix}") String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix(prefix);
        executor.setAwaitTerminationMillis(1000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean(name = "reindexExecutor")
    public Executor reindexExecutor(@Value("${reindex.executor.core.pool.size}") Integer corePoolSize,
                                    @Value("${reindex.executor.max.pool.size}") Integer maxPoolSize,
                                    @Value("${reindex.executor.queue.capacity}") Integer queueCapacity,
                                    @Value("${reindex.executor.prefix}") String prefix) {
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
}
