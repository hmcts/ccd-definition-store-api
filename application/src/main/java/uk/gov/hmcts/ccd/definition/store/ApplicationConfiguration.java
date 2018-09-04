package uk.gov.hmcts.ccd.definition.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
class ApplicationConfiguration {

    @Value("${async.executor.core.pool.size}")
    private Integer corePoolSize;

    @Value("${async.executor.max.pool.size}")
    private Integer maxPoolSize;

    @Value("${async.executor.queue.capacity}")
    private Integer queueCapacity;

    @Value("${async.executor.prefix}")
    private String prefix;

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(prefix);
        executor.initialize();
        return executor;
    }
}
