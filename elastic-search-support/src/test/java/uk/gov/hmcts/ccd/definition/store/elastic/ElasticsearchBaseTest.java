package uk.gov.hmcts.ccd.definition.store.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        ServletWebServerFactoryAutoConfiguration.class,
        ElasticsearchConfigurationIT.class
    }
)
@ContextConfiguration(initializers = ElasticsearchContainerInitializer.class)
public abstract class ElasticsearchBaseTest implements TestUtils {

    protected static final String WILDCARD = "*";

    @Autowired
    protected ElasticsearchClient elasticsearchClient;

    @Autowired
    protected ObjectMapper objectMapper;

    protected CustomComparator ignoreFieldsComparator(String... paths) {
        return new CustomComparator(JSONCompareMode.LENIENT, Arrays.stream(paths)
            .map(path -> new Customization(path, (o1, o2) -> true))
            .toArray(Customization[]::new));
    }

    protected String getElasticsearchIndices(String... caseTypes) throws IOException {
        String indexPattern = getIndicesFromCaseTypes(caseTypes);
        GetIndexResponse response = elasticsearchClient.indices()
            .get(g -> g.index(indexPattern));
        return objectMapper.writeValueAsString(response.toString());
    }

    protected String deleteElasticsearchIndices(String... caseTypes) throws IOException {
        String indexPattern = caseTypes[0].equals(WILDCARD) ? WILDCARD : getIndicesFromCaseTypes(caseTypes);
        DeleteIndexResponse response = elasticsearchClient.indices()
            .delete(d -> d.index(indexPattern));
        return "acknowledged: " + response.acknowledged();
    }

    private String getIndicesFromCaseTypes(String... caseTypes) {
        return Arrays.stream(caseTypes)
            .map(caseType -> caseType.toLowerCase() + "_cases")
            .collect(Collectors.joining(","));
    }

    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(
        WebEndpointsSupplier webEndpointsSupplier,
        EndpointMediaTypes endpointMediaTypes,
        CorsEndpointProperties corsProperties,
        WebEndpointProperties webEndpointProperties,
        Environment environment) {

        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>(webEndpointsSupplier.getEndpoints());

        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);

        return new WebMvcEndpointHandlerMapping(
            endpointMapping,
            webEndpointsSupplier.getEndpoints(),
            endpointMediaTypes,
            corsProperties.toCorsConfiguration(),
            new EndpointLinksResolver(allEndpoints, basePath),
            shouldRegisterLinksMapping
        );
    }

    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment,
                                               String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled()
            && (StringUtils.hasText(basePath)
            || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }
}
