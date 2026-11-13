package ai.maum.data.edit.api.admin.project;

import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import ai.maum.data.edit.api.admin.project.dto.request.AdminProjectDeleteRequest;
import ai.maum.data.edit.api.admin.project.dto.request.AdminProjectRegisterRequest;
import ai.maum.data.edit.api.admin.project.dto.request.AdminProjectReuploadRequest;
import ai.maum.data.edit.api.admin.project.dto.response.AdminProjectFileListResponse;
import ai.maum.data.edit.api.admin.project.dto.response.AdminProjectListResponse;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceUpdatePostStatusRequest;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/project")
@RequiredArgsConstructor
@Secured({"ROLE_ADMIN"})
@Tag(name = "관리자 프로젝트")
public class AdminProjectController {
  private final AdminProjectService adminProjectService;

  /**
   * 프로젝트를 삭제한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param request     요청 정보
   */
  @PostMapping("/delete/{workspace_id}")
  @Operation(summary = "프로젝트 삭제",
      description = "프로젝트를 삭제한다." +
          "\n### 응답 코드" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음" +
          "\n- P000 : 해당 프로젝트가 존재하지 않음" +
          "\n- P006 : 게시 중인 프로젝트가 존재하여 삭제할 수 없음")
  public BaseResponse<Void> deleteProject(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @RequestBody AdminProjectDeleteRequest request
  ) {
    adminProjectService.deleteProject(new ObjectId(workspaceId), request);
    return BaseResponse.success();
  }

  /**
   * 프로젝트 업로드 상태 정보 목록을 조회한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param projectId   프로젝트 ID
   * @param page        페이지 번호
   * @return 업로드 상태 정보 목록
   */
  @GetMapping("/file/list/{workspace_id}/{project_id}/{page}")
  @Operation(summary = "프로젝트 업로드 상태 정보 목록 조회", description = "프로젝트 업로드 상태 정보 목록을 조회한다.")
  public BaseResponse<AdminProjectFileListResponse> getFileList(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @Parameter(description = "프로젝트 ID", example = "507f1f77bcf86cd799439012") @PathVariable("project_id") String projectId,
      @Parameter(description = "페이지 번호", example = "1") @PathVariable int page
  ) {
    return BaseResponse.success(adminProjectService.getFileList(
        new ObjectId(workspaceId),
        new ObjectId(projectId),
        page));
  }

