package uk.gov.hmcts.ccd.definition.store.rest.configuration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class Resilience4jConfig {

    @Getter
    @Value("${ratelimiter.default.limitForPeriod}")
    private int ratelimiterDefaultLimitForPeriod;

    @Getter
    @Value("${ratelimiter.default.limitRefreshPeriod}")
    private int ratelimiterDefaultLimitRefreshPeriod;

    @Getter
    @Value("${ratelimiter.default.timeoutDuration}")
    private int ratelimiterDefaultTimeoutDuration;

    @Getter
    @Value("${bulkhead.default.maxConcurrentCalls}")
    private int bulkheadDefaultMaxConcurrentCalls;

    @Getter
    @Value("${bulkhead.default.maxWaitDuration}")
    private int bulkheadDefaultMaxWaitDuration;

    public static final String RATE_LIMITER_CASE_TYPE = "case_type";
    public static final String RATE_LIMITER_JURISDICTIONS = "jurisdictions";
    public static final String RATE_LIMITER_EVENT_TRIGGERS = "event-triggers";

    public static final String BULKHEAD_CASE_TYPE = "case_type";
    public static final String BULKHEAD_JURISDICTIONS = "jurisdictions";
    public static final String BULKHEAD_EVENT_TRIGGERS = "event-triggers";

    private final RateLimiterRegistry rateLimiterRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    public Resilience4jConfig(RateLimiterRegistry rateLimiterRegistry, BulkheadRegistry bulkheadRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
    }

    @Bean
    public RateLimiter caseTypeRateLimiter() {
        RateLimiter rateLimiter = createRateLimiter(RATE_LIMITER_CASE_TYPE,
            getRatelimiterDefaultLimitForPeriod(),
            getRatelimiterDefaultLimitRefreshPeriod(),
            getRatelimiterDefaultTimeoutDuration());
        initiateRateLimiterEvent(rateLimiter);

        return rateLimiter;
    }

    @Bean
    public RateLimiter eventTriggersRateLimiter() {
        RateLimiter rateLimiter = createRateLimiter(RATE_LIMITER_EVENT_TRIGGERS,
            getRatelimiterDefaultLimitForPeriod(),
            getRatelimiterDefaultLimitRefreshPeriod(),
            getRatelimiterDefaultTimeoutDuration());
        initiateRateLimiterEvent(rateLimiter);

        return rateLimiter;
    }

    @Bean
    public RateLimiter jurisdictionRateLimiter() {
        RateLimiter rateLimiter = createRateLimiter(RATE_LIMITER_JURISDICTIONS,
            getRatelimiterDefaultLimitForPeriod(),
            getRatelimiterDefaultLimitRefreshPeriod(),
            getRatelimiterDefaultTimeoutDuration());
        initiateRateLimiterEvent(rateLimiter);

        return rateLimiter;
    }

    private RateLimiter createRateLimiter(final String name, int limitForPeriod, int limitRefreshPeriod,
                                          int timeoutDuration) {
        RateLimiterConfig config = RateLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(timeoutDuration))
            .limitRefreshPeriod(Duration.ofSeconds(limitRefreshPeriod))
            .limitForPeriod(limitForPeriod)
            .build();

        return rateLimiterRegistry.rateLimiter(name, config);
    }

    @Bean
    public Bulkhead caseTypeBulkhead() {
        Bulkhead bulkhead = createBulkhead(BULKHEAD_CASE_TYPE,
            bulkheadDefaultMaxConcurrentCalls,
            bulkheadDefaultMaxWaitDuration);
        initiateBulkheadEvent(bulkhead);

        return bulkhead;
    }

    @Bean
    public Bulkhead eventTriggersBulkhead() {
        Bulkhead bulkhead = createBulkhead(BULKHEAD_EVENT_TRIGGERS,
            bulkheadDefaultMaxConcurrentCalls,
            bulkheadDefaultMaxWaitDuration);
        initiateBulkheadEvent(bulkhead);

        return bulkhead;
    }

    @Bean
    public Bulkhead jurisdictionBulkhead() {
        Bulkhead bulkhead = createBulkhead(BULKHEAD_JURISDICTIONS,
            bulkheadDefaultMaxConcurrentCalls,
            bulkheadDefaultMaxWaitDuration);
        initiateBulkheadEvent(bulkhead);

        return bulkhead;
    }

    private Bulkhead createBulkhead(final String name, int bulkheadDefaultMaxConcurrentCalls,
                                    int bulkheadDefaultMaxWaitDuration) {
        BulkheadConfig config = BulkheadConfig.custom()
            .maxConcurrentCalls(bulkheadDefaultMaxConcurrentCalls)
            .maxWaitDuration(Duration.ofMillis(bulkheadDefaultMaxWaitDuration))
            .build();

        return bulkheadRegistry.bulkhead(name, config);
    }

    private void initiateRateLimiterEvent(RateLimiter rateLimiter) {
        RateLimiter.EventPublisher eventPublisher = rateLimiter
            .getEventPublisher();

        eventPublisher.onSuccess(event -> log.info(event.toString()));
        eventPublisher.onFailure(event -> log.info(event.toString()));
    }

    private void initiateBulkheadEvent(Bulkhead bulkhead) {
        Bulkhead.EventPublisher eventPublisher = bulkhead
            .getEventPublisher();

        // eventPublisher.onCallPermitted(event -> log.info(event.toString()));
        eventPublisher.onCallRejected(event -> log.error(event.toString()));
    }
}
