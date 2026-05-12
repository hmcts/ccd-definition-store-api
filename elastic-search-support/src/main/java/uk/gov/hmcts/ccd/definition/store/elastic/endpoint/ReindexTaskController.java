package uk.gov.hmcts.ccd.definition.store.elastic.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexService;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexTask;

@RestController
@RequestMapping(ReindexTaskController.REINDEX_TASKS_URI)
@Api(value = ReindexTaskController.REINDEX_TASKS_URI)
public class ReindexTaskController {

    public static final String REINDEX_TASKS_URI = "/elastic-support/reindex/tasks";

    private final ReindexService reindexService;

    @Autowired
    public ReindexTaskController(ReindexService reindexService) {
        this.reindexService = reindexService;
    }

    @GetMapping
    @ApiOperation(value = "Get all reindex tasks, optionally by case type",
        response = ReindexTask.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved reindex tasks"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<Page<ReindexTask>> getReindexTasksByCaseType(
        @RequestParam(value = "caseType", required = false) String caseType,
        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
        @RequestParam(value = "size", required = false, defaultValue = "25") Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        Page<ReindexTask> response = reindexService.getTasksByCaseType(caseType, pageable);
        return ResponseEntity.ok(response);
    }
}
