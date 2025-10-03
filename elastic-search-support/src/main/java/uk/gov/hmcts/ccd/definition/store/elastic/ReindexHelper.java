package uk.gov.hmcts.ccd.definition.store.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.GetTaskRequest;
import org.elasticsearch.client.tasks.GetTaskResponse;
import org.elasticsearch.tasks.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
public class ReindexHelper {

    private static final Logger log = LoggerFactory.getLogger(ReindexHelper.class);
    private final RestHighLevelClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private Executor executor = asyncExecutor();

    public ReindexHelper(RestHighLevelClient client) {
        this.client = client;
    }

    public String reindexIndex(String sourceIndex,
                             String destIndex,
                             long pollIntervalMs,
                             ReindexListener listener) throws IOException {
        String jsonBody = "{ \"source\": { \"index\": \"" + sourceIndex + "\" }, "
            + "\"dest\": { \"index\": \"" + destIndex + "\" } }";

        Request request = new Request("POST", "/_reindex");
        request.addParameter("wait_for_completion", "false"); // async
        request.addParameter("refresh", "false"); // refresh later
        request.setJsonEntity(jsonBody);

        Response response = client.getLowLevelClient().performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        String taskId = mapper.readTree(responseBody).get("task").asText();
        String[] parts = taskId.split(":");
        String nodeId = parts[0];
        log.info("Reindex task started: {}", taskId);

        executor.execute(() -> {
            try {

                while (true) {
                    GetTaskRequest getTaskRequest = new GetTaskRequest(nodeId, Long.valueOf(parts[1]));
                    Optional<GetTaskResponse> taskResponse = client.tasks().get(getTaskRequest, RequestOptions.DEFAULT);

                    if (taskResponse.isEmpty()) {
                        log.info("Task not found: {}. Retrying...", taskId);
                        Thread.sleep(pollIntervalMs);
                        continue;
                    }

                    TaskInfo taskInfo = taskResponse.get().getTaskInfo();
                    if (taskInfo == null) {
                        log.info("Task info not found yet for task: {}. Retrying...", taskId);
                        Thread.sleep(pollIntervalMs);
                        continue;
                    }

                    if (taskResponse.get().isCompleted()) {
                        Object statusObj = taskInfo.getStatus();
                        if (statusObj != null) {
                            String json = mapper.writeValueAsString(statusObj);
                            JsonNode statusJson = mapper.readTree(json);

                            if (statusJson.has("failures")
                                && !statusJson.path("failures").isEmpty()) {
                                listener.onFailure(new RuntimeException("Reindex process failed: "
                                    + statusJson.path("failures")));
                            } else {
                                int total = statusJson.path("total").asInt(0);
                                int created = statusJson.path("created").asInt(0);
                                int updated = statusJson.path("updated").asInt(0);
                                int batches = statusJson.path("batches").asInt(0);
                                log.info("Progress: total={}, created={}, updated={}, batches={}",
                                    total, created, updated, batches);
                                listener.onSuccess();
                            }
                        }
                        break;
                    }
                }
            } catch (InterruptedException | IOException ie) {
                listener.onFailure(new RuntimeException("Reindex process failed: " + ie.getLocalizedMessage()));
            }
        });
        return taskId;
    }

    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("reindex-exec-");
        executor.initialize();
        return executor;
    }
}
