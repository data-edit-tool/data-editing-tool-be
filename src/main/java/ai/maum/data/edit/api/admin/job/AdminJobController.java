package ai.maum.data.edit.api.admin.job;

import org.bson.types.ObjectId;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ai.maum.data.edit.api.admin.job.request.AdminJobAssignableUserListRequest;
import ai.maum.data.edit.api.admin.job.request.AdminJobWorkerUpdateRequest;
import ai.maum.data.edit.api.admin.job.response.AdminJobAssignableUserResponse;
import ai.maum.data.edit.api.admin.job.response.AdminJobResponse;
import ai.maum.data.edit.common.response.BaseResponse;
import ai.maum.data.edit.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/jobs")
@Secured({"ROLE_ADMIN"})
@Tag(name = "관리자 작업(subset)")
public class AdminJobController {

  private final AdminJobService adminJobService;

  @GetMapping("/assignable-users")
  @Operation(summary = "작업에 할당 가능한 사용자 목록 조회")
  public BaseResponse<PageResponse<AdminJobAssignableUserResponse>> getAssignableUsers(
      @ParameterObject AdminJobAssignableUserListRequest request) {
    return BaseResponse.success(adminJobService.getAssignableUsers(request));
  }

  @GetMapping
  @Operation(summary = "작업 목록 조회")
  public BaseResponse<List<AdminJobResponse>> getJobs(@RequestParam("project_id") String projectId) {
    return BaseResponse.success(adminJobService.getJobs(new ObjectId(projectId)));
  }

  @PostMapping("/{job_id}/worker")
  @Operation(summary = "작업자 등록/변경/해제")
  public BaseResponse<Void> updateWorker(@PathVariable("job_id") String jobId,
      @RequestBody AdminJobWorkerUpdateRequest request) {
    adminJobService.updateWorker(new ObjectId(jobId), request);

    return BaseResponse.success();
  }
}
