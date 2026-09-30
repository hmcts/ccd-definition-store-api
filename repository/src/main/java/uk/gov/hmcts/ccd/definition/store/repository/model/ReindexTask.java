package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ReindexTask {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    private boolean deleteOldIndex;
    private String caseType;
    private String jurisdiction;
    private String indexName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String status;
    private String exceptionMessage;
    private String reindexResponse;
    private String whoImported;

    public Long getDuration() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return Duration.between(
            startTime.truncatedTo(ChronoUnit.SECONDS).atZone(DEFAULT_ZONE),
            endTime.truncatedTo(ChronoUnit.SECONDS).atZone(DEFAULT_ZONE)
        ).getSeconds();
    }
}
