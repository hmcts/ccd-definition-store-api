package uk.gov.hmcts.ccd.definition.store.repository;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void shouldFail_whenObjectCannotBeMappedAsString() {
        final LocalDate date = LocalDate.of(2020, 02, 20);
        final String s = JsonUtils.toString(date);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonUtils.fromString(s, String.class);
        });
        assertThat(ex.getMessage(), containsString(" cannot be transformed to Json object"));
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

    @Test
    public void shouldFail_whenObjectCannotBeMappedAsJsonNode() throws Throwable {
        final LocalDate date = LocalDate.of(2020, 02, 20);
        final JsonNode s = JsonUtils.toJsonNodeTree(date);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            JsonUtils.fromNode(s, String.class);
        });
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(MismatchedInputException.class));
        assertThat(cause.getMessage(), containsString(
                "Cannot deserialize value of type `java.lang.String` from Array value"));
    }
}
