package uk.gov.hmcts.ccd.definition.store;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.core.env.Environment;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;


class SwaggerConfigurationTest extends BaseTest {

    private static final int EXPECTED_TOTAL_ENDPOINTS = 28;
    private static final String EXPECTED_APPLICATION_CONTEXT_NAME = "application-1";

    @Autowired
    private WebEndpointsSupplier webEndpointsSupplier;

    @Autowired
    private ServletEndpointsSupplier servletEndpointsSupplier;

    @Autowired
    private ControllerEndpointsSupplier controllerEndpointsSupplier;

    @Autowired
    private EndpointMediaTypes endpointMediaTypes;

    @Autowired
    private CorsEndpointProperties corsProperties;

    @Autowired
    private WebEndpointProperties webEndpointProperties;

    @Autowired
    private Environment environment;

    @Autowired
    private SwaggerConfiguration swaggerConfiguration;

    @Test
    public void successfullyLoadWebEndpointServletHandlerMappingTest() {
        assertNotNull(webEndpointsSupplier);
        assertNotNull(servletEndpointsSupplier);
        assertNotNull(controllerEndpointsSupplier);
        assertNotNull(endpointMediaTypes);
        assertNotNull(corsProperties);
        assertNotNull(webEndpointProperties);
        assertNotNull(environment);
        assertNotNull(swaggerConfiguration);

        WebMvcEndpointHandlerMapping webMvcEndpointHandlerMapping =
            swaggerConfiguration.webEndpointServletHandlerMapping(webEndpointsSupplier, servletEndpointsSupplier,
            controllerEndpointsSupplier, endpointMediaTypes,corsProperties, webEndpointProperties, environment);

        assertEquals(EXPECTED_TOTAL_ENDPOINTS, webMvcEndpointHandlerMapping.getEndpoints().size());
        assertEquals(EXPECTED_APPLICATION_CONTEXT_NAME, webMvcEndpointHandlerMapping.getApplicationContext().getId());
    }

}
