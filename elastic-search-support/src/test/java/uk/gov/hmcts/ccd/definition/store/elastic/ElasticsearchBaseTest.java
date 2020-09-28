package uk.gov.hmcts.ccd.definition.store.elastic;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = { ServletWebServerFactoryAutoConfiguration.class, ElasticsearchConfigurationIT.class })
@ContextConfiguration(initializers = ElasticsearchContainerInitializer.class)
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
            .performRequest(method, endpoint)
            .getEntity());
    }

    private String getIndicesFromCaseTypes(String... caseTypes) {
        return Arrays.stream(caseTypes)
            .map(caseType -> caseType.toLowerCase() + "_cases")
            .collect(Collectors.joining(","));
    }
}
