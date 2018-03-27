package uk.gov.hmcts.ccd.definition.store;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import com.microsoft.applicationinsights.telemetry.RequestTelemetry;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

@Component
public class AppInsights extends AbstractAppInsights {
    private static final String MODULE = "CASE_DEFINITION";

    public AppInsights(TelemetryClient telemetry) {
        super(telemetry);
    }

    public void trackRequest(long duration, boolean success) {
        RequestTelemetry rt = new RequestTelemetry();
        rt.setName(MODULE);
        rt.setSuccess(success);
        rt.setDuration(new Duration(duration));
        telemetry.trackRequest(rt);
    }

    public void trackException(Exception e) {
        telemetry.trackException(e);
    }

    public void trackDependency(String dependencyName, String commandName, long duration, boolean success) {
        telemetry.trackDependency(dependencyName, commandName, new Duration(duration), success);
    }
}
