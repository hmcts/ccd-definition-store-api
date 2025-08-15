package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserInfoMixin;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
            .featuresToEnable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .featuresToEnable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            .featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
            .mixIn(UserInfo.class, UserInfoMixin.class)
            .modulesToInstall(JavaTimeModule.class)
            .build();

    private JsonUtils() {
    }

    public static <T> T fromString(String string, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(string, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                String.format("The given string value: %s cannot be transformed to Json object", string), e);
        }
    }

    public static String toString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new
                IllegalArgumentException(
                String.format("The given Json object value: %s cannot be transformed to a String", value), e);
        }
    }

    public static JsonNode toJsonNode(String value) {
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static JsonNode toJsonNodeTree(Object value) {
        return OBJECT_MAPPER.valueToTree(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T value) {
        return fromString(toString(value), (Class<T>) value.getClass());
    }

    public static <T> T fromNode(JsonNode node, Class<T> type) {
        try {
            return OBJECT_MAPPER.treeToValue(node, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
