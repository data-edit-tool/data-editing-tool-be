package ai.maum.data.edit.api.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ai.maum.data.edit.api.admin.request.AdminCreateRequest;
import ai.maum.data.edit.api.admin.request.AdminUpdatePasswordRequest;
import ai.maum.data.edit.api.admin.response.AdminRecoveryResponse;
import ai.maum.data.edit.api.admin.validator.AdminValidator;
import ai.maum.data.edit.api.user.dto.request.LoginRequest;
import ai.maum.data.edit.api.user.dto.response.LoginResponse;
import ai.maum.data.edit.common.config.annotation.NoToken;
import ai.maum.data.edit.common.enums.SecurityMeta;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "관리자")
public class AdminController {

  private final AdminService adminService;

  @PostMapping
  @Operation(summary = "관리자 계정 생성")
  @NoToken
  public BaseResponse<Void> create(@RequestBody AdminCreateRequest request) {
    AdminValidator.validate(request);

    adminService.create(request);
    return BaseResponse.success();
  }

  @GetMapping(value = "/exist")
  @Operation(summary = "관리자 계정 존재 여부 조회")
  @NoToken
  public BaseResponse<Boolean> getAdminExist() {
    return BaseResponse.success(adminService.getAdminExist());
  }

  @PostMapping("/login")
  @Operation(summary = "로그인",
      description = "로그인을 수행한다." +
          "\n### 응답 코드" +
          "\n- F000 : 정상적인 응답으로써 TOKEN 발부",
      security = {@SecurityRequirement(name = SecurityMeta.DOCUMENT_NO_TOKEN)})
  @NoToken
  public BaseResponse<LoginResponse> login(@RequestBody LoginRequest request) {
    return BaseResponse.success(adminService.login(request));
  }

  @PutMapping("/password")
  @Operation(summary = "관리자 계정 비밀번호 변경")
  @NoToken
  public BaseResponse<Void> updatePassword(@RequestBody AdminUpdatePasswordRequest request) {
    adminService.updatePassword(request);

    return BaseResponse.success();
  }

  @GetMapping(value = "/recovery-code/verify")
  @Operation(summary = "관리자 계정 복구 코드 검증")
  @NoToken
  public BaseResponse<AdminRecoveryResponse> verifyRecoveryCode(@RequestParam("recovery_code") String recoveryCode) {
    return BaseResponse.success(adminService.verifyRecoveryCode(recoveryCode));
  }
}
