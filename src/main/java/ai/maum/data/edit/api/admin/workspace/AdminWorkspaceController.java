package ai.maum.data.edit.api.admin.workspace;

import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceAssignRequest;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceDeleteRequest;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceRegisterRequest;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceUnassignRequest;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceUpdateRequest;
import ai.maum.data.edit.api.admin.workspace.dto.response.AdminWorkspaceLatestUploadResponse;
import ai.maum.data.edit.api.admin.workspace.dto.response.AdminWorkspaceListResponse;
import ai.maum.data.edit.api.admin.workspace.dto.response.AdminWorkspaceResponse;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/workspace")
@RequiredArgsConstructor
@Secured({"ROLE_ADMIN"})
@Tag(name = "관리자 워크스페이스")
public class AdminWorkspaceController {

  private final AdminWorkspaceService adminWorkspaceService;

  /**
   * 워크스페이스 작업자를 등록한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param request     요청 정보
   */
  @PostMapping("/assign/{workspace_id}")
  @Operation(summary = "워크스페이스 작업자 등록",
      description = "워크스페이스 작업자를 등록한다." +
          "\n### 응답 코드" +
          "\n- F006 : 요청 파라미터가 존재하지 않거나 잘못됨" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음")
  public BaseResponse<Void> assignMember(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @RequestBody AdminWorkspaceAssignRequest request
  ) {
    adminWorkspaceService.assignMember(new ObjectId(workspaceId), request);
    return BaseResponse.success();
  }

  /**
   * 해당 워크스페이스 이름이 이미 등록되었는지 확인한다.
   *
   * @param name 워크스페이스명
   * @return 워크스페이스명 중복 결과
   */
  @GetMapping("/name/check")
  @Operation(summary = "워크스페이스명 중복 확인",
      description = "해당 워크스페이스 이름이 이미 등록되었는지 확인한다.\n\n" +
          "중복: true, 중복 아님: false")
  public BaseResponse<Boolean> checkWorkspaceName(
      @Parameter(description = "워크스페이스명", example = "워크스페이스1") String name
  ) {
    return BaseResponse.success(adminWorkspaceService.checkWorkspaceName(
        URLDecoder.decode(name, StandardCharsets.UTF_8)));
  }

  /**
   * 하나 이상의 워크스페이스들을 삭제한다.
   *
   * @param request 요청 정보
   */
  @PostMapping("/delete")
  @Operation(summary = "워크스페이스 삭제",
      description = "하나 이상의 워크스페이스들을 삭제한다." +
          "\n### 응답 코드" +
          "\n- W000 : 워크스페이스가 존재하지 않음" +
          "\n- W001 : 워크스페이스에 진행 중인 프로젝트 존재함")
  public BaseResponse<Void> deleteWorkspace(@RequestBody AdminWorkspaceDeleteRequest request
  ) {
    adminWorkspaceService.deleteWorkspace(request);
    return BaseResponse.success();
  }

  /**
   * 최근 업로드 완료 정보를 조회한다.
   *
   * @return 최근 업로드 완료 정보
   */
  @GetMapping("/upload/latest")
  @Operation(summary = "최근 업로드 완료 정보 조회",
      description = "최근 업로드 완료 정보를 조회한다.")
  public BaseResponse<AdminWorkspaceLatestUploadResponse> getLatestUploadInfo() {
    return BaseResponse.success(adminWorkspaceService.getLatestUploadInfo());
  }

  /**
   * 워크스페이스를 조회한다.
   *
   * @param workspaceId 워크스페이스 ID
   */
  @GetMapping("/{workspace_id}")
  @Operation(summary = "워크스페이스 조회",
      description = "워크스페이스를 조회한다." +
          "\n### 응답 코드" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음")
  public BaseResponse<AdminWorkspaceResponse> getWorkspace(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId) {
    return BaseResponse.success(adminWorkspaceService.getWorkspace(new ObjectId(workspaceId)));
  }

  /**
   * 워크스페이스 목록 및 개수를 조회한다.
   *
   * @param page      페이지 번호
   * @param status    워크스페이스 상태
   * @param query     검색어
   * @param filter    검색조건
   * @param startDate 시작일
   * @param endDate   종료일
   * @return 워크스페이스 목록 및 개수
   */
  @GetMapping("/list/{page}")
  @Operation(summary = "워크스페이스 목록 조회",
      description = "워크스페이스 목록 및 개수를 조회한다." +
          "\n검색어를 빈 값으로 입력할 경우 전체에 대해 조회한다.")
  public BaseResponse<AdminWorkspaceListResponse> getWorkspaceList(
      @Parameter(description = "페이지 번호", example = "1") @PathVariable int page,
      @Parameter(description = "워크스페이스 상태(전체: FILTER_WS_ST_ALL, 공개 전: FILTER_WS_ST_PENDING, 공개 중: FILTER_WS_ST_PROGRESS, 기간 만료: FILTER_WS_ST_EXPIRE)", example = "FILTER_WS_ST_ALL") String status,
      @Parameter(description = "검색어", example = "1번") @RequestParam(defaultValue = "") @Nullable String query,
      @Parameter(description = "검색조건(워크스페이스명: FILTER_WS_CATEGORY_NAME, 워크스페이스 소개: FILTER_WS_CATEGORY_DESC, 담당자: FILTER_WS_CATEGORY_MANAGER", example = "FILTER_WS_CATEGORY_NAME") String filter,
      @Parameter(description = "시작일", example = "2022-05-01") @RequestParam("start_date") @Nullable String startDate,
      @Parameter(description = "종료일", example = "2023-06-30") @RequestParam("end_date") @Nullable String endDate
  ) {
    return BaseResponse.success(adminWorkspaceService.getWorkspaceList(
        page,
        filter,
        query,
        status,
        startDate == null ? null : LocalDate.parse(startDate),
        endDate == null ? null : LocalDate.parse(endDate)));
  }

  /**
   * 워크스페이스를 등록한다.
   *
   * @param request 요청 정보
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "워크스페이스 등록",
      description = "워크스페이스를 등록한다." +
          "\n### 응답 코드" +
          "\n- F004 : 파일 업로드 중 오류 발생")
  public BaseResponse<ObjectId> registerWorkspace(@ModelAttribute AdminWorkspaceRegisterRequest request) {
    return BaseResponse.success(adminWorkspaceService.registerWorkspace(request));
  }

  /**
   * 워크스페이스 작업자 등록을 취소한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param request     요청 정보
   */
  @PostMapping("/unassign/{workspace_id}")
  @Operation(summary = "워크스페이스 작업자 등록 취소",
      description = "워크스페이스 작업자 등록을 취소한다." +
          "\n### 응답 코드" +
          "\n- F006 : 요청 파라미터가 존재하지 않거나 잘못됨" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음" +
          "\n- W003 : 작업 중인 프로젝트가 존재하여 등록 취소할 수 없음")
  public BaseResponse<Void> unassignMember(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @RequestBody AdminWorkspaceUnassignRequest request
  ) {
    adminWorkspaceService.unassignMember(new ObjectId(workspaceId), request);
    return BaseResponse.success();
  }

  /**
   * 워크스페이스를 수정한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param request     요청 정보
   */
  @PutMapping(path = "/update/{workspace_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "워크스페이스 수정",
      description = "워크스페이스를 수정한다." +
          "\n### 응답 코드" +
          "\n- F004 : 파일 업로드 중 오류 발생" +
          "\n- F005 : 파일 삭제 중 오류 발생")
  public BaseResponse<Void> updateWorkspace(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @ModelAttribute AdminWorkspaceUpdateRequest request) {
    adminWorkspaceService.updateWorkspace(new ObjectId(workspaceId), request);
    return BaseResponse.success();
  }
}
