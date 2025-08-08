package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.ccd.definition.store.excel.service.ReindexTaskServiceImpl;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.util.List;

@RestController
@RequestMapping(ReindexTaskController.REINDEX_TASKS_URI)
@Api(value = ReindexTaskController.REINDEX_TASKS_URI)
public class ReindexTaskController {

    public static final String REINDEX_TASKS_URI = "/elastic-support/reindex/tasks";

    private final ReindexTaskServiceImpl reindexTaskService;

    @Autowired
    public ReindexTaskController(ReindexTaskServiceImpl reindexTaskService) {
        this.reindexTaskService = reindexTaskService;
    }

    @GetMapping
    @ApiOperation(value = "Get all reindex tasks, optionally by case type",
                  response = ReindexEntity.class,
                  responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved reindex tasks"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<List<ReindexEntity>> getReindexTasksByCaseType(
        @RequestParam(value = "caseType", required = false) String caseType
    ) {
        List<ReindexEntity> tasks = reindexTaskService.getTasksByCaseType(caseType);
        return ResponseEntity.ok(tasks);
    }
}
