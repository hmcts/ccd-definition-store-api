package uk.gov.hmcts.ccd.definition.store.excel.client.translation;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;

public class ServiceAuthHeadersInterceptor implements RequestInterceptor {

    private static final String EXPERIMENTAL = "experimental";

    private final SecurityUtils securityUtils;

    public ServiceAuthHeadersInterceptor(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!template.headers().containsKey("ServiceAuthorization")) {
            template.header("ServiceAuthorization", securityUtils.getS2SToken());
        }
        template.header(EXPERIMENTAL, "true");
    }
}

