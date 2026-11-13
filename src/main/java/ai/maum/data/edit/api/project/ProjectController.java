package ai.maum.data.edit.api.project;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ai.maum.data.edit.api.project.dto.response.ProjectInfoResponse;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@Tag(name = "프로젝트")
public class ProjectController {

  private final ProjectService projectService;

  /**
   * 프로젝트 가공 데이터를 추출한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param projectId   프로젝트 ID
   * @param request     요청 정보
   * @return 프로젝트 가공 데이터
   */
  @PostMapping("/extract/{workspace_id}/{project_id}")
  @Operation(summary = "프로젝트 가공 데이터 추출",
      description = "프로젝트 가공 데이터를 추출한다." +
          "\n### 응답 코드" +
          "\n- P017 : 추출한 데이터를 저장할 수 없음" +
          "\n- P018 : CSV 정보가 존재하지 않음")
  public BaseResponse<String> extract(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @Parameter(description = "프로젝트 ID", example = "507f1f77bcf86cd799439012") @PathVariable("project_id") String projectId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(title = "추출할 작업 상태 목록",
          description = "추출할 작업 상태 목록",
          example = "[\"AS\", \"CM\"]")))
      @RequestBody List<String> request
  ) {
    return BaseResponse.success(
        projectService.extract(new ObjectId(workspaceId), new ObjectId(projectId), request, false));
  }

  /**
   * 프로젝트 정보를 조회한다.
   *
   * @param projectId 프로젝트 ID
   * @return 프로젝트 정보
   */
  @GetMapping("/find")
  @Operation(summary = "프로젝트 정보 조회", description = "프로젝트 정보를 조회한다.")
  public BaseResponse<ProjectInfoResponse> findProjectInfo(
      @Parameter(description = "프로젝트 ID", example = "507f1f77bcf86cd799439012") @RequestParam("project_id") String projectId,
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439012") @RequestParam("workspace_id") String workspaceId
  ) {
    return BaseResponse.success(
        projectService.findProjectInfo(new ObjectId(workspaceId), new ObjectId(projectId)));
  }
}
