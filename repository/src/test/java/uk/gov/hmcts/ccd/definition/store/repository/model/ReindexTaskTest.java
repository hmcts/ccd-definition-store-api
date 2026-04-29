package uk.gov.hmcts.ccd.definition.store.repository.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.JsonUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReindexTaskTest {

    @Test
    void shouldSerializeStartAndEndTimeUsingExpectedPattern() {
        ReindexTask task = new ReindexTask();
        task.setStartTime(LocalDateTime.of(2026, 4, 28, 15, 0, 1));
        task.setEndTime(LocalDateTime.of(2026, 4, 28, 16, 10, 59));

        String json = JsonUtils.toString(task);

        assertTrue(json.contains("\"startTime\":\"2026-04-28 15:00:01\""));
        assertTrue(json.contains("\"endTime\":\"2026-04-28 16:10:59\""));
    }

    @Test
    void shouldDeserializeStartAndEndTimeUsingExpectedPattern() {
        String json = """
            {
              "startTime": "2026-04-28 15:00:01",
              "endTime": "2026-04-28 16:10:59"
            }
            """;

        ReindexTask task = JsonUtils.fromString(json, ReindexTask.class);

        assertEquals(LocalDateTime.of(2026, 4, 28, 15, 0, 1), task.getStartTime());
        assertEquals(LocalDateTime.of(2026, 4, 28, 16, 10, 59), task.getEndTime());
    }
}