  /**
   * 프로젝트 목록 및 개수를 조회한다.
   *
   * @param workspaceId   워크스페이스 ID
   * @param page          페이지 번호
   * @param toolType      프로젝트 가공 유형
   * @param uploadStatus  업로드 상태 코드
   * @param projectStatus 프로젝트 상태 코드
   * @param postStatus    게시 상태 코드
   * @param query         검색어
   * @param startDate     시작일
   * @param endDate       종료일
   * @return 프로젝트 목록 및 개수
   */
  @GetMapping("/list/{workspace_id}/{page}")
  @Operation(summary = "프로젝트 목록 조회",
      description = "프로젝트 목록 및 개수를 조회한다." +
          "\n### 응답 코드" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음")
  public BaseResponse<AdminProjectListResponse> getProjectList(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @Parameter(description = "페이지 번호", example = "1") @PathVariable int page,
      @Parameter(description = "프로젝트 가공 유형(전체: FILTER_PJ_TOOL_TYPE_ALL, 복합대화 구간 태깅: FILTER_PJ_TOOL_TYPE_CC)", example = "FILTER_PJ_TOOL_TYPE_ALL") @RequestParam("tool_type") String toolType,
      @Parameter(description = "업로드 상태 코드(전체: FILTER_PJ_UPLOAD_STATUS_ALL, 성공: FILTER_PJ_UPLOAD_STATUS_SUCCESS, 진행 중: FILTER_PJ_UPLOAD_STATUS_PROGRESS, 실패: FILTER_PJ_UPLOAD_STATUS_ERROR)", example = "FILTER_PJ_UPLOAD_STATUS_ALL") @RequestParam("upload_status") String uploadStatus,
      @Parameter(description = "프로젝트 상태 코드(전체: FILTER_PJ_PROJECT_STATUS_ALL, 대기 중: FILTER_PJ_PROJECT_STATUS_PENDING, 진행 중: FILTER_PJ_PROJECT_STATUS_WORKING, 완료: FILTER_PJ_PROJECT_STATUS_FINISH)", example = "FILTER_PJ_PROJECT_STATUS_ALL") @RequestParam("project_status") String projectStatus,
      @Parameter(description = "게시 상태 코드(전체: FILTER_PJ_POST_STATUS_ALL, 게시 중: FILTER_PJ_POST_STATUS_ON, 일시정지: FILTER_PJ_POST_STATUS_OFF)", example = "FILTER_PJ_POST_STATUS_ALL") @RequestParam("post_status") String postStatus,
      @Parameter(description = "검색어", example = "1번") @RequestParam(defaultValue = "") @Nullable String query,
      @Parameter(description = "시작일", example = "2022-05-01") @RequestParam("start_date") @Nullable String startDate,
      @Parameter(description = "종료일", example = "2023-06-30") @RequestParam("end_date") @Nullable String endDate
  ) {
    return BaseResponse.success(adminProjectService.getProjectList(
        new ObjectId(workspaceId),
        page,
        toolType,
        uploadStatus,
        projectStatus,
        postStatus,
        query,
        startDate == null ? null : LocalDate.parse(startDate),
        endDate == null ? null : LocalDate.parse(endDate)));
  }

  /**
   * 프로젝트를 등록한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param request     요청 정보
   */
  @PostMapping("/{workspace_id}")
  @Operation(summary = "프로젝트 등록",
      description = "프로젝트를 등록한다." +
          "\n### 응답 코드" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음" +
          "\n- P001 : 프로젝트명이 잘못됨" +
          "\n- P002 : 발화자 정보가 잘못됨" +
          "\n- P003 : 프로젝트에 업로드할 데이터 파일 정보가 존재하지 않음" +
          "\n- P004 : 프로젝트에 업로드할 발화자 이미지 파일 정보가 존재하지 않음" +
          "\n- P005 : 이미 존재하는 프로젝트가 있음" +
          "\n- P009 : 파일명이 잘못됨" +
          "\n- P014 : 프로젝트에 업로드할 STT 음성 파일 정보가 존재하지 않음")
  public BaseResponse<ObjectId> registerProject(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @RequestBody AdminProjectRegisterRequest request
  ) {
    return BaseResponse.success(adminProjectService.registerProject(new ObjectId(workspaceId), request));
  }

  /**
   * 프로젝트에 업로드 실패한 파일을 재업로드한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param projectId   프로젝트 ID
   * @param request     요청 정보
   */
  @PostMapping(path = "/file/reupload/{workspace_id}/{project_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "프로젝트 파일 재업로드",
      description = "프로젝트에 업로드 실패한 파일을 재업로드한다." +
          "\n### 응답 코드" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음" +
          "\n- P000 : 해당 프로젝트가 존재하지 않음" +
          "\n- P010 : 파일 종류가 잘못됨" +
          "\n- P016 : 해당 파일을 재업로드할 수 없음")
  public BaseResponse<Void> reupload(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @Parameter(description = "프로젝트 ID", example = "507f1f77bcf86cd799439012") @PathVariable("project_id") String projectId,
      @ModelAttribute AdminProjectReuploadRequest request
  ) {
    adminProjectService.reupload(
        new ObjectId(workspaceId),
        new ObjectId(projectId),
        request);
    return BaseResponse.success();
  }

  /**
   * 하나 이상의 프로젝트를 게시하거나 일시정지한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param request     요청 정보
   */
  @PostMapping("/post-status/{workspace_id}")
  @Operation(summary = "프로젝트 게시 상태 갱신",
      description = "하나 이상의 프로젝트를 게시하거나 일시정지한다." +
          "\n### 응답 코드" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음" +
          "\n- P000 : 해당 프로젝트가 존재하지 않음" +
          "\n- P015 : 프로젝트가 존재하지 않거나 업로드가 완료되지 않아 게시 상태를 갱신할 수 없음")
  public BaseResponse<Void> updatePostStatus(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @RequestBody AdminWorkspaceUpdatePostStatusRequest request
  ) {
    adminProjectService.updatePostStatus(new ObjectId(workspaceId), request);
    return BaseResponse.success();
  }

  /**
   * 프로젝트에 등록할 파일을 업로드한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param projectId   프로젝트 ID
   * @param file        업로드할 파일
   */
  @PostMapping(path = "/file/upload/{workspace_id}/{project_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "프로젝트 파일 업로드",
      description = "프로젝트에 등록할 파일을 업로드한다." +
          "\n### 응답 코드" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음" +
          "\n- P000 : 해당 프로젝트가 존재하지 않음" +
          "\n- P008 : 프로젝트에 업로드 대기 중인 해당 파일이 존재하지 않음" +
          "\n- P009 : 파일명이 잘못됨" +
          "\n- P010 : 파일 종류가 잘못됨" +
          "\n- P011 : 해당 파일에 대한 발화자 데이터가 존재하지 않음" +
          "\n- P012 : 해당 파일에 대한 엔진 데이터가 존재하지 않음" +
          "\n- P013 : 파일이 잘못됨")
  public BaseResponse<Void> uploadFile(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @Parameter(description = "프로젝트 ID", example = "507f1f77bcf86cd799439012") @PathVariable("project_id") String projectId,
      @RequestPart MultipartFile file
  ) {
    adminProjectService.uploadFile(
        new ObjectId(workspaceId),
        new ObjectId(projectId),
        file);
    return BaseResponse.success();
  }
}
