package uk.gov.hmcts.ccd.definition.store.rest.configuration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
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
    @Value("${bulkhead.default.maxConcurrentCalls}")
    private int bulkheadDefaultMaxConcurrentCalls;

    @Getter
    @Value("${bulkhead.default.maxWaitDuration}")
    private int bulkheadDefaultMaxWaitDuration;

    public static final String BULKHEAD_CASE_TYPE = "case_type";
    public static final String BULKHEAD_JURISDICTIONS = "jurisdictions";
    public static final String BULKHEAD_EVENT_TRIGGERS = "event-triggers";

    private final BulkheadRegistry bulkheadRegistry;

    public Resilience4jConfig(BulkheadRegistry bulkheadRegistry) {
        this.bulkheadRegistry = bulkheadRegistry;
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

    private void initiateBulkheadEvent(Bulkhead bulkhead) {
        Bulkhead.EventPublisher eventPublisher = bulkhead
            .getEventPublisher();

        eventPublisher.onCallRejected(event -> log.error(event.toString()));
    }
}
