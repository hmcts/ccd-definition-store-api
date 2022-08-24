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

        assertEquals(16, webMvcEndpointHandlerMapping.getEndpoints().size());
        assertEquals("application-1", webMvcEndpointHandlerMapping.getApplicationContext().getId());
    }

}
