package ai.maum.data.edit.api.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(title = "로그인 응답")
public class LoginResponse {

  @Schema(title = "엑세스 토큰", description = "JWT 엑세스 토큰", example = "token1234@")
  private String accessToken;
  @Schema(title = "리프레시 토큰", description = "JWT 리프레시 토큰", example = "token1234@")
  private String refreshToken;

  public LoginResponse(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
