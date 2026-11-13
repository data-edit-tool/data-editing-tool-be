package ai.maum.data.edit.api.job.controller;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ai.maum.data.edit.api.job.dto.request.StartJobRequest;
import ai.maum.data.edit.api.job.dto.response.JobListResponse;
import ai.maum.data.edit.api.job.dto.response.ViewerJobDetailResponse;
import ai.maum.data.edit.api.job.service.JobService;
import ai.maum.data.edit.api.workspace.field.labeling.ComplexConversation;
import ai.maum.data.edit.api.workspace.field.labeling.Labeling;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "작업", description = "job API")
@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {

  private final JobService jobService;

  @GetMapping("/findLabeling")
  @Operation(summary = "작업과 가공 작업 레이블링 데이터 조회",
      description = "작업과 가공 작업 레이블링 데이터를 조회한다." +
          "\n### 요청 파라미터" +
          "\n- step : 1=verbal / 2=paraverbal / 3=nonverbal / default=works.step" +
          "<br>작업 화면 진입 시 step을 빈 값으로 보내면 작업 중이던 step의 데이터가 출력됨." +
          "<br>step은 작업 화면에서 '이전/다음 step' 버튼을 이용해 화면 이동할 때 사용되는 파라미터." +
          "\n" +
          "\n### 응답 work_status 코드 설명" +
          "\n- RD: 작업 전(최초 생성 시의 상태)" +
          "\n- AS: 작업 중" +
          "\n- RQ: 검수 요청" +
          "\n- IS: 검수 중" +
          "\n- RJ: 반려" +
          "\n- IM: 작업 불가" +
          "\n- IC: 작업불가 검수 중" +
          "\n- NU: 작업 불가 승인" +
          "\n- CM: 최종 승인"
  )
  public BaseResponse<Labeling> findLabeling(
      @RequestParam("project_id") String projectId,
      @RequestParam(defaultValue = "0") int step,
      @RequestParam(value = "job_id", required = false, defaultValue = "") String jobId) {
    return BaseResponse.success(jobService.findLabeling(new ObjectId(projectId), step, new ObjectId(jobId)));
  }

  @GetMapping("/viewer")
  public BaseResponse<List<ViewerJobDetailResponse>> getViewerJobDetail(
      @RequestParam("project_id") String projectId, @RequestParam("job_id") String jobId
  ) {
    return BaseResponse.success(jobService.getViewerJobDetail(new ObjectId(projectId), new ObjectId(jobId)));
  }

  /**
   * 작업 목록을 조회한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param projectId   프로젝트 ID
   * @return 작업 목록
   */
  @GetMapping("/list")
  @Operation(summary = "작업 목록 조회", description = "작업 목록을 조회한다.")
  public BaseResponse<JobListResponse> getJobList(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @RequestParam("workspace_id") String workspaceId,
      @Parameter(description = "프로젝트 ID", example = "507f1f77bcf86cd799439012") @RequestParam("project_id") String projectId) {
    return BaseResponse.success(jobService.getJobList(new ObjectId(projectId)));
  }

  /**
   * @param request 요청 파라미터
   */
  @PostMapping("/start")
  @Operation(summary = "가공 작업 시작", description = "작업에 대한 데이터 레이블링 작업을 시작한다.")
  synchronized public BaseResponse<ComplexConversation> startJob(@RequestBody StartJobRequest request) {
    return BaseResponse.success(jobService.startJob(request));
  }
}
