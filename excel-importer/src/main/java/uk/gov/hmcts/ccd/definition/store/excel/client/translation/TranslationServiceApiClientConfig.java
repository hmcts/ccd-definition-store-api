package uk.gov.hmcts.ccd.definition.store.excel.client.translation;

import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;

public class TranslationServiceApiClientConfig {

    @Bean
    public ServiceAuthHeadersInterceptor systemUserAuthHeadersInterceptor(SecurityUtils securityUtils) {
        return new ServiceAuthHeadersInterceptor(securityUtils);
    }

}
