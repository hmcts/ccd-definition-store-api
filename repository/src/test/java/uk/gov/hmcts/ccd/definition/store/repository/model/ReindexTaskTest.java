package uk.gov.hmcts.ccd.definition.store.repository.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.JsonUtils;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

class ReindexTaskTest {

    @Test
    void shouldSerializeStartAndEndTimeUsingExpectedPattern() {
        ReindexTask task = new ReindexTask();
        task.setStartTime(LocalDateTime.of(2026, 4, 28, 15, 0, 1));
        task.setEndTime(LocalDateTime.of(2026, 4, 28, 16, 10, 59));

        String json = JsonUtils.toString(task);

        assertThat(json, containsString("\"startTime\":\"2026-04-28 15:00:01\""));
        assertThat(json, containsString("\"endTime\":\"2026-04-28 16:10:59\""));
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

        assertThat(task.getStartTime(), is(equalTo(LocalDateTime.of(2026, 4, 28, 15, 0, 1))));
        assertThat(task.getEndTime(), is(equalTo(LocalDateTime.of(2026, 4, 28, 16, 10, 59))));
    }

    @Test
    void shouldRoundDurationFromMillisToNearestSecond() {
        ReindexTask task = new ReindexTask();
        LocalDateTime start = LocalDateTime.of(2026, 5, 18, 16, 3, 44, 100_000_000);
        LocalDateTime end = start.plus(Duration.ofMillis(1500));
        task.setStartTime(start);
        task.setEndTime(end);

        assertThat(task.getDuration(), is(equalTo(2L)));
    }
}
