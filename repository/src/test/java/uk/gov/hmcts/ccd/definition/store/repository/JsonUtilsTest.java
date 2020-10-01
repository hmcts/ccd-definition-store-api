package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class JsonUtilsTest {

    @Test
    public void shouldGetObject_whenFromAndToString() {

        final String s = JsonUtils.toString("NGITB");

        assertThat(JsonUtils.fromString(s, String.class), is("NGITB"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFail_whenObjectsCannotBeMappedFromString() {

        final LocalDate date = LocalDate.of(2020, 02, 20);
        final String s = JsonUtils.toString(date);

        try {
            JsonUtils.fromString(s, LocalDate.class);
        } catch (IllegalArgumentException ex) {
            Assertions.assertThat(ex).hasMessageContaining(" cannot be transformed to Json object");
            throw ex;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFail_whenObjectCannotBeMappedAsString() {
        final LocalDate date = LocalDate.of(2020, 02, 20);
        final String s = JsonUtils.toString(date);

        try {
            JsonUtils.fromString(s, String.class);
        } catch (IllegalArgumentException ex) {
            Assertions.assertThat(ex).hasMessageContaining(" cannot be transformed to Json object");
            throw ex;
        }
    }

    @Test
    public void shouldGetObject_whenFromAndToJsonNode() {

        final JsonNode node = JsonUtils.toJsonNodeTree("NGITB");

        assertThat(JsonUtils.fromNode(node, String.class), is("NGITB"));
    }

    @Test(expected = JsonMappingException.class)
    public void shouldFail_whenObjectsCannotBeMappedJsonNode() throws Throwable {

        final LocalDate date = LocalDate.of(2020, 02, 20);
        final JsonNode s = JsonUtils.toJsonNodeTree(date);

        try {
            JsonUtils.fromNode(s, LocalDate.class);
        } catch (IllegalArgumentException ex) {
            final Throwable cause = ex.getCause();
            assertThat(cause, instanceOf(JsonMappingException.class));
            Assertions.assertThat(cause).hasMessageContaining("Cannot construct instance of `java.time.LocalDate`");
            throw cause;
        }
    }

    @Test(expected = JsonMappingException.class)
    public void shouldFail_whenObjectCannotBeMappedAsJsonNode() throws Throwable {
        final LocalDate date = LocalDate.of(2020, 02, 20);
        final JsonNode s = JsonUtils.toJsonNodeTree(date);

        try {
            JsonUtils.fromNode(s, String.class);
        } catch (IllegalArgumentException ex) {
            final Throwable cause = ex.getCause();
            assertThat(cause, instanceOf(JsonMappingException.class));
            Assertions.assertThat(cause).hasMessageContaining(
                "Cannot deserialize instance of `java.lang.String` out of START_OBJECT token");
            throw cause;
        }
    }
}


