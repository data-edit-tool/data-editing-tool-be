package ai.maum.data.edit.api.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.maum.data.edit.api.user.dto.request.LoginRequest;
import ai.maum.data.edit.api.user.dto.request.PasswordInitRequest;
import ai.maum.data.edit.api.user.dto.request.ReissueRequest;
import ai.maum.data.edit.api.user.dto.response.LoginResponse;
import ai.maum.data.edit.api.user.dto.response.ReissueResponse;
import ai.maum.data.edit.common.config.annotation.NoToken;
import ai.maum.data.edit.common.enums.ResponseMeta;
import ai.maum.data.edit.common.enums.SecurityMeta;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "사용자")
public class UserController {

  private final UserService userService;

  /**
   * 사용자의 토큰이 유효한지 검증한다.
   * 토큰 유효성 검증은 필터에서 수행되기 때문에 로직이 없는 호출용 API이다.
   */
  @GetMapping("/token/check")
  @Operation(summary = "토큰 유효성 검증", description = "사용자의 토큰이 유효한지 검증한다.")
  public BaseResponse<Void> checkToken() {
    return BaseResponse.success();
  }

  /**
   * @param request 비밀번호를 변경하기 위한 정보
   */
  @PutMapping("/initPassword")
  @Operation(summary = "비밀번호 초기화",
      description = "최초 로그인 시 비밀번호를 초기화한다.",
      security = {@SecurityRequirement(name = SecurityMeta.DOCUMENT_NO_TOKEN)})
  @NoToken
  public BaseResponse<Boolean> initPassword(@RequestBody PasswordInitRequest request) {
    return BaseResponse.success(userService.initPassword(request));
  }

  /**
   * 사용자 계정과 암호화된 비밀번호를 받아 로그인을 수행한다.
   *
   * @param request 사용자 정보
   * @return 로그인 결과 응답
   */
  @PostMapping("/login")
  @Operation(summary = "로그인",
      description = "로그인을 수행한다." +
          "\n### 응답 코드" +
          "\n- F000 : 정상적인 응답으로써 TOKEN 발부" +
          "\n- U001 : 비밀번호 초기화를 요구하는 코드",
      security = {@SecurityRequirement(name = SecurityMeta.DOCUMENT_NO_TOKEN)})
  @NoToken
  public BaseResponse<LoginResponse> login(@RequestBody LoginRequest request) {
    // 비밀번호 초기화 진행여부 확인
    userService.validateInitPassword(request);
    return BaseResponse.success(userService.login(request));
  }

  /**
   * 로그아웃을 수행한다.
   */
  @PostMapping("/logout")
  @Operation(summary = "로그아웃",
      description = "로그아웃을 수행한다." +
          "\n### 응답 코드" +
          "\n- F000 : 정상 응답")
  public BaseResponse<Void> logout() {
    userService.logout();
    return BaseResponse.success();
  }

  /**
   * RefreshToken을 받아 Token을 재발급한다.
   */
  @PostMapping("/token/reissue")
  @Operation(summary = "Token 재발급",
      description = "RefreshToken으로 Token을 재발급한다." +
          "\n### 응답 코드" +
          "\n- F000 : 정상적인 응답으로써 TOKEN 발부" +
          "\n- F011 : RefreshToken 만료" +
          "\n- F012 : 유효하지 않은 Token")
  @NoToken
  public BaseResponse<ReissueResponse> reissue(
      @Parameter(hidden = true) @RequestHeader(required = false) String authorization,
      @RequestBody ReissueRequest request) {
    if (StringUtils.isAnyBlank(request.getRefreshToken(), authorization)) {
      throw BaseException.of(ResponseMeta.PARAM_WRONG);
    }

    return BaseResponse.success(userService.reissue(authorization, request));
  }
}
