package uk.gov.hmcts.ccd.definition.store;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppInsightsConfiguration {

    @Bean
    public TelemetryClient telemetryClient() {
        return new TelemetryClient();
    }
}
