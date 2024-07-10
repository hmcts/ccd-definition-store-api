package uk.gov.hmcts.ccd.definition.store.rest.configuration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


class Resilience4jConfigTest {
    @Mock
    private BulkheadRegistry bulkheadRegistry;

    @Mock
    private Bulkhead bulkhead;

    @Mock
    private Bulkhead.EventPublisher bulkheadEventPublisher;

    private Resilience4jConfig resilience4jConfig;

    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(bulkhead.getEventPublisher()).thenReturn(bulkheadEventPublisher);
        when(bulkheadRegistry.bulkhead(anyString(), any(BulkheadConfig.class))).thenReturn(bulkhead);

        resilience4jConfig = new Resilience4jConfig(bulkheadRegistry);
        resilience4jConfig = spy(resilience4jConfig);

        doReturn(10).when(resilience4jConfig).getBulkheadDefaultMaxConcurrentCalls();
        doReturn(1000).when(resilience4jConfig).getBulkheadDefaultMaxWaitDuration();
    }

    @Test
    public void testCaseTypeBulkhead() {
        Bulkhead result = resilience4jConfig.caseTypeBulkhead();
        assertNotNull(result);
    }

    @Test
    public void testEventTriggersBulkhead() {
        Bulkhead result = resilience4jConfig.eventTriggersBulkhead();
        assertNotNull(result);
    }

    @Test
    public void testJurisdictionBulkhead() {
        Bulkhead result = resilience4jConfig.jurisdictionBulkhead();
        assertNotNull(result);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }
}
