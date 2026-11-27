package uk.gov.hmcts.ccd.definition.store;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonUtilsTest {

    @Test
    void shouldConvertObjectToJsonNodeMap() {
        Map<String, Object> source = Map.of("name", "caseType", "version", 1);

        Map<String, JsonNode> result = JacksonUtils.convertValue(source);

        assertThat(result.get("name").asText()).isEqualTo("caseType");
        assertThat(result.get("version").asInt()).isEqualTo(1);
    }

    @Test
    void shouldConvertObjectToJsonNode() {
        Map<String, Object> source = Map.of("name", "caseType");

        JsonNode result = JacksonUtils.convertValueJsonNode(source);

        assertThat(result.get("name").asText()).isEqualTo("caseType");
    }

    @Test
    void shouldProvideHashMapTypeReference() {
        Map<String, Object> source = Map.of("name", "caseType");

        Map<String, JsonNode> result = JacksonUtils.MAPPER.convertValue(
            source,
            JacksonUtils.getHashMapTypeReference()
        );

        assertThat(result.get("name").asText()).isEqualTo("caseType");
    }

    @Test
    void shouldAllowSingleQuotedAndUnquotedFieldNames() throws Exception {
        JsonNode result = JacksonUtils.MAPPER.readTree("{name:'caseType'}");

        assertThat(result.get("name").asText()).isEqualTo("caseType");
    }
}
