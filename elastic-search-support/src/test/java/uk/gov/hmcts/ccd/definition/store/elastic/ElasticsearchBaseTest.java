package uk.gov.hmcts.ccd.definition.store.elastic;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestHighLevelClient;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.CaseDataAPIApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {ServletWebServerFactoryAutoConfiguration.class, ElasticsearchConfigurationIT.class,
        CaseDataAPIApplication.class})
@ContextConfiguration(initializers = ElasticsearchContainerInitializer.class)
@ActiveProfiles("test")
public abstract class ElasticsearchBaseTest implements TestUtils {

    private static final String GET = "GET";
    private static final String DELETE = "DELETE";
    protected static final String WILDCARD = "*";

    @Autowired
    private RestHighLevelClient elasticClient;

    protected CustomComparator ignoreFieldsComparator(String... paths) {
        return new CustomComparator(JSONCompareMode.LENIENT, Arrays.stream(paths)
            .map(path -> new Customization(path, ((o1, o2) -> true)))
            .toArray(Customization[]::new));
    }

    protected String getElasticsearchIndices(String... caseTypes) throws IOException {
        return elasticResponseAsString(GET, String.format("/%s", getIndicesFromCaseTypes(caseTypes)));
    }

    protected String deleteElasticsearchIndices(String... caseTypes) throws IOException {
        String indices = caseTypes[0].equals(WILDCARD) ? WILDCARD : String.format(
            "/%s", getIndicesFromCaseTypes(caseTypes));
        return elasticResponseAsString(DELETE, indices);
    }

    private String elasticResponseAsString(String method, String endpoint) throws IOException {
        return EntityUtils.toString(elasticClient
            .getLowLevelClient()
            .performRequest(new Request(method, endpoint))
            .getEntity());
    }

    private String getIndicesFromCaseTypes(String... caseTypes) {
        return Arrays.stream(caseTypes)
            .map(caseType -> caseType.toLowerCase() + "_cases")
            .collect(Collectors.joining(","));
    }

    //CCD-3509 CVE-2021-22044 required to fix null pointers in integration tests,
    //conflict in Springfox after Springboot 2.6.10
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier,
        ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier,
        EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties,
        WebEndpointProperties webEndpointProperties, Environment environment) {

        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment,
            basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
            corsProperties.toCorsConfiguration(),
            new EndpointLinksResolver(allEndpoints, basePath),
            shouldRegisterLinksMapping);
    }

    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment,
                                               String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath)
            || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }

}
