package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Guards the serialized shape of the {@code /api/data/case-type/{id}} response against silent
 * drift in the {@code case_type_snapshot} cache.
 *
 * <p>A snapshot row is a frozen JSON copy of this response, keyed by the case type <em>definition</em>
 * version. Changing the response model is a <em>code</em> change, so the definition version does not
 * move and the cache key still matches. Rows written before the change keep being served, silently
 * missing whatever was added.
 *
 * <p>That is harmless when a new field is populated by a new spreadsheet column, because a case type
 * that has not been re-imported has no value for it either way. It is <em>not</em> harmless when a new
 * field exposes data that already exists in the database: the snapshot then returns null while a
 * rebuild from the database returns the real value, and nothing reports the disagreement.
 *
 * <p>This test fails whenever the shape changes so that the author can decide which case they are in.
 * If the change exposes pre-existing data, invalidate the cache in the same release by adding a
 * migration containing {@code DELETE FROM case_type_snapshot;}.
 *
 * @see CaseTypeSnapshotRepository
 */
class CaseTypeResponseShapeGuardTest {

    private static final String BASELINE = "/case-type-response-shape.txt";

    @Test
    @DisplayName("Case type response shape is unchanged - if it changed, the snapshot cache "
        + "may need invalidating with DELETE FROM case_type_snapshot")
    void caseTypeResponseShapeUnchanged_otherwiseSnapshotCacheMayNeedInvalidating() {
        SortedSet<String> actual = serializedShapeOf(CaseType.class);
        SortedSet<String> expected = readBaseline();

        if (actual.equals(expected)) {
            return;
        }

        SortedSet<String> added = new TreeSet<>(actual);
        added.removeAll(expected);

        SortedSet<String> removed = new TreeSet<>(expected);
        removed.removeAll(actual);

        // assertEquals rather than fail() so the IDE and CI both render a usable diff
        assertEquals(expected, actual, buildFailureMessage(added, removed));
    }

    private static String buildFailureMessage(Set<String> added, Set<String> removed) {
        StringBuilder message = new StringBuilder("\n\nThe /case-type response shape has changed.\n");

        if (!added.isEmpty()) {
            message.append("\n  ADDED:   ").append(String.join(", ", added));
        }
        if (!removed.isEmpty()) {
            message.append("\n  REMOVED: ").append(String.join(", ", removed));
        }

        return message.append("""


            Stored case_type_snapshot rows hold a frozen copy of this response, keyed by the case type
            definition version. A code-only change does not move that version, so existing rows keep
            being served in the old shape until each case type is imported again.

            Decide which kind of change this is:

              1. The field is populated by NEW import data.
                 Nothing to do - a case type that has not been re-imported has no value for it either
                 way, so cached and rebuilt responses agree.

              2. The field exposes data ALREADY in the database, or a field was removed.
                 Invalidate the cache in this same release by adding a migration containing:

                     DELETE FROM case_type_snapshot;

                 Every case type then rebuilds from the database on first request, in the new shape.

            Then update src/test/resources%s to match.
            """.formatted(BASELINE)).toString();
    }

    /**
     * Walks the response model the way Jackson serializes it, so {@code @JsonProperty} renames and
     * ignored members are reflected exactly as they appear on the wire. Uses the same mapper the
     * snapshot cache reads and writes with.
     */
    private static SortedSet<String> serializedShapeOf(Class<?> rootType) {
        SortedSet<String> shape = new TreeSet<>();
        collect(JsonUtils.OBJECT_MAPPER.constructType(rootType), new HashSet<>(), shape);
        return shape;
    }

    private static void collect(JavaType type, Set<Class<?>> visited, SortedSet<String> shape) {
        if (type == null) {
            return;
        }

        if (type.isContainerType()) {
            collect(type.getKeyType(), visited, shape);
            collect(type.getContentType(), visited, shape);
            return;
        }

        Class<?> rawType = type.getRawClass();
        if (rawType.isPrimitive() || rawType.isEnum()
            || rawType.getName().startsWith("java.") || !visited.add(rawType)) {
            return;
        }

        SerializationConfig config = JsonUtils.OBJECT_MAPPER.getSerializationConfig();
        BeanDescription description = config.introspect(type);

        for (BeanPropertyDefinition property : description.findProperties()) {
            shape.add(rawType.getSimpleName() + "." + property.getName());
            collect(property.getPrimaryType(), visited, shape);
        }
    }

    private static SortedSet<String> readBaseline() {
        try (InputStream inputStream = CaseTypeResponseShapeGuardTest.class.getResourceAsStream(BASELINE)) {
            assertNotNull(inputStream, "Missing baseline resource: src/test/resources" + BASELINE);

            String contents = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            return contents.lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .collect(Collectors.toCollection(TreeSet::new));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read baseline resource " + BASELINE, e);
        }
    }
}
