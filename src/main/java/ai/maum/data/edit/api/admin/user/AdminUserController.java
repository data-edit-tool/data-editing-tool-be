package ai.maum.data.edit.api.admin.user;

import org.bson.types.ObjectId;
import org.springframework.lang.Nullable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import ai.maum.data.edit.api.admin.user.dto.request.AdminUserDeleteRequest;
import ai.maum.data.edit.api.admin.user.dto.request.AdminUserRegisterRequest;
import ai.maum.data.edit.api.admin.user.dto.response.AdminUserListForWorkspaceAssignmentResponse;
import ai.maum.data.edit.api.admin.user.dto.response.AdminUserListResponse;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Tag(name = "관리자 사용자")
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping("/check")
  @Operation(summary = "사용자 계정 중복 확인",
      description = "사용자 계정이 이미 등록되었는지 확인한다.\n\n" +
          "중복: true, 중복 아님: false")
  @Secured({"ROLE_ADMIN"})
  public BaseResponse<Boolean> checkAccount(
      @Parameter(description = "사용자 계정", example = "worker001") String account
  ) {
    return BaseResponse.success(adminUserService.checkUserAccount(account));
  }

  @PostMapping("/delete")
  @Operation(summary = "사용자 삭제",
      description = "하나 이상의 사용자들을 삭제한다." +
          "\n### 응답 코드" +
          "\n- U000 : 일치하는 사용자 정보가 없음" +
          "\n- U002 : 진행 중인 프로젝트가 존재하여 삭제 불가" +
          "\n- U004 : 관리자 계정은 수행 불가")
  @Secured({"ROLE_ADMIN"})
  public BaseResponse<Void> deleteUser(@RequestBody AdminUserDeleteRequest request
  ) {
    adminUserService.deleteUser(request);
    return BaseResponse.success();
  }

  /**
   * 워크스페이스에 작업자별 지정된 사용자 목록을 조회한다.
   * 작업자 종류가 null일 경우 지정된 전체 사용자 목록을 조회한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param page        페이지 번호
   * @param type        작업자 종류, Nullable
   * @param query       검색어, Nullable
   * @return 작업자별 사용자 목록
   */
  @GetMapping("/list/assign/{workspace_id}/{page}")
  @Operation(summary = "워크스페이스 작업자별 지정된 사용자 목록 조회",
      description = "워크스페이스 작업자별 지정된 사용자 목록을 조회한다." +
          "\n작업자 종류를 빈 값으로 입력할 경우 전체 종류에 대해 조회한다." +
          "\n검색어를 빈 값으로 입력할 경우 전체에 대해 조회한다." +
          "\n### 응답 코드" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음")
  @Secured({"ROLE_ADMIN"})
  public BaseResponse<AdminUserListForWorkspaceAssignmentResponse> getAssignedUserListForWorkspaceAssignment(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @Parameter(description = "페이지 번호", example = "1") @PathVariable int page,
      @Parameter(description = "작업자 종류(작업자: W, 검수자: C, 뷰어: V)", example = "C") @Nullable Character type,
      @Parameter(description = "검색어", example = "checker00") @RequestParam(defaultValue = "") @Nullable String query
  ) {
    return BaseResponse.success(adminUserService.getAssignedUserListForWorkspaceAssignment(
        new ObjectId(workspaceId), page, type, query));
  }

  /**
   * 워크스페이스에 지정 가능한 사용자 목록을 조회한다.
   *
   * @param workspaceId 워크스페이스 ID
   * @param page        페이지 번호
   * @param query       검색어, Nullable
   * @return 작업자별 사용자 목록
   */
  @GetMapping("/list/available/{workspace_id}/{page}")
  @Operation(summary = "워크스페이스 지정 가능한 사용자 목록 조회",
      description = "워크스페이스에 지정 가능한 사용자 목록을 조회한다." +
          "\n검색어를 빈 값으로 입력할 경우 전체에 대해 조회한다." +
          "\n### 응답 코드" +
          "\n- W000 : 해당 워크스페이스가 존재하지 않음")
  @Secured({"ROLE_ADMIN"})
  public BaseResponse<AdminUserListForWorkspaceAssignmentResponse> getAvailableUserListForWorkspaceAssignment(
      @Parameter(description = "워크스페이스 ID", example = "507f1f77bcf86cd799439011") @PathVariable("workspace_id") String workspaceId,
      @Parameter(description = "페이지 번호", example = "1") @PathVariable int page,
      @Parameter(description = "검색어", example = "checker00") @RequestParam(defaultValue = "") @Nullable String query
  ) {
    return BaseResponse.success(adminUserService.getAvailableUserListForWorkspaceAssignment(
        new ObjectId(workspaceId), page, query));
  }

  /**
   * 사용자 목록 및 작업자 인원 수를 조회한다.
   *
   * @param page      페이지 번호
   * @param filter    검색조건
   * @param query     검색어
   * @param startDate 시작일
   * @param endDate   종료일
   * @return 사용자 목록 및 작업자 인원 수
   */
  @GetMapping("/list/{page}")
  @Operation(summary = "사용자 목록 조회", description = "사용자 목록 및 작업자 인원 수를 조회한다.")
  @Secured({"ROLE_ADMIN"})
  public BaseResponse<AdminUserListResponse> getUserList(
      @Parameter(description = "페이지 번호", example = "1") @PathVariable int page,
      @Parameter(description = "검색조건(사용자 ID: FILTER_US_CATEGORY_ID, 사용자 설명: FILTER_US_CATEGORY_DESC)", example = "FILTER_US_CATEGORY_ID") String filter,
      @Parameter(description = "검색어", example = "worker") @RequestParam(defaultValue = "") @Nullable String query,
      @Parameter(description = "시작일", example = "2022-05-01") @RequestParam("start_date") @Nullable String startDate,
      @Parameter(description = "종료일", example = "2023-06-30") @RequestParam("end_date") @Nullable String endDate
  ) {
    return BaseResponse.success(adminUserService.getUserList(
        page,
        filter,
        query,
        startDate == null ? null : LocalDate.parse(startDate),
        endDate == null ? null : LocalDate.parse(endDate).plusDays(1)));
  }

  /**
   * 사용자 비밀번호를 초기화한다.
   *
   * @param userId 사용자 ID
   */
  @PostMapping("/password/init/{user_id}")
  @Operation(summary = "사용자 비밀번호 초기화",
      description = "사용자 비밀번호를 초기화한다." +
          "\n### 응답 코드" +
          "\n- U000 : 일치하는 사용자 정보가 없음" +
          "\n- U004 : 관리자 계정은 수행 불가")
  @Secured({"ROLE_ADMIN"})
  public BaseResponse<Void> initPassword(
      @Parameter(description = "사용자 ID", example = "507f1f77bcf86cd799439011") @PathVariable("user_id") String userId
  ) {
    adminUserService.initPassword(new ObjectId(userId));
    return BaseResponse.success();
  }

  /**
   * 사용자를 등록한다.
   *
   * @param request 요청 정보
   */
  @PostMapping
  @Operation(summary = "사용자 등록",
      description = "사용자를 등록한다." +
          "\n### 응답 코드" +
          "\n- U001 : 사용자 계정이 이미 존재함")
  @Secured({"ROLE_ADMIN"})
  public BaseResponse<Void> registerUser(@RequestBody AdminUserRegisterRequest request) {
    adminUserService.registerUser(request);

    return BaseResponse.success();
  }
}
