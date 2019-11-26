package uk.gov.hmcts.net.ccd.definition.store;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.web.context.ContextCleanupListener;
import uk.gov.hmcts.ccd.definition.store.excel.service.ImportServiceImpl;

import java.io.IOException;

@Configuration
public class TestConfiguration extends ContextCleanupListener {

    @Autowired
    public void loadLocalOverridePropertiesIfItExists(StandardEnvironment environment) {
        try {
            String localPropertiesPath = environment.resolvePlaceholders("classpath:test-local-override.properties");
            ResourcePropertySource localPropertySource = new ResourcePropertySource(localPropertiesPath);
            environment.getPropertySources().addFirst(localPropertySource);
        } catch (IOException ignored) {
        }
    }

    @Bean
    @Primary
    public ImportServiceImpl importServiceSpy(ImportServiceImpl importService) {
        // Use a spy (which is autowired in BaseTest), so specific methods can be overridden
        return Mockito.spy(importService);
    }
}
