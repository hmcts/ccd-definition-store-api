package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.exc.MismatchedInputException;

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

    @Test
    public void shouldGetObject_whenFromStringToLocalDate() {

        final LocalDate date = LocalDate.of(2020, 02, 20);
        final String s = JsonUtils.toString(date);
        assertThat(JsonUtils.fromString(s, LocalDate.class), is(LocalDate.of(2020, 02, 20)));
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

    @Test
    public void shouldGetObject_whenFromJsonNodeToLocalDate() {

        final LocalDate date = LocalDate.of(2020, 02, 20);
        final JsonNode s = JsonUtils.toJsonNodeTree(date);

        assertThat(JsonUtils.fromNode(s, LocalDate.class), is(LocalDate.of(2020, 02, 20)));
    }

    @Test(expected = MismatchedInputException.class)
    public void shouldFail_whenObjectCannotBeMappedAsJsonNode() throws Throwable {
        final LocalDate date = LocalDate.of(2020, 02, 20);
        final JsonNode s = JsonUtils.toJsonNodeTree(date);

        try {
            JsonUtils.fromNode(s, String.class);
        } catch (IllegalArgumentException ex) {
            final Throwable cause = ex.getCause();
            assertThat(cause, instanceOf(JsonMappingException.class));
            Assertions.assertThat(cause).hasMessageContaining("Cannot deserialize value of type `java.lang.String` from Array value");
            throw cause;
        }
    }
}


