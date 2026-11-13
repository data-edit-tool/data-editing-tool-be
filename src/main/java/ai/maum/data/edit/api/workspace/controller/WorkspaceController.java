package ai.maum.data.edit.api.workspace.controller;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ai.maum.data.edit.api.workspace.dto.response.GuideResponse;
import ai.maum.data.edit.api.workspace.dto.response.ProjectListResponse;
import ai.maum.data.edit.api.workspace.dto.response.WorkspaceListResponse;
import ai.maum.data.edit.api.workspace.service.WorkspaceService;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "워크스페이스", description = "workspace API")
@RestController
@RequestMapping("/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

  private final WorkspaceService workspaceService;

  /**
   * 가이드 다운로드
   *
   * @param workspaceId 워크스페이스 ID
   */
  @GetMapping("/guide/list")
  @Operation(summary = "가이드 목록", description = "첨부되어 있는 가이드를 목록을 조회한다.")
  public BaseResponse<GuideResponse> downloadGuide(@RequestParam("workspace_id") String workspaceId) {
    return BaseResponse.success(workspaceService.findGuidesList(new ObjectId(workspaceId)));
  }

  /**
   * 워크스페이스에 존재하는 프로젝트들의 가공 데이터를 추출한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param request     요청 정보
   * @return 워크스페이스 가공 데이터
   */
  @PostMapping("/extract/{workspace_id}")
  @Operation(summary = "워크스페이스 가공 데이터 추출",
      description = "워크스페이스에 있는 모든 프로젝트의 가공 데이터를 추출한다." +
          "\n### 응답 코드" +
          "\n- W000 : 워크스페이스가 존재하지 않음" +
          "\n- P017 : 추출한 데이터를 저장할 수 없음" +
          "\n- P018 : CSV 정보가 존재하지 않음")
  public BaseResponse<String> extract(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(title = "추출할 작업 상태 목록",
          description = "추출할 작업 상태 목록",
          example = "[\"AS\", \"CM\"]")))
      @RequestBody List<String> request
  ) {
    return BaseResponse.success(workspaceService.extract(new ObjectId(workspaceId), request));
  }

  @GetMapping("/find")
  @Operation(summary = "워크스페이스 조회", description = "워크스페이스 상세 조회")
  public BaseResponse<ProjectListResponse> findWorkspace(@RequestParam("workspace_id") String workspaceId) {
    return BaseResponse.success(workspaceService.findWorkspace(new ObjectId(workspaceId)));
  }

  @GetMapping("/list")
  @Operation(summary = "워크스페이스 목록 조회", description = "워크스페이스 목록 조회")
  public BaseResponse<List<WorkspaceListResponse>> workspaceList(
      @RequestParam(value = "workspace_name", required = false) String workspaceName,
      @RequestParam(value = "workspace_intro", required = false) String workspaceIntro,
      @RequestParam(value = "incharge", required = false) String incharge,
      @RequestParam(value = "start_date", required = false) String startDate,
      @RequestParam(value = "end_date", required = false) String endDate) {
    return BaseResponse.success(
        workspaceService.workspaceList(workspaceName, workspaceIntro, incharge, startDate, endDate));
  }
}
