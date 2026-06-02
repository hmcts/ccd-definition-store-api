package uk.gov.hmcts.ccd.definition.store.repository.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.JsonUtils;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
    void shouldCalculateDurationUsingSamePrecisionAsDisplayedTimes() {
        ReindexTask row1 = new ReindexTask();
        row1.setStartTime(LocalDateTime.of(2026, 5, 18, 16, 3, 44, 900_000_000));
        row1.setEndTime(LocalDateTime.of(2026, 5, 18, 16, 3, 46, 100_000_000));
        assertThat(row1.getDuration(), is(equalTo(2L)));

        ReindexTask row2 = new ReindexTask();
        row2.setStartTime(LocalDateTime.of(2026, 5, 18, 13, 59, 9, 800_000_000));
        row2.setEndTime(LocalDateTime.of(2026, 5, 18, 13, 59, 15, 300_000_000));
        assertThat(row2.getDuration(), is(equalTo(6L)));
    }

    @Test
    void shouldReturnNullDurationWhenEndTimeIsNull() {
        ReindexTask task = new ReindexTask();
        task.setStartTime(LocalDateTime.of(2026, 4, 30, 20, 34, 35));

        String json = JsonUtils.toString(task);

        assertThat(task.getDuration(), is(nullValue()));
        assertThat(json, containsString("\"duration\":null"));
    }
}
