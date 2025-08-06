package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserInfoMixin;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;

public class JacksonUtils {

    private static final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
        .featuresToEnable(MapperFeature.DEFAULT_VIEW_INCLUSION)
        .featuresToEnable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
        .featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        .mixIn(UserInfo.class, UserInfoMixin.class)
        .modulesToInstall(JavaTimeModule.class)
        .build();

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static HashMap<String, JsonNode> convertValue(Object from) {
        return objectMapper.convertValue(from, new TypeReference<HashMap<String, JsonNode>>() {
        });
    }

    public static JsonNode convertValueJsonNode(Object from) {
        return objectMapper.convertValue(from, JsonNode.class);
    }

    public static final TypeReference<HashMap<String, JsonNode>> getHashMapTypeReference() {
        return new TypeReference<HashMap<String, JsonNode>>() {
        };
    }

}
