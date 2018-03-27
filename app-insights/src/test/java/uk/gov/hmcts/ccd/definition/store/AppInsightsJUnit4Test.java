package uk.gov.hmcts.ccd.definition.store;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.TelemetryContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class AppInsightsJUnit4Test {
    private AppInsights classUnderTest;

    @Mock
    private TelemetryClient telemetryClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TelemetryContext telemetryContext = new TelemetryContext();
        telemetryContext.setInstrumentationKey("some-key");
        doReturn(telemetryContext).when(telemetryClient).getContext();
        classUnderTest = new AppInsights(telemetryClient);
    }

    @Test
    public void trackRequest() {
        classUnderTest.trackRequest(10L, false);
        verify(telemetryClient, times(1)).trackRequest(any());
    }


    @Test
    public void trackException() {
        Exception e = new Exception();
        classUnderTest.trackException(e);
        verify(telemetryClient, times(1)).trackException(e);
    }

    @Test
    public  void trackDependency() {
        classUnderTest.trackDependency("some", "command", 10L, true);
        verify(telemetryClient, times(1)).trackDependency(anyString(), anyString(), any(), anyBoolean());
    }
}
