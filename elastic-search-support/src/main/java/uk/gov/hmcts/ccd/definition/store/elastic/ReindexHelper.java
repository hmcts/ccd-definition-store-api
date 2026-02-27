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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.ccd.definition.store.elastic.listener.ReindexListener;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
public class ReindexHelper {

    private final RestHighLevelClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private Executor executor = asyncExecutor();
    private static final String FAILURES = "failures";

    public ReindexHelper(RestHighLevelClient client) {
        this.client = client;
    }

    public String reindexIndex(String sourceIndex,
                               String destIndex,
                               long pollIntervalMs,
                               ReindexListener listener) throws IOException {

        String taskId = startReindexTask(sourceIndex, destIndex);
        String[] parts = taskId.split(":");
        String nodeId = parts[0];
        long taskNumericId = Long.parseLong(parts[1]);
        log.info("Reindex task started: {}", taskId);

        executor.execute(() -> monitorReindexTask(nodeId, taskNumericId, taskId, destIndex, pollIntervalMs, listener));

        return taskId;
    }

    private String startReindexTask(String sourceIndex, String destIndex) throws IOException {
        String jsonBody = String.format("""
            {
              "source": { "index": "%s" },
              "dest": { "index": "%s" }
            }
            """, sourceIndex, destIndex);

        Request request = buildReindexRequest(jsonBody);
        Response response = client.getLowLevelClient().performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        return mapper.readTree(responseBody).get("task").asText();
    }

    private Request buildReindexRequest(String jsonBody) {
        Request request = new Request("POST", "/_reindex");
        request.addParameter("wait_for_completion", "false");
        request.addParameter("refresh", "false");
        request.addParameter("requests_per_second", "-1");
        request.addParameter("timeout", "2h");
        request.addParameter("slices", "auto");
        request.setJsonEntity(jsonBody);
        return request;
    }

    private void monitorReindexTask(String nodeId,
                                    long taskNumericId,
                                    String taskId,
                                    String destIndex,
                                    long pollIntervalMs,
                                    ReindexListener listener) {

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Optional<GetTaskResponse> taskResponse = fetchTaskResponse(nodeId, taskNumericId);
                if (shouldExitPolling(taskResponse, taskId, pollIntervalMs, listener, destIndex)) {
                    break;
                }
                Thread.sleep(pollIntervalMs);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            listener.onFailure(new RuntimeException("Reindex polling interrupted", ie));
        } catch (IOException ioe) {
            listener.onFailure(new RuntimeException("Reindex process failed: " + ioe.getLocalizedMessage(), ioe));
        }
    }

    private boolean shouldExitPolling(Optional<GetTaskResponse> taskResponse,
                                      String taskId,
                                      long pollIntervalMs,
                                      ReindexListener listener,
                                      String destIndex) throws IOException, InterruptedException {

        if (shouldWaitForMissingTask(taskResponse, taskId, pollIntervalMs)) {
            return false;
        }

        if (taskResponse.isPresent()) {
            GetTaskResponse response = taskResponse.get();
            TaskInfo taskInfo = response.getTaskInfo();

            if (shouldWaitForMissingInfo(taskInfo, taskId, pollIntervalMs)) {
                return false;
            }

            if (response.isCompleted()) {
                return handleCompletion(taskInfo, listener, destIndex);
            }
        }
        return false;
    }


    private Optional<GetTaskResponse> fetchTaskResponse(String nodeId, long taskNumericId) throws IOException {
        GetTaskRequest getTaskRequest = new GetTaskRequest(nodeId, taskNumericId);
        return client.tasks().get(getTaskRequest, RequestOptions.DEFAULT);
    }

    private boolean shouldWaitForMissingTask(Optional<GetTaskResponse> taskResponse,
                                             String taskId,
                                             long pollIntervalMs) throws InterruptedException {
        if (taskResponse.isEmpty()) {
            log.info("Task not found: {}. Retrying...", taskId);
            Thread.sleep(pollIntervalMs);
            return true;
        }
        return false;
    }

    private boolean shouldWaitForMissingInfo(TaskInfo taskInfo,
                                             String taskId,
                                             long pollIntervalMs) throws InterruptedException {
        if (taskInfo == null) {
            log.info("Task info not found yet for task: {}. Retrying...", taskId);
            Thread.sleep(pollIntervalMs);
            return true;
        }
        return false;
    }

    private boolean handleCompletion(TaskInfo taskInfo,
                                     ReindexListener listener,
                                     String destIndex) throws IOException {
        Object statusObj = taskInfo.getStatus();
        if (statusObj == null) {
            listener.onFailure(new RuntimeException("Reindex process completed with unknown status"));
            return true;
        }

        JsonNode statusJson = toStatusJson(statusObj);
        if (hasFailures(statusJson)) {
            listener.onFailure(new RuntimeException("Reindex process failed: " + statusJson.path(FAILURES)));
            return true;
        }

        logProgress(destIndex, statusJson);
        listener.onSuccess();
        return true;
    }

    private JsonNode toStatusJson(Object statusObj) throws IOException {
        String json = mapper.writeValueAsString(statusObj);
        return mapper.readTree(json);
    }

    private boolean hasFailures(JsonNode statusJson) {
        return statusJson.has(FAILURES) && !statusJson.path(FAILURES).isEmpty();
    }

    private void logProgress(String destIndex, JsonNode statusJson) {
        int total = statusJson.path("total").asInt(0);
        int created = statusJson.path("created").asInt(0);
        int updated = statusJson.path("updated").asInt(0);
        int batches = statusJson.path("batches").asInt(0);
        log.info("Progress for index {}: total={}, created={}, updated={}, batches={}",
            destIndex, total, created, updated, batches);
    }

    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(50);
        threadPoolTaskExecutor.setThreadNamePrefix("reindex-exec-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
